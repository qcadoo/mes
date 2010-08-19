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

    public GridDefinition(final String name) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columns == null) ? 0 : columns.hashCode());
        result = prime * result + ((defaultOrder == null) ? 0 : defaultOrder.hashCode());
        result = prime * result + ((defaultRestrictions == null) ? 0 : defaultRestrictions.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((searchableFields == null) ? 0 : searchableFields.hashCode());
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
        GridDefinition other = (GridDefinition) obj;
        if (columns == null) {
            if (other.columns != null)
                return false;
        } else if (!columns.equals(other.columns))
            return false;
        if (defaultOrder == null) {
            if (other.defaultOrder != null)
                return false;
        } else if (!defaultOrder.equals(other.defaultOrder))
            return false;
        if (defaultRestrictions == null) {
            if (other.defaultRestrictions != null)
                return false;
        } else if (!defaultRestrictions.equals(other.defaultRestrictions))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (searchableFields == null) {
            if (other.searchableFields != null)
                return false;
        } else if (!searchableFields.equals(other.searchableFields))
            return false;
        return true;
    }
}
