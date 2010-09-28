package com.qcadoo.mes.core.view.elements.grid;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.model.FieldDefinition;
import com.qcadoo.mes.core.utils.ExpressionUtil;

/**
 * Columns defines one column on grid. It can be a one-field column or composite column.
 * 
 * Expression is JavaScript command used for formatting the column's cell value. TODO masz
 * 
 * Aggregation is displayed under the column. It aggregate all values for given column, from all pages. Aggregation is available
 * only for column which one-field column field column with aggregable type - FieldType#isAggregable().
 * 
 * Width is % of grid width for presentation given column
 * 
 * Method {@link ColumnDefinition#getValue(Entity)} returns value of the column for given entity.
 * 
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldDefinition
 * @apiviz.has com.qcadoo.mes.core.data.definition.ColumnAggregationMode
 */
public final class ColumnDefinition {

    private final String name;

    private final List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

    private ColumnAggregationMode aggregationMode;

    private String expression;

    private Integer width;

    public ColumnDefinition(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }

    public void addField(final FieldDefinition field) {
        this.fields.add(field);
    }

    public ColumnAggregationMode getAggregationMode() {
        return aggregationMode;
    }

    public void setAggregationMode(final ColumnAggregationMode aggregationMode) {
        this.aggregationMode = aggregationMode;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(final String expression) {
        this.expression = expression;
    }

    public String getValue(final Entity entity) {
        return ExpressionUtil.getValue(entity, this);
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(final Integer width) {
        this.width = width;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(aggregationMode).append(expression).append(fields).append(name).append(width)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ColumnDefinition)) {
            return false;
        }
        ColumnDefinition other = (ColumnDefinition) obj;
        return new EqualsBuilder().append(aggregationMode, other.aggregationMode).append(expression, other.expression)
                .append(name, other.name).append(width, other.width).append(fields, other.fields).isEquals();
    }

}
