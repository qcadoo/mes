package com.qcadoo.mes.core.data.definition.grid;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;

/**
 * Grid defines structure used for listing entities. It contains the list of field that can be used for restrictions and the list
 * of columns. It also have default order and default restrictions.
 * 
 * Searchable fields must have searchable type - FieldType#isSearchable().
 * 
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldDefinition
 * @apiviz.owns com.qcadoo.mes.core.data.definition.ColumnDefinition
 * @apiviz.uses com.qcadoo.mes.core.data.search.Order
 * @apiviz.uses com.qcadoo.mes.core.data.search.Restriction
 */
public final class GridDefinition extends ComponentDefinition {

    private Set<DataFieldDefinition> searchableFields;

    private List<ColumnDefinition> columns;

    public GridDefinition(final String name, final DataDefinition dataDefinition) {
        super(name, dataDefinition);
    }

    @Override
    public int getType() {
        return ComponentDefinition.TYPE_GRID;
    }

    public Set<DataFieldDefinition> getSearchableFields() {
        return searchableFields;
    }

    public void setSearchableFields(final Set<DataFieldDefinition> searchableFields) {
        this.searchableFields = searchableFields;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public void setColumns(final List<ColumnDefinition> columns) {
        this.columns = columns;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 37).append(columns).append(searchableFields).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GridDefinition)) {
            return false;
        }
        GridDefinition other = (GridDefinition) obj;
        return new EqualsBuilder().append(columns, other.columns).append(searchableFields, other.searchableFields).isEquals();
    }
}
