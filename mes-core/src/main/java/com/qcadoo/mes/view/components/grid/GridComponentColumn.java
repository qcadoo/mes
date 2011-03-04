/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.view.components.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.util.StringUtils;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.internal.utils.ExpressionUtils;

public final class GridComponentColumn {

    private static final int DEFAULT_COLUMN_WIDTH = 100;

    private final String name;

    private final List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

    private String expression;

    private Integer width = DEFAULT_COLUMN_WIDTH;

    private boolean link;

    private boolean hidden;

    public GridComponentColumn(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setWidth(final Integer width) {
        this.width = width;
    }

    public Integer getWidth() {
        return width;
    }

    public boolean isLink() {
        return link;
    }

    public void setLink(final boolean link) {
        this.link = link;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getAlign() {
        if (fields.size() == 1 && Number.class.isAssignableFrom(fields.get(0).getType().getType())) {
            return "right";
        } else {
            return "left";
        }
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public void setExpression(final String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public void addField(final FieldDefinition field) {
        fields.add(field);
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }

    public String getValue(final Entity entity, final Locale locale) {
        if (StringUtils.hasText(expression)) {
            return ExpressionUtils.getValue(entity, expression, locale);
        } else {
            String value = ExpressionUtils.getValue(entity, fields, locale);
            if (value != null) {
                value = value.replaceAll("\n", " ");
            }
            return value;
        }
    }

}
