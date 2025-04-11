package com.qcadoo.mes.materialFlowResources;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.LookupUtils;
import com.qcadoo.mes.basic.controllers.dataProvider.DataProvider;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.dto.ColumnProperties;
import com.qcadoo.mes.materialFlowResources.service.ReservationsService;
import com.qcadoo.plugin.api.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.qcadoo.mes.materialFlowResources.listeners.DocumentsListListeners.MOBILE_WMS;
import static com.qcadoo.mes.materialFlowResources.listeners.DocumentsListListeners.REALIZED;

@Repository
public class DocumentPositionService {

    private static final String POSITION_ID = "positionId";

    private static final String DOCUMENT_ID = "documentId";

    private static final String ID = "id";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DocumentPositionValidator validator;

    @Autowired
    private LookupUtils lookupUtils;

    @Autowired
    private DataProvider dataProvider;

    @Autowired
    private DocumentPositionResourcesHelper positionResourcesHelper;

    @Autowired
    private ReservationsService reservationsService;

    @Autowired
    private AttributePositionService attributePositionService;

    @Autowired
    private PluginManager pluginManager;

    public GridResponse<DocumentPositionDTO> findAll(final Long documentId, final String _sidx, final String _sord, final int page,
                                                     final int perPage, final DocumentPositionDTO position, final Map<String, String> attributeFilters) {
        String sidx = _sidx != null ? _sidx : "";
        String sord = _sord != null ? _sord : "";

        Preconditions.checkState(Arrays.asList("asc", "desc", "").contains(sord));

        Map<String, Object> config = getGridConfig(documentId);

        List<ColumnProperties> columns = (List<ColumnProperties>) config.get("columns");

        List<String> attrCloumns = columns.stream().filter(c -> c.isChecked() && c.isForAttribute())
                .map(ColumnProperties::getName).collect(Collectors.toList());

        StringBuilder attrQueryPart = new StringBuilder();

        if (!attrCloumns.isEmpty()) {
            attrCloumns.forEach(ac -> {
                attrQueryPart.append(" , ");
                attrQueryPart.append("(SELECT string_agg(positionattributevalue.value, ', ') ");
                attrQueryPart.append("FROM materialflowresources_positionattributevalue positionattributevalue ");
                attrQueryPart.append("LEFT JOIN basic_attribute att ON att.id = positionattributevalue.attribute_id ");
                attrQueryPart.append("WHERE positionattributevalue.position_id = p.id AND att.number ='" + ac
                        + "' group by att.number) as \"" + ac + "\" ");
            });
        }

        String query = "SELECT %s FROM ( SELECT p.*, p.document_id AS document, product.number AS product, product.name AS productName, product.unit, typeofloadunit.name AS typeOfLoadUnit, "
                + "palletnumber.number AS palletnumber, location.number AS storagelocation, resource.number AS resource, batch.number as batch, batch.id as batchId, \n"
                + "(coalesce(r1.resourcesCount,0) < 2 AND p.quantity >= coalesce(resource.quantity,0)) AS lastResource, p.pickingdate AS pickingDate, staff.name || ' ' || staff.surname AS pickingWorker "
                + attrQueryPart
                + "	FROM materialflowresources_position p\n"
                + "	LEFT JOIN basic_product product ON (p.product_id = product.id)\n"
                + "	LEFT JOIN basic_palletnumber palletnumber ON (p.palletnumber_id = palletnumber.id)\n"
                + "	LEFT JOIN materialflowresources_resource resource ON (p.resource_id = resource.id)\n"
                + "	LEFT JOIN advancedgenealogy_batch batch ON (p.batch_id = batch.id)\n"
                + "	LEFT JOIN qcadoosecurity_user u ON (p.pickingworker_id = u.id)\n"
                + "	LEFT JOIN basic_staff staff ON (u.staff_id = staff.id)\n"
                + " LEFT JOIN basic_typeofloadunit typeofloadunit ON typeofloadunit.id = p.typeofloadunit_id \n"
                + " LEFT JOIN (SELECT palletnumber_id, count(id) as resourcesCount FROM materialflowresources_resource GROUP BY palletnumber_id) r1 ON r1.palletnumber_id = resource.palletnumber_id \n"
                + "	LEFT JOIN materialflowresources_storagelocation location ON (p.storagelocation_id = location.id) WHERE p.document_id = :documentId %s) q ";

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put(DOCUMENT_ID, documentId);

        query += lookupUtils.addQueryWhereForObject(position);

        parameters.putAll(lookupUtils.getParametersForObject(position));

        if (!attributeFilters.isEmpty()) {
            StringBuilder attributeFiltersBuilder = new StringBuilder();

            attributeFiltersBuilder.append("WHERE ");

            for (Map.Entry<String, String> filterElement : attributeFilters.entrySet()) {
                attributeFiltersBuilder.append("q.\"" + filterElement.getKey() + "\" ");
                attributeFiltersBuilder.append("ilike :" + filterElement.getKey().replaceAll("[^a-zA-Z0-9]+", "") + " ");

                parameters.put(filterElement.getKey().replaceAll("[^a-zA-Z0-9]+", ""), "%" + filterElement.getValue() + "%");
            }

            query = query + attributeFiltersBuilder.toString();
        }

        String queryCount = String.format(query, "COUNT(*)", "");

        String orderBy;

        if (sidx.startsWith("attrs.")) {
            orderBy = "\"" + sidx.replace("attrs.", "") + "\"";
        } else {
            orderBy = sidx;
        }

        String queryRecords = String.format(query, "*", "ORDER BY " + orderBy + " " + sord)
                + String.format(" LIMIT %d OFFSET %d", perPage, perPage * (page - 1));

        int countRecords = jdbcTemplate.queryForObject(queryCount, parameters, Long.class).intValue();

        List<DocumentPositionDTO> records = jdbcTemplate.query(queryRecords, parameters, (resultSet, i) -> {
            DocumentPositionDTO documentPositionDTO = new DocumentPositionDTO();
            documentPositionDTO.setId(resultSet.getLong(ID));
            documentPositionDTO.setDocument(resultSet.getLong("document"));
            documentPositionDTO.setNumber(resultSet.getInt("number"));
            documentPositionDTO.setProduct(resultSet.getString("product"));
            documentPositionDTO.setProductName(resultSet.getString("productName"));
            documentPositionDTO.setQuantity(resultSet.getBigDecimal("quantity"));
            documentPositionDTO.setUnit(resultSet.getString("unit"));
            documentPositionDTO.setGivenquantity(resultSet.getBigDecimal("givenquantity"));
            documentPositionDTO.setGivenunit(resultSet.getString("givenunit"));
            documentPositionDTO.setConversion(resultSet.getBigDecimal("conversion"));
            documentPositionDTO.setExpirationDate(resultSet.getDate("expirationDate"));
            documentPositionDTO.setProductionDate(resultSet.getDate("productionDate"));
            documentPositionDTO.setPalletNumber(resultSet.getString("palletNumber"));
            documentPositionDTO.setResourceNumber(resultSet.getString("resourceNumber"));
            documentPositionDTO.setPickingDate(resultSet.getDate("pickingDate"));
            documentPositionDTO.setPickingWorker(resultSet.getString("pickingWorker"));
            documentPositionDTO.setTypeOfLoadUnit(resultSet.getString("typeOfLoadUnit"));
            documentPositionDTO.setStorageLocation(resultSet.getString("storageLocation"));
            documentPositionDTO.setPrice(resultSet.getBigDecimal("price"));
            documentPositionDTO.setSellingPrice(resultSet.getBigDecimal("sellingPrice"));
            documentPositionDTO.setBatch(resultSet.getString("batch"));
            documentPositionDTO.setBatchId(resultSet.getLong("batchId"));
            documentPositionDTO.setResource(resultSet.getString("resource"));
            documentPositionDTO.setWaste(resultSet.getBoolean("waste"));
            documentPositionDTO.setLastResource(resultSet.getBoolean("lastResource"));

            if (!attrCloumns.isEmpty()) {
                Map<String, Object> attrs = Maps.newHashMap();

                for (String ac : attrCloumns) {
                    attrs.put(ac, resultSet.getString(ac));
                }

                documentPositionDTO.setAttrs(attrs);
            }

            return documentPositionDTO;
        });

        return new GridResponse<>(page, Double.valueOf(Math.ceil((1.0 * countRecords) / perPage)).intValue(), countRecords,
                records);
    }

