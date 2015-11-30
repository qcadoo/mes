package com.qcadoo.mes.materialFlowResources;

import com.google.common.base.Strings;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.mappers.DocumentPositionMapper;
import com.qcadoo.model.api.DictionaryService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.BeanMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentPositionRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DictionaryService dictionaryService;

    public List<DocumentPositionVO> findAll(final Long documentId, final String sidx, final String sord) {
        String query = "SELECT position.*, product.number as product_number, additionalcode.code as additionalcode_code, palletnumber.number as palletnumber_number\n"
                + "	FROM materialflowresources_position position\n"
                + "	left join basic_product product on (position.product_id = product.id)\n"
                + "	left join basic_additionalcode additionalcode on (position.additionalcode_id = additionalcode.id)\n"
                + "	left join basic_palletnumber palletnumber on (position.palletnumber_id = palletnumber.id) WHERE position.document_id = :documentId ORDER BY " + sidx + " " + sord;

        List<DocumentPositionVO> list = jdbcTemplate.query(query, Collections.singletonMap("documentId", documentId), new DocumentPositionMapper());

        return list;
    }

    public void delete(Long id) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM materialflowresources_position WHERE id = :id ");
        jdbcTemplate.update(queryBuilder.toString(), Collections.singletonMap("id", id));
    }

    public void create(DocumentPositionVO documentPositionVO) {
        Map<String, Object> params = tryMapDocumentPositionVOToParams(documentPositionVO);

        String keys = params.keySet().stream().collect(Collectors.joining(", "));
        String values = params.keySet().stream().map(key -> {
            return ":" + key;
        }).collect(Collectors.joining(", "));

        String query = String.format("INSERT INTO materialflowresources_position (%s) VALUES (%s) RETURNING id", keys, values);

        jdbcTemplate.queryForObject(query, params, Long.class);
    }

    public void update(Long id, DocumentPositionVO documentPositionVO) {
        Map<String, Object> params = tryMapDocumentPositionVOToParams(documentPositionVO);

        String set = params.keySet().stream().map(key -> {
            return key + "=:" + key;
        }).collect(Collectors.joining(", "));
        String query = String.format("UPDATE materialflowresources_position SET %s WHERE id = :id ", set);

        jdbcTemplate.update(query, params);
    }

    public List<Map<String, String>> getTypes() {
        List<Map<String, String>> types = new ArrayList<>();

        for (DocumentType documentType : DocumentType.values()) {
            Map<String, String> type = new HashMap<>();
            type.put("value", documentType.name());
            type.put("key", documentType.getStringValue());
            types.add(type);
        }

        return types;
    }

    public List<Map<String, String>> getUnits() {
        return dictionaryService.getKeys("units").stream().map(unit -> {
            Map<String, String> type = new HashMap<>();
            type.put("value", unit);
            type.put("key", unit);

            return type;
        }).collect(Collectors.toList());
    }

    public List<Map<String, String>> getTypeOfPallets() {
        return dictionaryService.getKeys("typeOfPallet").stream().map(unit -> {
            Map<String, String> type = new HashMap<>();
            type.put("value", unit);
            type.put("key", unit);

            return type;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> tryMapDocumentPositionVOToParams(DocumentPositionVO vo) {
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

//        params.put("storagelocation_id", vo.getStorage_location_id());
        return params;
    }

    private Long tryGetProductIdByNumber(String productNumber) {
        if(Strings.isNullOrEmpty(productNumber)){
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
        if(Strings.isNullOrEmpty(additionalCode)){
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
        if(Strings.isNullOrEmpty(palletNumber)){
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
}
