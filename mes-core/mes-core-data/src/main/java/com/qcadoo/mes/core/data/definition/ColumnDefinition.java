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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

}
