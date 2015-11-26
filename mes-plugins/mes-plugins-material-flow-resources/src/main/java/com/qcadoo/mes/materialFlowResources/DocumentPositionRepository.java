package com.qcadoo.mes.materialFlowResources;

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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentPositionRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DictionaryService dictionaryService;

    public List<DocumentPositionVO> findAll(final Long documentId, final String sidx, final String sord) {
        String query = "SELECT * FROM materialflowresources_position WHERE document_id = :documentId ORDER BY " + sidx + " " + sord;

        List<DocumentPositionVO> list = jdbcTemplate.query(query, Collections.singletonMap("documentId", documentId), new DocumentPositionMapper());

        return list;
    }

    public void delete(Long id) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM materialflowresources_position WHERE id = :id ");
        jdbcTemplate.update(queryBuilder.toString(), Collections.singletonMap("id", id));
    }

    public void create(DocumentPositionVO documentPositionVO) {
        Map<String, Object> params = new DocumentPositionMapper().mapVoToParams(documentPositionVO);

        String keys = params.keySet().stream().collect(Collectors.joining(", "));
        String values = params.keySet().stream().map(key -> {
            return ":" + key;
        }).collect(Collectors.joining(", "));

        String query = String.format("INSERT INTO materialflowresources_position (%s) VALUES (%s) RETURNING id", keys, values);

        jdbcTemplate.queryForObject(query, params, Long.class);
    }

    public void update(Long id, DocumentPositionVO documentPositionVO) {
        Map<String, Object> params = new DocumentPositionMapper().mapVoToParams(documentPositionVO);

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
}
