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
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldDefinition
 * @apiviz.has com.qcadoo.mes.core.data.definition.ColumnAggregationMode
 */
public interface ColumnDefinition {

    String getName();

    List<FieldDefinition> getFields();

    ColumnAggregationMode getAggregationMode();

    String getExpression();

}
