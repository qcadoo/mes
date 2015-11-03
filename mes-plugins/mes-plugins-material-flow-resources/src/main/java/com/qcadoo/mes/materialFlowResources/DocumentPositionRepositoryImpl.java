package com.qcadoo.mes.materialFlowResources;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ProductVO;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;

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
public class DocumentPositionRepositoryImpl implements DocumentPositionRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> findAll(String sidx, String sord) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM materialflowresources_position ORDER BY " + sidx + " " + sord);
        // TODO pobranie tylko z konkretnego dokumentu
        List<Map<String, Object>> list = jdbcTemplate.queryForList(query.toString(), Collections.EMPTY_MAP);
        return list;

    }

    @Override
    public void delete(Long id) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM materialflowresources_position WHERE id = :id ");
        jdbcTemplate.update(queryBuilder.toString(), Collections.singletonMap("id", id));
    }

    @Override public void create(DocumentPositionVO documentPositionVO) {
        Map<String, Object> params = new HashMap<>(new BeanMap(documentPositionVO));
        params.remove("id");
        params.remove("class");

        StringBuilder queryBuilder = new StringBuilder("INSERT INTO materialflowresources_position( ");
        queryBuilder.append(params.keySet().stream().collect(Collectors.joining(", "))+") ");
        queryBuilder.append("VALUES ("+params.keySet().stream().map(key -> {return ":"+key;}).collect(Collectors.joining(", "))+") RETURNING id ");

        jdbcTemplate.queryForObject(queryBuilder.toString(), params, Long.class);
    }

    @Override public void update(Long id, DocumentPositionVO documentPositionVO) {
        Map<String, Object> params = new HashMap<>(new BeanMap(documentPositionVO));
        params.remove("class");

        jdbcTemplate
                .update("UPDATE materialflowresources_position SET " + params.keySet().stream().map(key -> {
                    return key + "=:" + key;
                }).collect(Collectors.joining(", ")) + " WHERE id = :id ", params);
    }


    public List<Map<String,String>> getTypes(){
        List<Map<String, String>> types = new ArrayList<>();

        for(DocumentType documentType : DocumentType.values()){
            Map<String, String> type = new HashMap<>();
            type.put("value", documentType.name());
            type.put("key", documentType.getStringValue());
            types.add(type);
        }

        return types;
    }

}
