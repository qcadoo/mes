package com.qcadoo.mes.materialFlowResources;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.controllers.GridResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DocumentPositionService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DocumentPositionValidator validator;

    public GridResponse<DocumentPositionDTO> findAll(final Long documentId, final String _sidx, final String _sord, int page, int perPage, DocumentPositionDTO position) {
        String sidx = _sidx != null ? _sidx.toLowerCase() : "";
        String sord = _sord != null ? _sord.toLowerCase() : "";

        Preconditions.checkState(Arrays.asList("asc", "desc", "").contains(sord));
        Preconditions.checkState(Arrays.asList(DocumentPositionDTO.class.getDeclaredFields()).stream().map(Field::getName).map(String::toLowerCase).collect(Collectors.toList()).contains(sidx));

        String query = "SELECT %s FROM ( SELECT p.*, p.document_id as document, product.number as product, product.unit, additionalcode.code as additionalcode, "
                + "palletnumber.number as palletnumber, location.number as storagelocation\n"
                + "	FROM materialflowresources_position p\n"
                + "	left join basic_product product on (p.product_id = product.id)\n"
                + "	left join basic_additionalcode additionalcode on (p.additionalcode_id = additionalcode.id)\n"
                + "	left join basic_palletnumber palletnumber on (p.palletnumber_id = palletnumber.id)\n"
                + "	left join materialflowresources_storagelocation location on (p.storagelocation_id = location.id) WHERE p.document_id = :documentId %s) q ";

        query += addQueryWhereForPosition(position);

        Map<String, Object> parameters = getParametersForPosition(position);
        parameters.put("documentId", documentId);

        String queryCount = String.format(query, "COUNT(*)", "");
        String queryRecords = String.format(query, "*", "ORDER BY " + sidx + " " + sord) + String.format(" LIMIT %d OFFSET %d", perPage, perPage * (page - 1));

        Integer records = jdbcTemplate.queryForObject(queryCount, parameters, Long.class).intValue();
        List<DocumentPositionDTO> list = jdbcTemplate.query(queryRecords, parameters, new BeanPropertyRowMapper(DocumentPositionDTO.class));

        return new GridResponse<>(page, Double.valueOf(Math.ceil((1.0 * records) / perPage)).intValue(), records, list);
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

    public List<StorageLocationDTO> getStorageLocations(String q) {
        if (Strings.isNullOrEmpty(q)) {
            return Lists.newArrayList();

        } else {
            String query = "SELECT id, number from materialflowresources_storagelocation WHERE number ilike :q LIMIT 15;";
            return jdbcTemplate.query(query, Collections.singletonMap("q", '%' + q + '%'), new BeanPropertyRowMapper(StorageLocationDTO.class));
        }
    }

    public Map<String, Object> getGridConfig(Long documentId) {
        try {
            String query = "select * from materialflowresources_documentpositionparametersitem";
            List<Map<String, Object>> items = jdbcTemplate.queryForList(query, Collections.EMPTY_MAP);

            Map<String, Object> config = new HashMap<>();
            config.put("readOnly", isGridReadOnly(documentId));
            
            for(Map<String, Object> item : items){
                config.put("show"+item.get("name").toString().toLowerCase(), item.get("checked"));
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

        List<DocumentPositionDTO> list = jdbcTemplate.query(query, Collections.singletonMap("documentId", documentId), new BeanPropertyRowMapper(DocumentPositionDTO.class));
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

    private String addQueryWhereForPosition(DocumentPositionDTO position) {
        List<String> items = new ArrayList<>();

        if (position != null) {
            for (Field field : DocumentPositionDTO.class.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(position);
                    if (value != null) {
                        if (value instanceof Number) {
                            items.add(String.format("%s = :%s", field.getName(), field.getName()));

                        } else if (value instanceof Date) {
                            items.add(String.format("%s = :%s", field.getName(), field.getName()));

                        } else if (value instanceof String) {
                            items.add(String.format("lower(%s) like lower(:%s)", field.getName(), field.getName()));
                        }
                    }

                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        String where = "";

        if (!items.isEmpty()) {
            where = " WHERE " + items.stream().collect(Collectors.joining(" AND "));
        }

        return where;
    }

    private Map<String, Object> getParametersForPosition(DocumentPositionDTO position) {
        Map<String, Object> parameters = new HashMap<>();

        if (position != null) {
            for (Field field : DocumentPositionDTO.class.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(position);
                    if (value != null) {
                        if (value instanceof String) {
                            parameters.put(field.getName(), "%" + value + "%");

                        } else {
                            parameters.put(field.getName(), value);
                        }
                    }

                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return parameters;
    }
}
