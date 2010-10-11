package com.qcadoo.mes.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.view.components.grid.ColumnDefinition;

public final class ExpressionUtil {

    private ExpressionUtil() {
    }

    public static String getValue(final Entity entity, final ColumnDefinition columnDefinition) {
        String value = null;
        if (StringUtils.isEmpty(columnDefinition.getExpression())) {
            value = getValueWithoutExpression(entity, columnDefinition);
        } else {
            value = getValueWithExpression(entity, columnDefinition.getExpression());
        }
        if (StringUtils.isEmpty(value) || "null".equals(value)) {
            return null;
        } else {
            return value;
        }
    }

    public static String getValue(final Entity entity, final String expression) {
        String value = null;
        if (StringUtils.isEmpty(expression)) {
            throw new IllegalStateException("Must have expression");
        } else {
            value = getValueWithExpression(entity, expression);
        }
        if (StringUtils.isEmpty(value) || "null".equals(value)) {
            return null;
        } else {
            return value;
        }
    }

    private static String getValueWithExpression(final Entity entity, final String expression) {
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(expression);
        EvaluationContext context = new StandardEvaluationContext();

        for (String fieldName : entity.getFields().keySet()) {
            Object value = entity.getField(fieldName);
            if (value instanceof Entity) {
                Map<String, Object> values = ((Entity) value).getFields();
                values.put("id", ((Entity) value).getId());
                context.setVariable(fieldName, values);
            } else {
                context.setVariable(fieldName, value);
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
