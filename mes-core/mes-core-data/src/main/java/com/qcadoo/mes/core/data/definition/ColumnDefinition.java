package com.qcadoo.mes.core.data.definition;

import java.util.List;

/**
 * Columns defines one column on grid. It can be a one-field column or composite column.
 * 
 * Expression is JavaScript command used for formatting the column's cell value. TODO masz
 * 
 * Aggregation is displayed under the column. It aggregate all values for given column, from all pages. Aggregation is available
 * only for column which one-field column field column with aggregable type - {@link FieldType#isAggregable()}.
 * 
 * Width is % of grid width for presentation given column
 * 
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldDefinition
 * @apiviz.has com.qcadoo.mes.core.data.definition.ColumnAggregationMode
 */
public final class ColumnDefinition {

    private String name;

    private List<FieldDefinition> fields;

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

    public void setFields(final List<FieldDefinition> fields) {
        this.fields = fields;
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

    public Integer getWidth() {
        return width;
    }

    public void setWidth(final Integer width) {
        this.width = width;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aggregationMode == null) ? 0 : aggregationMode.hashCode());
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
        result = prime * result + ((fields == null) ? 0 : fields.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((width == null) ? 0 : width.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ColumnDefinition other = (ColumnDefinition) obj;
        if (aggregationMode != other.aggregationMode)
            return false;
        if (expression == null) {
            if (other.expression != null)
                return false;
        } else if (!expression.equals(other.expression))
            return false;
        if (fields == null) {
            if (other.fields != null)
                return false;
        } else if (!fields.equals(other.fields))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (width == null) {
            if (other.width != null)
                return false;
        } else if (!width.equals(other.width))
            return false;
        return true;
    }

}