    private void delete(final Long id) {
        validator.validateBeforeDelete(id);

        String deleteQuery = "DELETE FROM materialflowresources_positionattributevalue WHERE position_id = :positionId";

        Map<String, Object> paramsDeleteAttribute = Maps.newHashMap();

        paramsDeleteAttribute.put(POSITION_ID, id);

        jdbcTemplate.update(deleteQuery, paramsDeleteAttribute);

        Map<String, Object> params = Maps.newHashMap();

        params.put(ID, id);

        String queryForDocumentId = "SELECT document_id, product_id, resource_id, quantity FROM materialflowresources_position WHERE id = :id";

        Map<String, Object> result = jdbcTemplate.queryForMap(queryForDocumentId, params);

        params.putAll(result);

        reservationsService.deleteReservationFromDocumentPosition(params);

        jdbcTemplate.update("DELETE FROM materialflowresources_position WHERE id = :id ", params);
    }

    public void create(final DocumentPositionDTO documentPositionVO) {
        Long batchId = documentPositionVO.getBatchId();

        if (Objects.nonNull(batchId) && batchId == 0) {
            documentPositionVO.setBatchId(null);
        }

        Map<String, Object> params = validator.validateAndTryMapBeforeCreate(documentPositionVO);

        if (params.get(ID) == null || Long.parseLong(params.get(ID).toString()) == 0) {
            params.remove(ID);
        }

        String keys = String.join(", ", params.keySet());
        keys += ", resourcenumber";

        String values = params.keySet().stream().map(key -> ":" + key).collect(Collectors.joining(", "));
        values += ", '" + documentPositionVO.getResource() + "'";

        String query = String.format("INSERT INTO materialflowresources_position (%s) VALUES (%s) RETURNING id", keys, values);

        Long positionId = jdbcTemplate.queryForObject(query, params, Long.class);

        if (positionId != null) {
            params.put(ID, positionId);

            reservationsService.createReservationFromDocumentPosition(params);
        }

        attributePositionService.createOrUpdateAttributePositionValues(true, positionId, documentPositionVO.getAttrs());

        updateDocumentPositionsNumbers(documentPositionVO.getDocument());
    }

