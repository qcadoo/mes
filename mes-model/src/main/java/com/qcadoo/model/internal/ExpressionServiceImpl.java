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

package com.qcadoo.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

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

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.localization.TranslationService;
import com.qcadoo.model.api.types.BelongsToType;
import com.qcadoo.model.api.types.FieldType;

/**
 * Helper class that contains methods to evaluate expression value.
 */
@Component
public final class ExpressionServiceImpl implements ExpressionService {

    private static final Logger LOG = LoggerFactory.getLogger(ExpressionServiceImpl.class);

    public static ExpressionService instance = null;

    @Autowired
    private TranslationService translationService;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public static ExpressionService getInstance() {
        return instance;
    }

    @Override
    public String getValue(final Entity entity, final List<FieldDefinition> fieldDefinitions, final Locale locale) {
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

        if (StringUtils.isEmpty(value) || "null".equals(value)) {
            return null;
        } else {
            return value;
        }
    }

    @Override
    public String getValue(final Entity entity, final String expression, final Locale locale) {
        if (StringUtils.isEmpty(expression) || "null".equals(expression)) {
            LOG.warn("Calculating empty expressions");
            return null;
        }

        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(translate(expression, locale));
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

        if (StringUtils.isEmpty(value) || "null".equals(value)) {
            return null;
        } else {
            return value;
        }
    }

    private String translate(final String expression, final Locale locale) {
        if (locale == null) {
            return expression;
        }

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

    private Map<String, Object> getValuesForEntity(final Entity entity, final Locale locale, final int level) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> values = new HashMap<String, Object>();
        values.put("id", entity.getId());

        if (level == 0) {
            values.putAll(entity.getFields());
            return values;
        }

        for (Map.Entry<String, Object> entry : entity.getFields().entrySet()) {
            if (entry.getValue() instanceof Collection) {
                values.put(entry.getKey(), entry.getValue());
            } else {
                FieldType type = entity.getDataDefinition().getField(entry.getKey()).getType();
                if (type instanceof BelongsToType) {
                    Entity belongsToEntity = getBelongsToEntity(entry.getValue(), (BelongsToType) type);
                    values.put(entry.getKey(), getValuesForEntity(belongsToEntity, locale, level - 1));
                } else {
                    String value = entry.getValue() != null ? type.toString(entry.getValue(), locale) : null;
                    values.put(entry.getKey(), value);
                }
            }
        }

        return values;
    }

    private Entity getBelongsToEntity(final Object value, final BelongsToType type) {
        if (value instanceof Entity) {
            return (Entity) value;
        } else if (value instanceof Number || value instanceof String) {
            return type.getDataDefinition().get(Long.parseLong(value.toString()));
        } else {
            return null;
        }
    }

}
