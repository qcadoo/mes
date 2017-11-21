package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.LookupUtils;
import com.qcadoo.mes.basic.controllers.dataProvider.DataProvider;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.service.ReservationsService;

@Repository
public class DocumentPositionService {

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

    public GridResponse<DocumentPositionDTO> findAll(final Long documentId, final String _sidx, final String _sord, int page,
            int perPage, final DocumentPositionDTO position) {
        String query = "SELECT %s FROM ( SELECT p.*, p.document_id AS document, product.number AS product, product.name AS productName, product.unit, additionalcode.code AS additionalcode, "
                + "palletnumber.number AS palletnumber, location.number AS storagelocation, resource.number AS resource, \n"
                + "(coalesce(r1.resourcesCount,0) < 2 AND p.quantity >= coalesce(resource.quantity,0)) AS lastResource "
                + "	FROM materialflowresources_position p\n"
                + "	LEFT JOIN basic_product product ON (p.product_id = product.id)\n"
                + "	LEFT JOIN basic_additionalcode additionalcode ON (p.additionalcode_id = additionalcode.id)\n"
                + "	LEFT JOIN basic_palletnumber palletnumber ON (p.palletnumber_id = palletnumber.id)\n"
                + "	LEFT JOIN materialflowresources_resource resource ON (p.resource_id = resource.id)\n"
                + " LEFT JOIN (SELECT palletnumber_id, count(id) as resourcesCount FROM materialflowresources_resource GROUP BY palletnumber_id) r1 ON r1.palletnumber_id = resource.palletnumber_id \n"
                + "	LEFT JOIN materialflowresources_storagelocation location ON (p.storagelocation_id = location.id) WHERE p.document_id = :documentId %s) q ";

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("documentId", documentId);

        return lookupUtils.getGridResponse(query, _sidx, _sord, page, perPage, position, parameters);
    }

    public void delete(final Long id) {
        validator.validateBeforeDelete(id);

        Map<String, Object> params = Maps.newHashMap();

        params.put("id", id);

        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("DELETE FROM materialflowresources_position WHERE id = :id ");

        String queryForDocumentId = "SELECT document_id, product_id, resource_id, quantity FROM materialflowresources_position WHERE id = :id";

        Map<String, Object> result = jdbcTemplate.queryForMap(queryForDocumentId, params);

        params.putAll(result);

        reservationsService.deleteReservationFromDocumentPosition(params);
        jdbcTemplate.update(queryBuilder.toString(), params);
    }

    public void create(final DocumentPositionDTO documentPositionVO) {
        Map<String, Object> params = validator.validateAndTryMapBeforeCreate(documentPositionVO);

        if (params.get("id") == null || Long.valueOf(params.get("id").toString()) == 0) {
            params.remove("id");
        }

        String keys = params.keySet().stream().collect(Collectors.joining(", "));
        keys += ", resourcenumber";
        String values = params.keySet().stream().map(key -> {
            return ":" + key;
        }).collect(Collectors.joining(", "));
        values += ", '" + documentPositionVO.getResource() + "'";

        String query = String.format("INSERT INTO materialflowresources_position (%s) "

        + "VALUES (%s) RETURNING id", keys, values);

        Long positionId = jdbcTemplate.queryForObject(query, params, Long.class);

        if (positionId != null) {
            params.put("id", positionId);

            reservationsService.createReservationFromDocumentPosition(params);
        }
    }

    public void update(final Long id, final DocumentPositionDTO documentPositionVO) {
        Map<String, Object> params = validator.validateAndTryMapBeforeUpdate(documentPositionVO);

        String set = params.keySet().stream().map(key -> {
            return key + "=:" + key;
        }).collect(Collectors.joining(", "));
        set += ", resourcenumber = '" + documentPositionVO.getResource() + "'";

        String query = String.format("UPDATE materialflowresources_position " + "SET %s " + "WHERE id = :id ", set);

        reservationsService.updateReservationFromDocumentPosition(params);
        jdbcTemplate.update(query, params);
    }