    public void update(final DocumentPositionDTO documentPositionVO) {
        Long batchId = documentPositionVO.getBatchId();

        if (Objects.nonNull(batchId) && batchId == 0) {
            documentPositionVO.setBatchId(null);
        }

        Map<String, Object> params = validator.validateAndTryMapBeforeUpdate(documentPositionVO);

        String set = params.keySet().stream().map(key -> key + "=:" + key).collect(Collectors.joining(", "));
        set += ", resourcenumber = '" + documentPositionVO.getResource() + "'";

        String query = String.format("UPDATE materialflowresources_position SET %s WHERE id = :id ", set);

        reservationsService.updateReservationFromDocumentPosition(params);
        jdbcTemplate.update(query, params);
        attributePositionService.createOrUpdateAttributePositionValues(false, documentPositionVO.getId(),
                documentPositionVO.getAttrs());

        updateDocumentPositionsNumbers(documentPositionVO.getDocument());
    }

    private List<StorageLocationDTO> getStorageLocations(String preparedQuery, final String query, final Map<String, Object> paramMap) {
        if (Strings.isNullOrEmpty(query)) {
            return Lists.newArrayList();
        } else {
            MapSqlParameterSource queryParameters = new MapSqlParameterSource(paramMap).addValue("query", '%' + query + '%');

            preparedQuery = preparedQuery.substring(0, preparedQuery.length() - 1); // remove trailing ';' char
            preparedQuery = preparedQuery + " LIMIT " + DataProvider.MAX_RESULTS + ';';

            return jdbcTemplate.query(preparedQuery, queryParameters,
                    BeanPropertyRowMapper.newInstance(StorageLocationDTO.class));
        }
    }

