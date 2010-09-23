package com.qcadoo.mes.core.internal.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.model.FieldDefinition;
import com.qcadoo.mes.core.view.elements.grid.ColumnDefinition;

public final class ExpressionUtil {

    private ExpressionUtil() {
    }

    public static String getValue(final Entity entity, final ColumnDefinition columnDefinition) {
        String value = null;
        if (StringUtils.isEmpty(columnDefinition.getExpression())) {
            value = getValueWithoutExpression(entity, columnDefinition);
        } else {
            value = getValueWithExpression(entity, columnDefinition);
        }
        if (StringUtils.isEmpty(value) || "null".equals(value)) {
            return null;
        } else {
            return value;
        }
    }

    private static String getValueWithExpression(final Entity entity, final ColumnDefinition columnDefinition) {
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(columnDefinition.getExpression());
        EvaluationContext context = new StandardEvaluationContext();

        for (FieldDefinition fieldDefinition : columnDefinition.getFields()) {
            Object value = entity.getField(fieldDefinition.getName());
            if (value instanceof Entity) {
                Map<String, Object> values = ((Entity) value).getFields();
                values.put("id", ((Entity) value).getId());
                context.setVariable(fieldDefinition.getName(), values);
            } else {
                context.setVariable(fieldDefinition.getName(), value);
            }
        }

        return String.valueOf(exp.getValue(context));
    }

    private static String getValueWithoutExpression(final Entity entity, final ColumnDefinition columnDefinition) {
        if (columnDefinition.getFields().size() == 1) {
            return columnDefinition.getFields().get(0).getValue(entity.getField(columnDefinition.getFields().get(0).getName()));
        } else {
            List<String> values = new ArrayList<String>();
            for (FieldDefinition fieldDefinition : columnDefinition.getFields()) {
                values.add(fieldDefinition.getValue(entity.getField(fieldDefinition.getName())));
            }
            return StringUtils.join(values, ", ");
        }
    }

}
