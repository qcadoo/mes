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
public final class GridDefinition {

    private String name;

    private Set<FieldDefinition> searchableFields;

    private List<ColumnDefinition> columns;

    private Order defaultOrder;

    private Set<Restriction> defaultRestrictions;

    public GridDefinition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<FieldDefinition> getSearchableFields() {
        return searchableFields;
    }

    public void setSearchableFields(final Set<FieldDefinition> searchableFields) {
        this.searchableFields = searchableFields;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public void setColumns(final List<ColumnDefinition> columns) {
        this.columns = columns;
    }

    public Order getDefaultOrder() {
        return defaultOrder;
    }

    public void setDefaultOrder(final Order defaultOrder) {
        this.defaultOrder = defaultOrder;
    }

    public Set<Restriction> getDefaultRestrictions() {
        return defaultRestrictions;
    }

    public void setDefaultRestrictions(final Set<Restriction> defaultRestrictions) {
        this.defaultRestrictions = defaultRestrictions;
    }

}