    public DataResponse getStorageLocationsResponse(final String query, final String product, final String document) {
        String preparedQuery;

        Map<String, Object> paramMap = Maps.newHashMap();

        paramMap.put("document", Integer.parseInt(document));

        if (Strings.isNullOrEmpty(product)) {
            preparedQuery = "SELECT sl.id, sl.number FROM materialflowresources_storagelocation sl "
                    + "WHERE sl.number ilike :query "
                    + "AND sl.location_id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document WHERE id = :document) "
                    + "AND sl.active = true;";
        } else {
            preparedQuery = "SELECT sl.id, sl.number FROM materialflowresources_storagelocation sl "
                    + "LEFT JOIN jointable_product_storagelocation psl ON psl.storagelocation_id = sl.id "
                    + "WHERE sl.number ilike :query "
                    + "AND sl.location_id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document WHERE id = :document) "
                    + "AND (psl.product_id IN (SELECT id FROM basic_product WHERE number LIKE :product) OR psl.product_id IS NULL) "
                    + "AND sl.active = true;";

            paramMap.put("product", product);
        }

        List<StorageLocationDTO> entities = getStorageLocations(preparedQuery, query, paramMap);

        return dataProvider.getDataResponse(query, preparedQuery, entities, paramMap);
    }

    public DataResponse getBatchesResponse(final String query, final String product) {
        if (StringUtils.isEmpty(product)) {
            return new DataResponse(Lists.newArrayList(), 0);
        } else {
            String preparedQuery;

            Map<String, Object> paramMap = Maps.newHashMap();

            paramMap.put("product", product);

            preparedQuery = "SELECT _batch.id, _batch.number as number, p.number as product "
                    + "FROM advancedgenealogy_batch _batch " + "LEFT JOIN basic_product p on p.id = _batch.product_id "
                    + "WHERE p.number = :product AND _batch.number ilike :query AND _batch.active=true";

            MapSqlParameterSource queryParameters = new MapSqlParameterSource(paramMap).addValue("query", '%' + query + '%');

            preparedQuery = preparedQuery + " LIMIT " + DataProvider.MAX_RESULTS + ';';

            List<BatchDTO> entities = jdbcTemplate.query(preparedQuery, queryParameters,
                    BeanPropertyRowMapper.newInstance(BatchDTO.class));

            return dataProvider.getDataResponse(query, preparedQuery, entities, paramMap);
        }
    }

    public Map<String, Object> getGridConfig(final Long documentId) {
        try {
            String query = "SELECT documentpositionparametersitem.*, attr.dataType as attributeDataType, attr.valueType as attributeValueType "
                    + "FROM materialflowresources_documentpositionparametersitem documentpositionparametersitem "
                    + "LEFT JOIN basic_attribute attr ON attr.id = documentpositionparametersitem.attribute_id  "
                    + "WHERE attr IS NULL OR attr.active = TRUE  ORDER BY documentpositionparametersitem.ordering";

            List<ColumnProperties> columns = jdbcTemplate.query(query, Collections.emptyMap(),
                    new BeanPropertyRowMapper(ColumnProperties.class));

            Boolean isOutDocument = (Boolean) isOutDocument(documentId);

            Map<String, Object> config = Maps.newHashMap();

            config.put("readOnly", isGridReadOnly(documentId));
            config.put("suggestResource", shouldSuggestResource());
            config.put("outDocument", isOutDocument);
            config.put("columns", columns);

            return config;
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyMap();
        }
    }

