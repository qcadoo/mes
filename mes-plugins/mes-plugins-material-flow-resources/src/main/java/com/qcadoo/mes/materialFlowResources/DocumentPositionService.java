package com.qcadoo.mes.materialFlowResources;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.LookupUtils;
import com.qcadoo.mes.basic.controllers.dataProvider.DataProvider;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.DataResponse;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

    public GridResponse<DocumentPositionDTO> findAll(final Long documentId, final String _sidx, final String _sord, int page,
            int perPage, DocumentPositionDTO position) {
        String query = "SELECT %s FROM ( SELECT p.*, p.document_id as document, product.number as product, product.unit, additionalcode.code as additionalcode, "
                + "palletnumber.number as palletnumber, location.number as storagelocation, resource.number as resource \n"
                + "	FROM materialflowresources_position p\n"
                + "	left join basic_product product on (p.product_id = product.id)\n"
                + "	left join basic_additionalcode additionalcode on (p.additionalcode_id = additionalcode.id)\n"
                + "	left join basic_palletnumber palletnumber on (p.palletnumber_id = palletnumber.id)\n"
                + "	left join materialflowresources_resource resource on (p.resource_id = resource.id)\n"
                + "	left join materialflowresources_storagelocation location on (p.storagelocation_id = location.id) WHERE p.document_id = :documentId %s) q ";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("documentId", documentId);

        return lookupUtils.getGridResponse(query, _sidx, _sord, page, perPage, position, parameters);
    }

    public void delete(Long id) {
        validator.validateBeforeDelete(id);

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM materialflowresources_position WHERE id = :id ");
        jdbcTemplate.update(queryBuilder.toString(), Collections.singletonMap("id", id));
    }

    public void create(DocumentPositionDTO documentPositionVO) {
        Map<String, Object> params = validator.validateAndTryMapBeforeCreate(documentPositionVO);

        if (params.get("id") == null || Long.valueOf(params.get("id").toString()) == 0) {
            params.remove("id");
        }

        String keys = params.keySet().stream().collect(Collectors.joining(", "));
        String values = params.keySet().stream().map(key -> {
            return ":" + key;
        }).collect(Collectors.joining(", "));

        String query = String.format("INSERT INTO materialflowresources_position (%s) VALUES (%s)", keys, values);

        jdbcTemplate.update(query, params);
    }

    public void update(Long id, DocumentPositionDTO documentPositionVO) {
        Map<String, Object> params = validator.validateAndTryMapBeforeUpdate(documentPositionVO);

        String set = params.keySet().stream().map(key -> {
            return key + "=:" + key;
        }).collect(Collectors.joining(", "));
        String query = String.format("UPDATE materialflowresources_position SET %s WHERE id = :id ", set);

        jdbcTemplate.update(query, params);
    }

    public List<AbstractDTO> getStorageLocations(String q, String product, String document) {

        if (Strings.isNullOrEmpty(q)) {
            return Lists.newArrayList();
        } else {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("q", '%' + q + '%');
            paramMap.put("document", Integer.parseInt(document));
            if (Strings.isNullOrEmpty(product)) {
                String query = "SELECT id, number from materialflowresources_storagelocation WHERE number ilike :q "
                        + "AND location_id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document where id = :document) "
                        + "LIMIT 20;";
                return jdbcTemplate.query(query, paramMap, new BeanPropertyRowMapper(StorageLocationDTO.class));
            } else {
                String query = "SELECT id, number from materialflowresources_storagelocation WHERE number ilike :q "
                        + "AND location_id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document where id = :document) "
                        + "AND (product_id IN (SELECT id FROM basic_product WHERE number LIKE :product) OR product_id IS NULL) LIMIT 20;";
                paramMap.put("product", product);
                return jdbcTemplate.query(query, paramMap, new BeanPropertyRowMapper(StorageLocationDTO.class));
            }

        }
    }

    public DataResponse getStorageLocationsResponse(String q, String product, String document) {
        String preparedQuery;
        if (Strings.isNullOrEmpty(product)) {
            preparedQuery = "SELECT id, number from materialflowresources_storagelocation WHERE number ilike :query "
                    + "AND location_id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document where id = "
                    + Integer.parseInt(document) + "); ";
        } else {

            preparedQuery = "SELECT id, number from materialflowresources_storagelocation WHERE number ilike :query "
                    + "AND location_id IN (SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) FROM materialflowresources_document where id = "
                    + Integer.parseInt(document) + ") " + "AND (product_id IN (SELECT id FROM basic_product WHERE number LIKE '"
                    + product + "') OR product_id IS NULL);";
        }
        List<AbstractDTO> entities = getStorageLocations(q, product, document);
        Map<String, Object> paramMap = new HashMap<>();
        return dataProvider.getDataResponse(q, preparedQuery, entities, paramMap);
    }

    public Map<String, Object> getGridConfig(Long documentId) {
        try {
            String query = "select * from materialflowresources_documentpositionparametersitem";
            List<Map<String, Object>> items = jdbcTemplate.queryForList(query, Collections.EMPTY_MAP);

            Map<String, Object> config = new HashMap<>();
            config.put("readOnly", isGridReadOnly(documentId));
            config.put("suggestResource", shouldSuggestResource());
            config.put("outDocument", isOutDocument(documentId));

            for (Map<String, Object> item : items) {
                config.put("show" + item.get("name").toString().toLowerCase(), item.get("checked"));
            }

            return config;

        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_MAP;
        }
    }

    public Map<String, Object> unitsOfProduct(String productNumber) {
        try {
            Map<String, Object> units = getUnitsFromProduct(productNumber);
            units.put("available_additionalunits", getAvailableAdditionalUnitsByProduct(units));
            calculateConversion(units);

            return units;

        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_MAP;
        }
    }

    public void updateDocumentPositionsNumbers(final Long documentId) {
        String query = "SELECT p.*, p.document_id as document, product.number as product, product.unit, additionalcode.code as additionalcode, palletnumber.number as palletnumber, "
                + "location.number as storagelocationnumber\n"
                + "	FROM materialflowresources_position p\n"
                + "	left join basic_product product on (p.product_id = product.id)\n"
                + "	left join basic_additionalcode additionalcode on (p.additionalcode_id = additionalcode.id)\n"
                + "	left join basic_palletnumber palletnumber on (p.palletnumber_id = palletnumber.id)\n"
                + "	left join materialflowresources_storagelocation location on (p.storagelocation_id = location.id) WHERE p.document_id = :documentId ORDER BY p.number";

        List<DocumentPositionDTO> list = jdbcTemplate.query(query, Collections.singletonMap("documentId", documentId),
                new BeanPropertyRowMapper(DocumentPositionDTO.class));
        int index = 1;
        for (DocumentPositionDTO documentPositionDTO : list) {
            documentPositionDTO.setNumber(index);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("number", documentPositionDTO.getNumber());
            parameters.put("id", documentPositionDTO.getId());
            String _query = "UPDATE materialflowresources_position SET number = :number WHERE id = :id ";
            jdbcTemplate.update(_query, parameters);
            index++;
        }

    }

    public Long findDocumentByPosition(final Long positionId) {
        String query = "SELECT p.document_id FROM materialflowresources_position p WHERE id = :id ";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("id", positionId);
        Long documentId = jdbcTemplate.queryForObject(query, parameters, Long.class);
        return documentId;
    }

    private List<Map<String, Object>> getAvailableAdditionalUnitsByProduct(Map<String, Object> units) {
        Long productId = Long.valueOf(units.get("id").toString());
        String query = "select unitto, quantityto, quantityfrom from qcadoomodel_unitconversionitem  where product_id = :id";
        List<Map<String, Object>> availableUnits = jdbcTemplate.queryForList(query, Collections.singletonMap("id", productId));

        List<Map<String, Object>> result = availableUnits.stream().map(entry -> {
            Map<String, Object> type = new HashMap<>();
            type.put("value", entry.get("unitto"));
            type.put("key", entry.get("unitto"));
            type.put("conversion", entry.get("conversion"));
            type.put("quantityto", entry.get("quantityto"));
            type.put("quantityfrom", entry.get("quantityfrom"));

            return type;
        }).collect(Collectors.toList());

        Map<String, Object> type = new HashMap<>();
        type.put("value", units.get("unit"));
        type.put("key", units.get("unit"));
        type.put("quantityfrom", BigDecimal.valueOf(1));
        type.put("quantityto", BigDecimal.valueOf(1));
        result.add(type);

        return result;
    }

    private void calculateConversion(Map<String, Object> units) {
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

    private Map<String, Object> getUnitsFromProduct(String productNumber) {
        String query = "SELECT id, unit, additionalunit FROM basic_product WHERE number = :number";
        Map<String, Object> units = jdbcTemplate.queryForMap(query, Collections.singletonMap("number", productNumber));

        if (units.get("additionalunit") == null || units.get("additionalunit").toString().isEmpty()) {
            units.put("additionalunit", units.get("unit"));
        }

        return units;
    }

    private boolean isGridReadOnly(Long documentId) {
        String query = "select state from materialflowresources_document WHERE id = :id";
        String stateString = jdbcTemplate.queryForObject(query, Collections.singletonMap("id", documentId), String.class);

        return DocumentState.parseString(stateString) == DocumentState.ACCEPTED;
    }

    private boolean shouldSuggestResource() {
        String query = "select suggestResource from materialflowresources_documentpositionparameters limit 1";
        Boolean suggestResource = jdbcTemplate.queryForObject(query, Collections.EMPTY_MAP, Boolean.class);
        return suggestResource;
    }

    private Object isOutDocument(Long documentId) {
        String query = "select type from materialflowresources_document WHERE id = :id";
        String stateString = jdbcTemplate.queryForObject(query, Collections.singletonMap("id", documentId), String.class);
        DocumentType type = DocumentType.parseString(stateString);
        return type == DocumentType.INTERNAL_OUTBOUND || type == DocumentType.RELEASE || type == DocumentType.TRANSFER;
    }

    public StorageLocationDTO getStorageLocation(String product, String document) {
        if (StringUtils.isEmpty(product)) {
            return null;
        }
        String query = "select sl.id, sl.number as number, p.name as product, loc.name as location from materialflowresources_storagelocation sl join basic_product p on p.id = sl.product_id join materialflow_location loc on loc.id = sl.location_id\n"
                + "where location_id in\n"
                + "(SELECT DISTINCT COALESCE(locationfrom_id, locationto_id) as location from materialflowresources_document WHERE id = :document) AND p.number = :product LIMIT 1;";
        Map<String, Object> filter = new HashMap<>();
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

    public String getProductFromLocation(String location) {
        if (StringUtils.isEmpty(location)) {
            return null;
        }
        String query = "SELECT p.name FROM materialflowresources_storagelocation l join basic_product p on l.product_id = p.id WHERE l.number = :location;";
        Map<String, Object> filter = new HashMap<>();
        filter.put("location", location);
        List<String> products = jdbcTemplate.queryForList(query, filter, String.class);
        if (products.isEmpty()) {
            return null;
        } else {
            return products.get(0);
        }
    }

    public ResourceDTO getResource(Long document, String product, BigDecimal conversion, String additionalCode) {
        boolean useAdditionalCode = org.apache.commons.lang3.StringUtils.isNotEmpty(additionalCode);
        String query = positionResourcesHelper.getResourceQuery(document, false, useAdditionalCode);
        Map<String, Object> filter = new HashMap<>();
        filter.put("product", product);
        filter.put("conversion", conversion);
        filter.put("context", document);
        if (useAdditionalCode) {
            filter.put("additionalCode", additionalCode);
        }
        List<ResourceDTO> batches = jdbcTemplate.query(query, filter, new BeanPropertyRowMapper(ResourceDTO.class));
        if (batches.isEmpty() && useAdditionalCode) {
            query = positionResourcesHelper.getResourceQuery(document, false, false);
            batches = jdbcTemplate.query(query, filter, new BeanPropertyRowMapper(ResourceDTO.class));
        }
        if (batches.isEmpty()) {
            return null;
        } else {
            return batches.get(0);
        }
    }

    public List<AbstractDTO> getResources(Long document, String q, String product, BigDecimal conversion,
            boolean useAdditionalCode, String additionalCode) {

        if (Strings.isNullOrEmpty(q) || Strings.isNullOrEmpty(product)) {
            return Lists.newArrayList();
        } else {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("query", '%' + q + '%');
            paramMap.put("product", product);
            paramMap.put("conversion", conversion);
            paramMap.put("context", document);
            if (useAdditionalCode) {
                paramMap.put("additionalCode", additionalCode);
            }
            String query = positionResourcesHelper.getResourceQuery(document, true, useAdditionalCode);
            return jdbcTemplate.query(query, paramMap, new BeanPropertyRowMapper(ResourceDTO.class));

        }
    }

    public DataResponse getResourcesResponse(Long document, String q, String product, BigDecimal conversion, String additionalCode) {
        if (Strings.isNullOrEmpty(product)) {
            return new DataResponse(Lists.newArrayList(), 0);
        }
        boolean useAdditionalCode = org.apache.commons.lang3.StringUtils.isNotEmpty(additionalCode);
        String query = '%' + q + '%';
        List<AbstractDTO> entities = getResources(document, query, product, conversion, useAdditionalCode, additionalCode);

        if(entities.isEmpty() && useAdditionalCode){
            useAdditionalCode = false;
            entities = getResources(document, query, product, conversion, false, additionalCode);
        }
        String preparedQuery = positionResourcesHelper.getResourceQuery(document, true, useAdditionalCode);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("product", product);
        paramMap.put("conversion", conversion);
        paramMap.put("context", document);
        if (useAdditionalCode) {
            paramMap.put("additionalCode", additionalCode);
        }


        return dataProvider.getDataResponse(query, preparedQuery, entities, paramMap);
    }

    public ResourceDTO getResourceByNumber(String resource) {
        String query = "select r.*, sl.number as storageLocation, pn.number as palletNumber, ac.code as additionalCode \n"
                + "FROM materialflowresources_resource r \n"
                + "LEFT JOIN materialflowresources_storagelocation sl on sl.id = storageLocation_id \n"
                + "LEFT JOIN basic_additionalcode ac on ac.id = additionalcode_id \n"
                + "LEFT JOIN basic_palletnumber pn on pn.id = palletnumber_id WHERE r.number = :resource";
        Map<String, Object> filter = new HashMap<>();
        filter.put("resource", resource);
        List<ResourceDTO> batches = jdbcTemplate.query(query, filter, new BeanPropertyRowMapper(ResourceDTO.class));
        if (batches.isEmpty()) {
            return null;
        } else {
            return batches.get(0);
        }
    }

}
