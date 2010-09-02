package com.qcadoo.mes.core.data.internal.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.ColumnDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;

public final class ExpressionUtil {

    private ExpressionUtil() {
    }

    public static String getValue(final Entity entity, final ColumnDefinition columnDefinition) {
        String value = null;
        if (StringUtils.isEmpty(columnDefinition.getExpression())) {
            if (columnDefinition.getFields().size() == 1) {
                value = columnDefinition.getFields().get(0)
                        .getValue(entity.getField(columnDefinition.getFields().get(0).getName()));
            } else {
                List<String> values = new ArrayList<String>();
                for (FieldDefinition fieldDefinition : columnDefinition.getFields()) {
                    values.add(fieldDefinition.getValue(entity.getField(fieldDefinition.getName())));
                }
                value = StringUtils.join(values, ", ");
            }
        } else {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(columnDefinition.getExpression());
            EvaluationContext context = new StandardEvaluationContext(entity);
            value = String.valueOf(exp.getValue(context));
        }
        if (StringUtils.isEmpty(value) || "null".equals(value)) {
            return null;
        } else {
            return value;
        }
    }

}
