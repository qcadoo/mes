package com.qcadoo.mes.materialFlowResources;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.AttributeValueType;
import com.qcadoo.model.api.BigDecimalUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
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
        String getAttribute = "SELECT at.* FROM basic_attribute at WHERE at.number = :attr";

        for (Map.Entry<String, Object> entry : attribiutesValue.entrySet()) {
            if (StringUtils.isNotEmpty((String) entry.getValue())) {
                Map<String, Object> attDefinitionsParameters = Maps.newHashMap();
                attDefinitionsParameters.put("attr", entry.getKey());
                AttributeDto attDefinition = jdbcTemplate.queryForObject(getAttribute, attDefinitionsParameters,
                        new BeanPropertyRowMapper<AttributeDto>(AttributeDto.class));
                if (Objects.nonNull(attDefinition)) {

                    Map<String, Object> params = Maps.newHashMap();
                    params.put("positionId", positionId);
                    params.put("attr", entry.getKey());
                    if (attDefinition.getValueType().equals(AttributeValueType.NUMERIC.getStringValue())) {
                        Either<Exception, Optional<BigDecimal>> eitherNumber = BigDecimalUtils.tryParseAndIgnoreSeparator(
                                (String) entry.getValue(), LocaleContextHolder.getLocale());
                        if (eitherNumber.isRight() && eitherNumber.getRight().isPresent()) {
                            int scale = attDefinition.getPrecision();

                            params.put("value", BigDecimalUtils.toString(eitherNumber.getRight().get(), scale));
                        } else {
                            params.put("value", entry.getValue());
                        }
                    } else {
                        params.put("value", entry.getValue());
                    }
                    jdbcTemplate.update(insertQueryBuilder.toString(), params);
                }
            }
        }

    }

}