    public List<AbstractDTO> getStorageLocations(final String q, final String product, final String document) {
        if (Strings.isNullOrEmpty(q)) {
            return Lists.newArrayList();
        } else {
            Map<String, Object> paramMap = Maps.newHashMap();

            paramMap.put("q", '%' + q + '%');
            paramMap.put("document", Integer.parseInt(document));

            if (Strings.isNullOrEmpty(product)) {
                String query = "SELECT id, number FROM materialflowresources_storagelocation WHERE number ilike :q "
                        + "AND location_id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document WHERE id = :document) "
                        + "LIMIT 20;";

                return jdbcTemplate.query(query, paramMap, new BeanPropertyRowMapper(StorageLocationDTO.class));
            } else {
                String query = "SELECT id, number FROM materialflowresources_storagelocation WHERE number ilike :q "
                        + "AND location_id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document WHERE id = :document) "
                        + "AND (product_id IN (SELECT id FROM basic_product WHERE number LIKE :product) OR product_id IS NULL) LIMIT 20;";
                paramMap.put("product", product);

                return jdbcTemplate.query(query, paramMap, new BeanPropertyRowMapper(StorageLocationDTO.class));
            }
        }
    }

    public DataResponse getStorageLocationsResponse(final String q, String product, String document) {
        String preparedQuery;

        if (Strings.isNullOrEmpty(product)) {
            preparedQuery = "SELECT id, number FROM materialflowresources_storagelocation WHERE number ilike :query "
                    + "AND location_id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document WHERE id = "
                    + Integer.parseInt(document) + ") AND active = true; ";
        } else {

            preparedQuery = "SELECT id, number FROM materialflowresources_storagelocation WHERE number ilike :query "
                    + "AND location_id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document WHERE id = "
                    + Integer.parseInt(document) + ") " + "AND (product_id IN (SELECT id FROM basic_product WHERE number LIKE '"
                    + product + "') OR product_id IS NULL) AND active = true;";
        }

        List<AbstractDTO> entities = getStorageLocations(q, product, document);

        Map<String, Object> paramMap = Maps.newHashMap();

        return dataProvider.getDataResponse(q, preparedQuery, entities, paramMap);
    }

    public Map<String, Object> getGridConfig(final Long documentId) {
        try {
            String query = "SELECT * FROM materialflowresources_documentpositionparametersitem ORDER BY ordering";

            List<Map<String, Object>> items = jdbcTemplate.queryForList(query, Collections.EMPTY_MAP);

            Map<String, Object> config = Maps.newHashMap();

            config.put("readOnly", isGridReadOnly(documentId));
            config.put("suggestResource", shouldSuggestResource());
            config.put("outDocument", isOutDocument(documentId));
            config.put("inBufferDocument", isInBufferDocument(documentId));

            Map<String, Object> columns = Maps.newLinkedHashMap();

            for (Map<String, Object> item : items) {
                columns.put(item.get("name").toString(), item.get("checked"));
            }

            config.put("columns", columns);

            return config;
        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_MAP;
        }
    }

