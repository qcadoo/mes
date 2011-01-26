/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.utils;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;

/**
 * Helper class that contains methods to evaluate expression value.
 * 
 */

@Component
public final class ExpressionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ExpressionUtil.class);

    private static TranslationService translationService;

    private static DataDefinitionService dataDefinitionService;

    public ExpressionUtil() {
    }

    @Autowired
    public void setTranslationService(final TranslationService translationService) {
        ExpressionUtil.setStaticTranslationService(translationService);
    }

    @Autowired
    public void setDataDefinitionService(final DataDefinitionService dataDefinitionService) {
        ExpressionUtil.setStaticDataDefinitionService(dataDefinitionService);
    }

    public static void setStaticDataDefinitionService(final DataDefinitionService dataDefinitionService) {
        ExpressionUtil.dataDefinitionService = dataDefinitionService;
    }

    private static void setStaticTranslationService(final TranslationService translationService) {
        ExpressionUtil.translationService = translationService;
    }

    /**
     * Generates text to display in grid cell. If columnDefinition has expression - uses it, otherwise result is value of field
     * (or comma separated fields values when columDefinition has more than one field). Returns null when generated value is null.
     * 
     * @param entity
     * @param columnDefinition
     * @param locale
     * @return text to display in grid cell
     */
    public static String getValue(final Entity entity, final List<FieldDefinition> fieldDefinitions, final Locale locale) {
        String value = getValueWithoutExpression(entity, fieldDefinitions, locale);

        if (StringUtils.isEmpty(value) || "null".equals(value)) {
            return null;
        } else {
            return value;
        }
    }

    /**
     * Evaluate expression value using entity fields values. Returns null when generated value is null.
     * 
     * @param entity
     * @param expression
     * @return result of expression evaluation
     */
    public static String getValue(final Entity entity, final String expression, final Locale locale) {
        checkState(!isEmpty(expression), "Expression must be defined");

        String value = getValueWithExpression(entity, translate(expression, locale), locale);

        if (StringUtils.isEmpty(value) || "null".equals(value)) {
            return null;
        } else {
            return value;
        }
    }

    private static String translate(final String expression, final Locale locale) {
        Matcher m = Pattern.compile("\\@([a-zA-Z0-9\\.]+)").matcher(expression);
        StringBuffer sb = new StringBuffer();

        int i = 0;

        while (m.find()) {
            sb.append(expression.substring(i, m.start()));
            sb.append(translationService.translate(m.group(1), locale));
            i = m.end();
        }

        if (i == 0) {
            return expression;
        }

        sb.append(expression.substring(i, expression.length()));

        return sb.toString();
    }

    private static String getValueWithExpression(final Entity entity, final String expression, final Locale locale) {
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(expression);
        EvaluationContext context = new StandardEvaluationContext();

        if (entity != null) {
            Map<String, Object> values = getValuesForEntity(entity, locale, 2);

            for (Map.Entry<String, Object> entry : values.entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
        }

        String value = String.valueOf(exp.getValue(context));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Calculating value of expression \"" + expression + "\" for " + entity + " : " + value);
        }

        return value;
    }

    private static Map<String, Object> getValuesForEntity(final Entity entity, final Locale locale, int level) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("id", entity.getId());

        if (level == 0) {
            values.putAll(entity.getFields());
            return values;
        }

        for (Map.Entry<String, Object> entry : entity.getFields().entrySet()) {
            DataDefinition dataDefinition = dataDefinitionService.get(entity.getPluginIdentifier(), entity.getName());

            if (entry.getValue() instanceof Entity) {
                values.put(entry.getKey(), getValuesForEntity((Entity) entry.getValue(), locale, --level));
            } else if (entry.getValue() instanceof Collection) {
                values.put(entry.getKey(), entry.getValue());
            } else {
                String value = entry.getValue() != null ? dataDefinition.getField(entry.getKey()).getType()
                        .toString(entry.getValue(), locale) : null;
                values.put(entry.getKey(), value);
            }
        }

        return values;
    }

    private static String getValueWithoutExpression(final Entity entity, final List<FieldDefinition> fieldDefinitions,
            final Locale locale) {
        String value = null;

        if (fieldDefinitions.size() == 1) {
            FieldDefinition field = fieldDefinitions.get(0);
            value = field.getValue(entity.getField(field.getName()), locale);
        } else {
            List<String> values = new ArrayList<String>();
            for (FieldDefinition fieldDefinition : fieldDefinitions) {
                values.add(fieldDefinition.getValue(entity.getField(fieldDefinition.getName()), locale));
            }
            value = StringUtils.join(values, ", ");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Calculating value of fields " + fieldDefinitions + " for " + entity + " : " + value);
        }

        return value;
    }
}
