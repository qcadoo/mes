package com.qcadoo.mes.utils;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.internal.BooleanType;
import com.qcadoo.mes.view.components.grid.ColumnDefinition;

@Component
public final class ExpressionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ExpressionUtil.class);

    private static TranslationService translationService;

    public ExpressionUtil() {
    }

    @Autowired
    public void setTranslationService(final TranslationService translationService) {
        ExpressionUtil.translationService = translationService;
    }

    public static String getValue(final Entity entity, final ColumnDefinition columnDefinition, final Locale locale) {
        String value = null;
        if (StringUtils.isEmpty(columnDefinition.getExpression())) {
            value = getValueWithoutExpression(entity, columnDefinition, locale);
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
        checkState(!isEmpty(expression), "Expression must be defined");

        String value = getValueWithExpression(entity, expression);

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

        if (entity != null) {
            context.setVariable("id", entity.getId());
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
        }

        String value = String.valueOf(exp.getValue(context));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Calculating value of expression \"" + expression + "\" for " + entity + " : " + value);
        }

        return value;
    }

    private static String getValueWithoutExpression(final Entity entity, final ColumnDefinition columnDefinition,
            final Locale locale) {
        String value = null;

        if (columnDefinition.getFields().size() == 1) {
            value = columnDefinition.getFields().get(0).getValue(entity.getField(columnDefinition.getFields().get(0).getName()));
            if (columnDefinition.getFields().get(0).getType() instanceof BooleanType) {
                if ("0".equals(value)) {
                    value = translationService.translate("commons.false", locale);
                } else {
                    value = translationService.translate("commons.true", locale);
                }
            }
        } else {
            List<String> values = new ArrayList<String>();
            for (FieldDefinition fieldDefinition : columnDefinition.getFields()) {
                values.add(fieldDefinition.getValue(entity.getField(fieldDefinition.getName())));
            }
            value = StringUtils.join(values, ", ");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Calculating value of column " + columnDefinition.getName() + " for " + entity + " : " + value);
        }

        return value;
    }
}
