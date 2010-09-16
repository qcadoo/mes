package com.qcadoo.mes.core.data.definition.view.elements.grid;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;
import com.qcadoo.mes.core.data.internal.types.HasManyType;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.search.SearchResult;

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

    private String correspondingViewName;

    private final DataDefinition dataDefinition;

    private String header;

    private final DataDefinitionService dataDefinitionService;

    public GridDefinition(final String name, DataDefinition dataDefinition, final String dataSource,
            final DataDefinitionService dataDefinitionService) {
        super(name, dataSource);
        this.dataDefinition = dataDefinition;
        this.dataDefinitionService = dataDefinitionService;
    }

    public DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    @Override
    public int getType() {
        return ComponentDefinition.TYPE_ELEMENT_GRID;
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

    public String getCorrespondingViewName() {
        return correspondingViewName;
    }

    public void setCorrespondingViewName(String correspondingViewName) {
        this.correspondingViewName = correspondingViewName;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    @Override
    public ListData getValue(DataDefinition dataDefinition, DataAccessService dataAccessService, Entity entity) {
        if (getDataSource() != null) {
            if (getDataSource().charAt(0) == '#') {
                return null;
            }
            DataFieldDefinition corespondingField = dataDefinition.getField(getDataSource());
            HasManyType corespondingType = (HasManyType) corespondingField.getType();
            DataDefinition corespondingDD = dataDefinitionService.get(corespondingType.getEntityName());
            SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity(corespondingDD);
            searchCriteriaBuilder = searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(
                    corespondingDD.getField(corespondingType.getFieldName()), entity.getId()));
            SearchCriteria searchCriteria = searchCriteriaBuilder.build();
            SearchResult rs = dataAccessService.find(searchCriteria);
            return generateListData(rs);
        } else {
            SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity(dataDefinition);
            SearchCriteria searchCriteria = searchCriteriaBuilder.build();
            SearchResult rs = dataAccessService.find(searchCriteria);
            return generateListData(rs);
        }
    }

    @Override
    public Object getUpdateValues(Map<String, String> updateComponents) {
        return null;
    }

    private ListData generateListData(SearchResult rs) {
        List<Entity> entities = rs.getEntities();
        List<Entity> gridEntities = new LinkedList<Entity>();

        for (Entity entity : entities) {
            Entity gridEntity = new Entity(entity.getId());
            for (ColumnDefinition column : columns) {
                gridEntity.setField(column.getName(), column.getValue(entity));
            }
            gridEntities.add(gridEntity);
        }

        int totalNumberOfEntities = rs.getTotalNumberOfEntities();
        return new ListData(totalNumberOfEntities, gridEntities);
    }
}
