package com.qcadoo.mes.view.components.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.util.StringUtils;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.utils.ExpressionUtil;

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
            return ExpressionUtil.getValue(entity, expression, locale);
        } else {
            String value = ExpressionUtil.getValue(entity, fields, locale);
            if (value != null) {
                value = value.replaceAll("\n", " ");
            }
            return value;
        }
    }

}