    public Map<String, Object> unitsOfProduct(final String productNumber) {
        try {
            Map<String, Object> units = getUnitsFromProduct(productNumber);

            units.put("available_additionalunits", getAvailableAdditionalUnitsByProduct(units));

            calculateConversion(units);

            return units;
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyMap();
        }
    }

    public ProductDTO getProductForProductNumber(final String number) {
        String _query = "SELECT product.id, product.number AS code, product.number, product.name, product.ean, product.globaltypeofmaterial, product.category "
                + "FROM basic_product product WHERE product.number = :number";

        List<ProductDTO> products = jdbcTemplate.query(_query, Collections.singletonMap("number", number),
                new BeanPropertyRowMapper(ProductDTO.class));

        if (products.size() == 1) {
            return products.get(0);
        } else {
            return null;
        }
    }

    public void updateDocumentPositionsNumbers(final Long documentId) {
        String query = "SELECT p.*, p.document_id AS document, product.number AS product, product.unit, palletnumber.number AS palletnumber, "
                + "location.number AS storagelocationnumber\n" + "	FROM materialflowresources_position p\n"
                + "	LEFT JOIN basic_product product ON (p.product_id = product.id)\n"
                + "	LEFT JOIN basic_palletnumber palletnumber ON (p.palletnumber_id = palletnumber.id)\n"
                + "	LEFT JOIN materialflowresources_storagelocation location ON (p.storagelocation_id = location.id) WHERE p.document_id = :documentId ORDER BY p.id";

        List<DocumentPositionDTO> list = jdbcTemplate.query(query, Collections.singletonMap(DOCUMENT_ID, documentId),
                new BeanPropertyRowMapper(DocumentPositionDTO.class));

        int index = 1;

        for (DocumentPositionDTO documentPositionDTO : list) {
            documentPositionDTO.setNumber(index);

            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("number", documentPositionDTO.getNumber());
            parameters.put(ID, documentPositionDTO.getId());

            String _query = "UPDATE materialflowresources_position SET number = :number WHERE id = :id ";

            jdbcTemplate.update(_query, parameters);

            index++;
        }
    }

    public Long findDocumentByPosition(final Long positionId) {
        String query = "SELECT p.document_id FROM materialflowresources_position p WHERE id = :id ";

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put(ID, positionId);

        return jdbcTemplate.queryForObject(query, parameters, Long.class);
    }

    private List<Map<String, Object>> getAvailableAdditionalUnitsByProduct(final Map<String, Object> units) {
        Long productId = Long.valueOf(units.get(ID).toString());

        String query = "SELECT convItem.unitto, convItem.quantityto, convItem.quantityfrom, dictItem.isinteger FROM qcadoomodel_unitconversionitem convItem "
                + " JOIN qcadoomodel_dictionaryitem dictItem ON dictItem.name = convItem.unitto "
                + " WHERE convItem.product_id = :id";

        List<Map<String, Object>> availableUnits = jdbcTemplate.queryForList(query, Collections.singletonMap(ID, productId));

        List<Map<String, Object>> result = availableUnits.stream().map(entry -> {
            Map<String, Object> type = Maps.newHashMap();

            type.put("value", entry.get("unitto"));
            type.put("key", entry.get("unitto"));
            type.put("conversion", entry.get("conversion"));
            type.put("quantityto", entry.get("quantityto"));
            type.put("quantityfrom", entry.get("quantityfrom"));
            type.put("isinteger", entry.get("isinteger"));

            return type;
        }).collect(Collectors.toList());

        Map<String, Object> type = Maps.newHashMap();

        type.put("value", units.get("unit"));
        type.put("key", units.get("unit"));
        type.put("quantityfrom", BigDecimal.valueOf(1));
        type.put("quantityto", BigDecimal.valueOf(1));
        type.put("isinteger", false);
        result.add(type);

        return result;
    }

