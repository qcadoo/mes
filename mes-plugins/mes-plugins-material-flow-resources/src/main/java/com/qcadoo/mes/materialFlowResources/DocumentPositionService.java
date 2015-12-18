package com.qcadoo.mes.materialFlowResources;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.mappers.DocumentPositionMapper;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentPositionService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DocumentPositionValidator validator;

    public List<DocumentPositionDTO> findAll(final Long documentId, final String _sidx, final String _sord) {
        String sidx = _sidx != null ? _sidx.toLowerCase() : "";
        String sord = _sord != null ? _sord.toLowerCase() : "";

        Preconditions.checkState(Arrays.asList("asc", "desc", "").contains(sord));
        Preconditions.checkState(Arrays.asList(DocumentPositionDTO.class.getDeclaredFields()).stream().map(Field::getName).collect(Collectors.toList()).contains(sidx));

        String query = "SELECT p.*, product.number as product_number, product.unit as product_unit, additionalcode.code as additionalcode_code, palletnumber.number as palletnumber_number, location.number as storagelocation_number\n"
                + "	FROM materialflowresources_position p\n"
                + "	left join basic_product product on (p.product_id = product.id)\n"
                + "	left join basic_additionalcode additionalcode on (p.additionalcode_id = additionalcode.id)\n"
                + "	left join basic_palletnumber palletnumber on (p.palletnumber_id = palletnumber.id)\n"
                + "	left join materialflowresources_storagelocation location on (p.storagelocation_id = location.id) WHERE p.document_id = :documentId ORDER BY " + sidx + " " + sord;

        List<DocumentPositionDTO> list = jdbcTemplate.query(query, Collections.singletonMap("documentId", documentId), new DocumentPositionMapper());

        return list;
    }

    public void delete(Long id) {
        validator.validateBeforeDelete(id);

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM materialflowresources_position WHERE id = :id ");
        jdbcTemplate.update(queryBuilder.toString(), Collections.singletonMap("id", id));
    }

    public void create(DocumentPositionDTO documentPositionVO) {
        validator.validateBeforeCreate(documentPositionVO);

        Map<String, Object> params = tryMapDocumentPositionVOToParams(documentPositionVO);
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
        validator.validateBeforeUpdate(documentPositionVO);

        Map<String, Object> params = tryMapDocumentPositionVOToParams(documentPositionVO);

        String set = params.keySet().stream().map(key -> {
            return key + "=:" + key;
        }).collect(Collectors.joining(", "));
        String query = String.format("UPDATE materialflowresources_position SET %s WHERE id = :id ", set);

        jdbcTemplate.update(query, params);
    }

    private Map<String, Object> tryMapDocumentPositionVOToParams(DocumentPositionDTO vo) {
        Map<String, Object> params = new HashMap<>();

        params.put("id", vo.getId());
        params.put("product_id", tryGetProductIdByNumber(vo.getProduct()));
        params.put("additionalcode_id", tryGetAdditionalCodeIdByCode(vo.getAdditional_code()));
        params.put("quantity", vo.getQuantity());
        params.put("givenquantity", vo.getGivenquantity());
        params.put("givenunit", vo.getGivenunit());
        params.put("conversion", vo.getConversion());
        params.put("expirationdate", vo.getExpirationdate());
        params.put("palletnumber_id", tryGetPalletNumberIdByNumber(vo.getPallet()));
        params.put("typeofpallet", vo.getType_of_pallet());
        params.put("storagelocation_id", tryGetStorageLocationIdByNumber(vo.getStorage_location()));
        params.put("document_id", vo.getDocument());
        params.put("productiondate", vo.getProductiondate());
        params.put("price", vo.getPrice());
        params.put("batch", vo.getBatch());

        return params;
    }

    private Long tryGetProductIdByNumber(String productNumber) {
        if (Strings.isNullOrEmpty(productNumber)) {
            return null;
        }

        try {
            Long productId = jdbcTemplate.queryForObject("SELECT product.id FROM basic_product product WHERE product.number = :number", Collections.singletonMap("number", productNumber), Long.class);

            return productId;

        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException(String.format("Nie znaleziono takiego produktu: '%s'.", productNumber));
        }
    }

    private Long tryGetAdditionalCodeIdByCode(String additionalCode) {
        if (Strings.isNullOrEmpty(additionalCode)) {
            return null;
        }

        try {
            Long additionalCodeId = jdbcTemplate.queryForObject("SELECT additionalcode.id FROM basic_additionalcode additionalcode WHERE additionalcode.code = :code",
                    Collections.singletonMap("code", additionalCode), Long.class);

            return additionalCodeId;

        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException(String.format("Nie znaleziono takiego dodatkowego kodu: '%s'.", additionalCode));
        }
    }

    private Long tryGetPalletNumberIdByNumber(String palletNumber) {
        if (Strings.isNullOrEmpty(palletNumber)) {
            return null;
        }

        try {
            Long palletNumberId = jdbcTemplate.queryForObject("SELECT palletnumber.id FROM basic_palletnumber palletnumber WHERE palletnumber.number = :number",
                    Collections.singletonMap("number", palletNumber), Long.class);

            return palletNumberId;

        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException(String.format("Nie znaleziono takiego numeru palety: '%s'.", palletNumber));
        }
    }

    private Long tryGetStorageLocationIdByNumber(String storageLocationNumber) {
        if (Strings.isNullOrEmpty(storageLocationNumber)) {
            return null;
        }

        try {
            Long storageLocationId = jdbcTemplate.queryForObject("SELECT storagelocation.id FROM materialflowresources_storagelocation storagelocation WHERE storagelocation.number = :number",
                    Collections.singletonMap("number", storageLocationNumber), Long.class);

            return storageLocationId;

        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException(String.format("Nie znaleziono takiego miejsca składowania: '%s'.", storageLocationNumber));
        }
    }

    public List<StorageLocationDTO> getStorageLocations(String q) {
        if (Strings.isNullOrEmpty(q)) {
            return Lists.newArrayList();

        } else {
            String query = "SELECT id, number from materialflowresources_storagelocation WHERE number ilike :q LIMIT 15;";
            return jdbcTemplate.query(query, Collections.singletonMap("q", '%' + q + '%'), new BeanPropertyRowMapper(StorageLocationDTO.class));
        }
    }

    public Map<String, Object> getGridConfig() {
        try {
            String query = "select * from materialflowresources_documentpositionparameters";
            return jdbcTemplate.queryForMap(query, Collections.EMPTY_MAP);

        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_MAP;
        }
    }

    public Map<String, Object> unitsOfProduct(String productNumber) {
        try {
            String query = "SELECT id, unit, additionalunit FROM basic_product WHERE number = :number";

            Map<String, Object> units = jdbcTemplate.queryForMap(query, Collections.singletonMap("number", productNumber));

            if (units.get("additionalunit") == null || units.get("additionalunit").toString().isEmpty()) {
                units.put("additionalunit", units.get("unit"));
            }

            List<Map<String, Object>> availableAdditionalUnits = getAvailableAdditionalUnitsByProduct(Long.valueOf(units.get("id").toString()));
            
            Map<String, Object> type = new HashMap<>();
            type.put("value", units.get("unit"));
            type.put("key", units.get("unit"));
            type.put("conversion", BigDecimal.valueOf(1));
            availableAdditionalUnits.add(type);
            units.put("available_additionalunits", availableAdditionalUnits);

            units.put("conversion", calculateConversion(availableAdditionalUnits, units.get("additionalunit").toString()));
            
            return units;

        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_MAP;
        }
    }

    private List<Map<String, Object>> getAvailableAdditionalUnitsByProduct(Long productId) {
        String query = "select unitto, quantityto/quantityfrom as conversion from qcadoomodel_unitconversionitem  where product_id = :id";
        List<Map<String, Object>> availableUnits = jdbcTemplate.queryForList(query, Collections.singletonMap("id", productId));

        List<Map<String, Object>> result = availableUnits.stream().map(entry -> {
            Map<String, Object> type = new HashMap<>();
            type.put("value", entry.get("unitto"));
            type.put("key", entry.get("unitto"));
            type.put("conversion", entry.get("conversion"));

            return type;
        }).collect(Collectors.toList());

        return result;
    }

    private BigDecimal calculateConversion(List<Map<String, Object>> availableAdditionalUnits, String additionalUnit) {
        BigDecimal conversion = null;
        
        Optional<Map<String, Object>> maybeEntry = availableAdditionalUnits.stream().filter(entry -> {return entry.get("key").equals(additionalUnit);}).findAny();
        if(maybeEntry.isPresent()){
            conversion = (BigDecimal) maybeEntry.get().get("conversion");
        }
        
        return conversion;
    }
}
