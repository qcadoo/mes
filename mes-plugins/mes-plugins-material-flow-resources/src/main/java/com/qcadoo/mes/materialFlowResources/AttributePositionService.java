package com.qcadoo.mes.materialFlowResources;

import com.google.common.collect.Maps;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AttributePositionService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void createOrUpdateAttributePositionValues(boolean creatingPosition, Long positionId,
            Map<String, Object> attribiutesValue) {

        if (!creatingPosition) {
            String deleteQuery = "DELETE FROM materialflowresources_positionattributevalue WHERE position_id = :positionId";
            Map<String, Object> params = Maps.newHashMap();
            params.put("positionId", positionId);
            jdbcTemplate.update(deleteQuery, params);
        }

        StringBuilder insertQueryBuilder = new StringBuilder();
        insertQueryBuilder
                .append("INSERT INTO materialflowresources_positionattributevalue(position_id, attribute_id, attributevalue_id, value) ");
        insertQueryBuilder.append("VALUES (:positionId, ");
        insertQueryBuilder.append("(SELECT at.id FROM basic_attribute at WHERE at.number = :attr), ");
        insertQueryBuilder
                .append("(SELECT av.id FROM basic_attributevalue av WHERE av.attribute_id = (SELECT at.id FROM basic_attribute at WHERE at.number = :attr) AND av.value = :value), ");
        insertQueryBuilder.append(":value)");

        for (Map.Entry<String, Object> entry : attribiutesValue.entrySet()) {
            if (StringUtils.isNotEmpty((String) entry.getValue())) {
                Map<String, Object> params = Maps.newHashMap();
                params.put("positionId", positionId);
                params.put("attr", entry.getKey());
                params.put("value", entry.getValue());
                jdbcTemplate.update(insertQueryBuilder.toString(), params);
            }
        }

    }

}