    private void calculateConversion(final Map<String, Object> units) {
        List<Map<String, Object>> availableAdditionalUnits = (List<Map<String, Object>>) units.get("available_additionalunits");

        String additionalUnit = units.get("additionalunit").toString();

        Optional<Map<String, Object>> maybeEntry = availableAdditionalUnits.stream()
                .filter(entry -> entry.get("key").equals(additionalUnit)).findAny();

        if (maybeEntry.isPresent()) {
            units.put("quantityto", maybeEntry.get().get("quantityto"));
            units.put("quantityfrom", maybeEntry.get().get("quantityfrom"));
        } else {
            units.put("quantityto", 0);
            units.put("quantityfrom", 0);
        }
    }

    private Map<String, Object> getUnitsFromProduct(final String productNumber) {
        String query = "SELECT id, unit, additionalunit FROM basic_product WHERE number = :number";

        Map<String, Object> units = jdbcTemplate.queryForMap(query, Collections.singletonMap("number", productNumber));

        if (units.get("additionalunit") == null || units.get("additionalunit").toString().isEmpty()) {
            units.put("additionalunit", units.get("unit"));
        }

        return units;
    }

    private boolean isGridReadOnly(final Long documentId) {
        String query = "SELECT state FROM materialflowresources_document WHERE id = :id";

        String stateString = jdbcTemplate.queryForObject(query, Collections.singletonMap(ID, documentId), String.class);

        boolean readOnly = DocumentState.parseString(stateString) == DocumentState.ACCEPTED;

        if (pluginManager.isPluginEnabled(MOBILE_WMS)) {
            query = "SELECT wms, stateinwms FROM materialflowresources_document WHERE id = :id";

            Map<String, Object> documentResult = jdbcTemplate.queryForMap(query, Collections.singletonMap(ID, documentId));

            readOnly = readOnly || documentResult.get(DocumentFields.WMS) != null
                    && (boolean) documentResult.get(DocumentFields.WMS)
                    && !REALIZED.equals(documentResult.get(DocumentFields.STATE_IN_WMS));
        }

        return readOnly;
    }

    private boolean shouldSuggestResource() {
        String query = "SELECT suggestResource FROM materialflowresources_documentpositionparameters LIMIT 1";

        return jdbcTemplate.queryForObject(query, Collections.emptyMap(), Boolean.class);
    }

    private Object isOutDocument(final Long documentId) {
        String query = "SELECT type FROM materialflowresources_document WHERE id = :id";

        String type = jdbcTemplate.queryForObject(query, Collections.singletonMap(ID, documentId), String.class);

        return DocumentType.isOutbound(type);
    }

    public StorageLocationDTO getStorageLocation(final String product, final String document) {
        if (StringUtils.isEmpty(product)) {
            return null;
        }

        String query = "SELECT sl.id AS id, sl.number AS number, p.name AS product, l.name AS location "
                + "FROM materialflowresources_storagelocation sl "
                + "JOIN materialflow_location l ON l.id = sl.location_id "
                + "RIGHT JOIN jointable_product_storagelocation psl ON psl.storagelocation_id = sl.id "
                + "LEFT JOIN basic_product p ON p.id = psl.product_id "
                + "WHERE sl.location_id IN "
                + "(SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) AS location FROM materialflowresources_document WHERE id = :document) "
                + "AND sl.active = true AND p.number = :product LIMIT 1;";

        Map<String, Object> filter = Maps.newHashMap();

        filter.put("product", product);
        filter.put("document", Integer.parseInt(document));

        List<StorageLocationDTO> locations = jdbcTemplate.query(query, filter,
                new BeanPropertyRowMapper(StorageLocationDTO.class));

        if (locations.size() == 1) {
            return locations.get(0);
        } else {
            return null;
        }
    }