    public Map<String, Object> unitsOfProduct(final String productNumber) {
        try {
            Map<String, Object> units = getUnitsFromProduct(productNumber);

            units.put("available_additionalunits", getAvailableAdditionalUnitsByProduct(units));

            calculateConversion(units);

            return units;
        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_MAP;
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
        String query = "SELECT p.*, p.document_id AS document, product.number AS product, product.unit, additionalcode.code AS additionalcode, palletnumber.number AS palletnumber, "
                + "location.number AS storagelocationnumber\n"
                + "	FROM materialflowresources_position p\n"
                + "	LEFT JOIN basic_product product ON (p.product_id = product.id)\n"
                + "	LEFT JOIN basic_additionalcode additionalcode ON (p.additionalcode_id = additionalcode.id)\n"
                + "	LEFT JOIN basic_palletnumber palletnumber ON (p.palletnumber_id = palletnumber.id)\n"
                + "	LEFT JOIN materialflowresources_storagelocation location ON (p.storagelocation_id = location.id) WHERE p.document_id = :documentId ORDER BY p.number";

        List<DocumentPositionDTO> list = jdbcTemplate.query(query, Collections.singletonMap("documentId", documentId),
                new BeanPropertyRowMapper(DocumentPositionDTO.class));
        int index = 1;

        for (DocumentPositionDTO documentPositionDTO : list) {
            documentPositionDTO.setNumber(index);

            Map<String, Object> parameters = Maps.newHashMap();

            parameters.put("number", documentPositionDTO.getNumber());
            parameters.put("id", documentPositionDTO.getId());

            String _query = "UPDATE materialflowresources_position SET number = :number WHERE id = :id ";

            jdbcTemplate.update(_query, parameters);

            index++;
        }
    }

    public Long findDocumentByPosition(final Long positionId) {
        String query = "SELECT p.document_id FROM materialflowresources_position p WHERE id = :id ";

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("id", positionId);

        Long documentId = jdbcTemplate.queryForObject(query, parameters, Long.class);

        return documentId;
    }

    private List<Map<String, Object>> getAvailableAdditionalUnitsByProduct(final Map<String, Object> units) {
        Long productId = Long.valueOf(units.get("id").toString());

        String query = "SELECT convItem.unitto, convItem.quantityto, convItem.quantityfrom, dictItem.isinteger FROM qcadoomodel_unitconversionitem convItem "
                + " JOIN qcadoomodel_dictionaryitem dictItem ON dictItem.name = convItem.unitto "
                + " WHERE convItem.product_id = :id";

        List<Map<String, Object>> availableUnits = jdbcTemplate.queryForList(query, Collections.singletonMap("id", productId));

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

        Optional<Map<String, Object>> maybeEntry = availableAdditionalUnits.stream().filter(entry -> {
            return entry.get("key").equals(additionalUnit);
        }).findAny();

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
        String stateString = jdbcTemplate.queryForObject(query, Collections.singletonMap("id", documentId), String.class);

        return DocumentState.parseString(stateString) == DocumentState.ACCEPTED;
    }

    private boolean shouldSuggestResource() {
        String query = "SELECT suggestResource FROM materialflowresources_documentpositionparameters LIMIT 1";

        return jdbcTemplate.queryForObject(query, Collections.EMPTY_MAP, Boolean.class);
    }

    private Object isOutDocument(final Long documentId) {
        String query = "SELECT type FROM materialflowresources_document WHERE id = :id";

        String stateString = jdbcTemplate.queryForObject(query, Collections.singletonMap("id", documentId), String.class);

        DocumentType type = DocumentType.parseString(stateString);

        return type == DocumentType.INTERNAL_OUTBOUND || type == DocumentType.RELEASE || type == DocumentType.TRANSFER;
    }

    private boolean isInBufferDocument(final Long documentId) {
        String query = "SELECT inbuffer FROM materialflowresources_document WHERE id = :id";

        return jdbcTemplate.queryForObject(query, Collections.singletonMap("id", documentId), Boolean.class);
    }

    public StorageLocationDTO getStorageLocation(final String product, final String document) {
        if (StringUtils.isEmpty(product)) {
            return null;
        }

        String query = "SELECT sl.id, sl.number AS number, p.name AS product, loc.name AS location FROM materialflowresources_storagelocation sl JOIN basic_product p ON p.id = sl.product_id JOIN materialflow_location loc ON loc.id = sl.location_id\n"
                + "WHERE location_id in\n"
                + "(SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) AS location FROM materialflowresources_document WHERE id = :document) AND sl.active = true AND p.number = :product LIMIT 1;";

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

    public ProductDTO getProductFromLocation(final String location) {
        if (StringUtils.isEmpty(location)) {
            return null;
        }

        String query = "SELECT p.number FROM materialflowresources_storagelocation l JOIN basic_product p ON l.product_id = p.id WHERE l.number = :location;";

        Map<String, Object> filter = Maps.newHashMap();

        filter.put("location", location);

        List<ProductDTO> products = jdbcTemplate.query(query, filter, new BeanPropertyRowMapper(ProductDTO.class));

        if (products.isEmpty()) {
            return null;
        } else {
            return products.get(0);
        }
    }

    public ResourceDTO getResource(final Long document, final String product, final BigDecimal conversion,
            final String additionalCode) {
        boolean useAdditionalCode = org.apache.commons.lang3.StringUtils.isNotEmpty(additionalCode);

        Map<String, Object> filter = Maps.newHashMap();

        filter.put("product", product);
        filter.put("conversion", conversion);
        filter.put("context", document);

        if (useAdditionalCode) {
            filter.put("add_code", additionalCode);
        }

        String query = positionResourcesHelper.getResourceQuery(document, false, useAdditionalCode);

        List<ResourceDTO> batches = jdbcTemplate.query(query, filter, new BeanPropertyRowMapper(ResourceDTO.class));

        if (batches.isEmpty() && useAdditionalCode) {
            query = positionResourcesHelper.getResourceQuery(document, false, false);

            batches = jdbcTemplate.query(query, filter, new BeanPropertyRowMapper(ResourceDTO.class));
        }
        batches = batches.stream().filter(resource -> resource.getAvailableQuantity().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (batches.isEmpty()) {
            return null;
        } else {
            return batches.get(0);
        }
    }

    public List<AbstractDTO> getResources(final Long document, final String q, final String product, final BigDecimal conversion,
            boolean useAdditionalCode, final String additionalCode) {
        if (Strings.isNullOrEmpty(q) || Strings.isNullOrEmpty(product)) {
            return Lists.newArrayList();
        } else {
            Map<String, Object> paramMap = Maps.newHashMap();

            paramMap.put("query", '%' + q + '%');
            paramMap.put("product", product);
            paramMap.put("conversion", conversion);
            paramMap.put("context", document);

            if (useAdditionalCode) {
                paramMap.put("add_code", additionalCode);
            }

            String query = positionResourcesHelper.getResourceQuery(document, true, useAdditionalCode);

            return jdbcTemplate.query(query, paramMap, new BeanPropertyRowMapper(ResourceDTO.class));
        }
    }

    public DataResponse getResourcesResponse(final Long document, final String q, final String product,
            final BigDecimal conversion, final String additionalCode, boolean shouldCheckMaxResults) {
        if (Strings.isNullOrEmpty(product)) {
            return new DataResponse(Lists.newArrayList(), 0);
        }

        boolean useAdditionalCode = org.apache.commons.lang3.StringUtils.isNotEmpty(additionalCode);

        String query = '%' + q + '%';

        List<AbstractDTO> entities = getResources(document, query, product, conversion, useAdditionalCode, additionalCode);

        if (entities.isEmpty() && useAdditionalCode) {
            useAdditionalCode = false;

            entities = getResources(document, query, product, conversion, false, additionalCode);
        }

        Map<String, Object> paramMap = Maps.newHashMap();

        paramMap.put("product", product);
        paramMap.put("conversion", conversion);
        paramMap.put("context", document);

        if (useAdditionalCode) {
            paramMap.put("add_code", additionalCode);
        }

        String preparedQuery = positionResourcesHelper.getResourceQuery(document, true, useAdditionalCode);

        return dataProvider.getDataResponse(query, preparedQuery, entities, paramMap, shouldCheckMaxResults);
    }

    public ResourceDTO getResourceByNumber(final String resource) {
        String query = "SELECT r.*, sl.number AS storageLocation, pn.number AS palletNumber, ac.code AS additionalCode, \n"
                + "coalesce(r1.resourcesCount,0) < 2 AS lastResource "
                + "FROM materialflowresources_resource r \n"
                + "LEFT JOIN (SELECT palletnumber_id, count(id) as resourcesCount FROM materialflowresources_resource GROUP BY palletnumber_id) r1 ON r1.palletnumber_id = r.palletnumber_id \n"
                + "LEFT JOIN materialflowresources_storagelocation sl ON sl.id = storageLocation_id \n"
                + "LEFT JOIN basic_additionalcode ac ON ac.id = additionalcode_id \n"
                + "LEFT JOIN basic_palletnumber pn ON pn.id = r.palletnumber_id WHERE r.number = :resource";

        Map<String, Object> filter = Maps.newHashMap();

        filter.put("resource", resource);

        List<ResourceDTO> batches = jdbcTemplate.query(query, filter, new BeanPropertyRowMapper(ResourceDTO.class));

        if (batches.isEmpty()) {
            return null;
        } else {
            return batches.get(0);
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
