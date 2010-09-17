package com.qcadoo.mes.core.data.definition.view.elements.grid;

import java.util.ArrayList;
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
import com.qcadoo.mes.core.data.definition.view.ContainerDefinition;
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

    private Set<DataFieldDefinition> orderableFields;

    private List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();

    private String correspondingViewName;

    private final DataAccessService dataAccessService;

    private final DataDefinitionService dataDefinitionService;

    public GridDefinition(final String name, final ContainerDefinition parentContainer, final String fieldPath,
            final String sourceFieldPath, final DataAccessService dataAccessService,
            final DataDefinitionService dataDefinitionService) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
        this.dataAccessService = dataAccessService;
        this.dataDefinitionService = dataDefinitionService;
    }

    @Override
    public String getType() {
        return "grid";
    }

    public Set<DataFieldDefinition> getSearchableFields() {
        return searchableFields;
    }

    public void setSearchableFields(final Set<DataFieldDefinition> searchableFields) {
        this.searchableFields = searchableFields;
    }

    public Set<DataFieldDefinition> getOrderableFields() {
        return orderableFields;
    }

    public void setOrderableFields(final Set<DataFieldDefinition> orderableFields) {
        this.orderableFields = orderableFields;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public void setColumns(final List<ColumnDefinition> columns) {
        this.columns = columns;
    }

    public String getCorrespondingViewName() {
        return correspondingViewName;
    }

    public void setCorrespondingViewName(final String correspondingViewName) {
        this.correspondingViewName = correspondingViewName;
    }

    @Override
    public Map<String, Object> getOptions() {
        Map<String, Object> viewOptions = super.getOptions();
        viewOptions.put("correspondingViewName", correspondingViewName);
        viewOptions.put("columns", getColumnsForOptions());
        viewOptions.put("fields", getFieldsForOptions(getDataDefinition().getFields()));
        return viewOptions;
    }

    private Object getFieldsForOptions(final Map<String, DataFieldDefinition> fields) {
        return new ArrayList<String>(fields.keySet());
    }

    private List<String> getColumnsForOptions() {
        List<String> columnsForOptions = new ArrayList<String>();
        for (ColumnDefinition column : columns) {
            columnsForOptions.add(column.getName());
        }
        return columnsForOptions;
    }

    @Override
    public Object getValue(final Entity entity, final Map<String, Object> selectableValues, final Object viewEntity) {
        if (getSourceFieldPath() != null) { // TODO
            if (getSourceFieldPath().charAt(0) == '#') {
                return null;
            }
            DataFieldDefinition corespondingField = getDataDefinition().getField(getSourceFieldPath());
            HasManyType corespondingType = (HasManyType) corespondingField.getType();
            DataDefinition corespondingDD = dataDefinitionService.get(corespondingType.getEntityName());
            SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity(corespondingDD);
            searchCriteriaBuilder = searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(
                    corespondingDD.getField(corespondingType.getFieldName()), entity.getId()));
            SearchCriteria searchCriteria = searchCriteriaBuilder.build();
            SearchResult rs = dataAccessService.find(searchCriteria);
            return generateListData(rs);
        } else {
            SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity(getDataDefinition());
            SearchCriteria searchCriteria = searchCriteriaBuilder.build();
            SearchResult rs = dataAccessService.find(searchCriteria);
            return generateListData(rs);
        }
    }

    private ListData generateListData(final SearchResult rs) {
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