    public ProductDTO getProductFromLocation(final String location, final Long documentId) {
        if (StringUtils.isEmpty(location)) {
            return null;
        }

        String query = "SELECT p.number FROM materialflowresources_storagelocation sl "
                + "JOIN materialflow_location l ON l.id = sl.location_id "
                + "RIGHT JOIN jointable_product_storagelocation psl ON psl.storagelocation_id = sl.id "
                + "LEFT JOIN basic_product p ON p.id = psl.product_id "
                + "WHERE sl.number = :location "
                + "AND l.id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document WHERE id = :document);";

        Map<String, Object> filter = Maps.newHashMap();

        filter.put("location", location);
        filter.put("document", documentId);

        List<ProductDTO> products = jdbcTemplate.query(query, filter, BeanPropertyRowMapper.newInstance(ProductDTO.class));

        if (products.isEmpty()) {
            return null;
        } else {
            return products.get(0);
        }
    }

    public ResourceDTO getResource(final Long document, final String product, final BigDecimal conversion, final Long batchId) {
        boolean useBatch = Objects.nonNull(batchId);

        Map<String, Object> filter = Maps.newHashMap();

        filter.put("product", product);
        filter.put("conversion", conversion);
        filter.put("context", document);

        if (useBatch) {
            filter.put("batch", batchId);
        }

        String query = positionResourcesHelper.getResourceQuery(document, false, useBatch);

        List<ResourceDTO> batches = jdbcTemplate.query(query, filter, new BeanPropertyRowMapper(ResourceDTO.class));

        batches = batches.stream().filter(resource -> resource.getAvailableQuantity().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (batches.isEmpty()) {
            return null;
        } else {
            return batches.get(0);
        }
    }

    public List<AbstractDTO> getResources(final Long document, final String additionalQuery, final String product, final BigDecimal conversion,
                                          boolean useBatch, Long batchId) {
        if (Strings.isNullOrEmpty(additionalQuery) || Strings.isNullOrEmpty(product)) {
            return Lists.newArrayList();
        } else {
            Map<String, Object> paramMap = Maps.newHashMap();

            paramMap.put("query", additionalQuery);
            paramMap.put("product", product);
            paramMap.put("conversion", conversion);
            paramMap.put("context", document);

            if (useBatch) {
                paramMap.put("batch", batchId);
            }

            String query = positionResourcesHelper.getResourceQuery(document, true, useBatch);

            return jdbcTemplate.query(query, paramMap, new BeanPropertyRowMapper(ResourceDTO.class));
        }
    }

    public DataResponse getResourcesResponse(final Long document, final String query, final String product,
                                             final BigDecimal conversion, final Long batchId, final boolean shouldCheckMaxResults) {
        if (Strings.isNullOrEmpty(product)) {
            return new DataResponse(Lists.newArrayList(), 0);
        }

        boolean useBatch = Objects.nonNull(batchId);

        String ilikeValue = "%" + query + "%";
        ilikeValue = ilikeValue.replace("*", "%");
        ilikeValue = ilikeValue.replace("%%", "%");

        List<AbstractDTO> entities = getResources(document, ilikeValue, product, conversion,
                useBatch, batchId);

        if (entities.isEmpty()) {
            entities = getResources(document, ilikeValue, product, conversion, useBatch, batchId);
        }

        Map<String, Object> paramMap = Maps.newHashMap();

        paramMap.put("product", product);
        paramMap.put("conversion", conversion);
        paramMap.put("context", document);

        if (useBatch) {
            paramMap.put("batch", batchId);
        }

        String preparedQuery = positionResourcesHelper.getResourceQuery(document, true, useBatch);

        return dataProvider.getDataResponse(ilikeValue, preparedQuery, entities, paramMap, shouldCheckMaxResults);
    }

    public ResourceDTO getResourceByNumber(final String resource) {
        String query = "SELECT r.*, batch.number as batch, sl.number AS storageLocation, typeofloadunit.name AS typeOfLoadUnit, "
                + "pn.number AS palletNumber, coalesce(r1.resourcesCount,0) < 2 AS lastResource "
                + "FROM materialflowresources_resource r \n"
                + "LEFT JOIN (SELECT palletnumber_id, count(id) AS resourcesCount FROM materialflowresources_resource GROUP BY palletnumber_id) r1 ON r1.palletnumber_id = r.palletnumber_id \n"
                + "LEFT JOIN materialflowresources_storagelocation sl ON sl.id = storageLocation_id \n"
                + "LEFT JOIN basic_typeofloadunit typeofloadunit ON typeofloadunit.id = r.typeofloadunit_id \n"
                + "LEFT JOIN advancedgenealogy_batch batch ON batch.id = r.batch_id \n"
                + "LEFT JOIN basic_palletnumber pn ON pn.id = r.palletnumber_id "
                + "WHERE r.number = :resource";

        Map<String, Object> filter = Maps.newHashMap();

        filter.put("resource", resource);

        List<ResourceDTO> batches = jdbcTemplate.query(query, filter, new BeanPropertyRowMapper(ResourceDTO.class));

        if (batches.isEmpty()) {
            return null;
        } else {
            ResourceDTO resourceDTO = batches.get(0);

            Map<String, Object> params = Maps.newHashMap();

            params.put("resourceId", resourceDTO.getId());

            String attrBuilder = "SELECT att.number, resourceattributevalue.value "
                    + "FROM materialflowresources_resourceattributevalue resourceattributevalue "
                    + "LEFT JOIN materialflowresources_resource res ON res.id = resourceattributevalue.resource_id "
                    + "LEFT JOIN basic_attribute att ON att.id = resourceattributevalue.attribute_id "
                    + "WHERE res.id = :resourceId";

            List<AttributeDto> attributes = jdbcTemplate.query(attrBuilder, params,
                    new BeanPropertyRowMapper(AttributeDto.class));

            Map<String, Object> attributeMap = Maps.newHashMap();

            attributes.forEach(att -> {
                attributeMap.put(att.getNumber(), att.getValue());
            });

            resourceDTO.setAttrs(attributeMap);

            return resourceDTO;
        }
    }

    public String getTypeOfLoadUnitByPalletNumber(final Long documentId, final String loadUnitNumber) {
        String query = "(SELECT typeofloadunit.name "
                + "FROM materialflowresources_resource resource "
                + "LEFT JOIN basic_palletnumber palletnumber "
                + "ON palletnumber.id = resource.palletnumber_id "
                + "LEFT JOIN basic_typeofloadunit typeofloadunit "
                + "ON typeofloadunit.id = resource.typeofloadunit_id "
                + "WHERE palletnumber.number = :loadUnitNumber "
                + "AND resource.location_id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document WHERE id = :documentId) "
                + "UNION "
                + "SELECT typeofloadunit.name "
                + "FROM materialflowresources_position p "
                + "LEFT JOIN basic_palletnumber palletnumber "
                + "ON palletnumber.id = p.palletnumber_id "
                + "LEFT JOIN basic_typeofloadunit typeofloadunit "
                + "ON typeofloadunit.id = p.typeofloadunit_id "
                + "WHERE palletnumber.number = :loadUnitNumber "
                + "AND p.document_id = :documentId) "
                + "LIMIT 1";

        Map<String, Object> filter = Maps.newHashMap();

        filter.put("documentId", documentId);
        filter.put("loadUnitNumber", loadUnitNumber);

        try {
            return jdbcTemplate.queryForObject(query, filter, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void deletePositions(final String ids) {
        List<String> items = Arrays.asList(ids.split("\\s*,\\s*"));

        Long documentId = findDocumentByPosition(Long.valueOf(items.stream().findFirst().get()));

        items.forEach(id -> {
            delete(Long.valueOf(id));
        });

        updateDocumentPositionsNumbers(documentId);
    }

}
