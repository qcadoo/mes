package com.qcadoo.mes.core.data.definition;

import java.util.List;
import java.util.Set;

import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.Restriction;

/**
 * Grid defines structure used for listing entities. It contains the list of field that can be used for restrictions and the list
 * of columns. It also have default order and default restrictions.
 * 
 * Searchable fields must have searchable type - {@link FieldType#isSearchable()}.
 * 
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldDefinition
 * @apiviz.owns com.qcadoo.mes.core.data.definition.ColumnDefinition
 * @apiviz.uses com.qcadoo.mes.core.data.search.Order
 * @apiviz.uses com.qcadoo.mes.core.data.search.Restriction
 */
public interface GridDefinition {

    String getName();

    Set<FieldDefinition> getSearchableFields();

    List<ColumnDefinition> getColumns();

    Order getDefaultOrder();

    Set<Restriction> getDefaultRestrictions();

}
