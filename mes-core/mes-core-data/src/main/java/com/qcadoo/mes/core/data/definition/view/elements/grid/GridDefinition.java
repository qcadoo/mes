package com.qcadoo.mes.core.data.definition.view.elements.grid;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.definition.view.ComponentDefinition;
import com.qcadoo.mes.core.data.definition.view.ContainerDefinition;
import com.qcadoo.mes.core.data.definition.view.ViewEntity;
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
public final class GridDefinition extends ComponentDefinition<ListData> {

    private final Set<DataFieldDefinition> searchableFields = new HashSet<DataFieldDefinition>();

    private final Set<DataFieldDefinition> orderableFields = new HashSet<DataFieldDefinition>();

    private final List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();

    private String correspondingViewName;

    private final DataAccessService dataAccessService;

    public GridDefinition(final String name, final ContainerDefinition<?> parentContainer, final String fieldPath,
            final String sourceFieldPath, final DataAccessService dataAccessService) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
        this.dataAccessService = dataAccessService;
    }

    @Override
    public String getType() {
        return "grid";
    }

    public Set<DataFieldDefinition> getSearchableFields() {
        return searchableFields;
    }

    public void addSearchableField(final DataFieldDefinition searchableField) {
        this.searchableFields.add(searchableField);
    }

    public Set<DataFieldDefinition> getOrderableFields() {
        return orderableFields;
    }

    public void addOrderableField(final DataFieldDefinition orderableField) {
        this.orderableFields.add(orderableField);
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public void addColumn(final ColumnDefinition column) {
        this.columns.add(column);
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
    public ViewEntity<ListData> castValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ViewEntity<ListData> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewEntity<Object> globalViewEntity, final ViewEntity<ListData> viewEntity) {
        // TODO viewEntity

        if ((getSourceFieldPath() != null && getSourceComponent() != null) || getFieldPath() != null) {
            if (entity == null) {
                return new ViewEntity<ListData>(new ListData(0, Collections.<Entity> emptyList()));
            }
            HasManyType hasManyType = null;
            if (getFieldPath() != null) {
                hasManyType = getHasManyType(getParentContainer().getDataDefinition(), getFieldPath());
            } else {
                hasManyType = getHasManyType(getSourceComponent().getDataDefinition(), getSourceFieldPath());
            }
            checkState(hasManyType.getDataDefinition().getName().equals(getDataDefinition().getName()),
                    "Grid and hasMany relation have different data definitions");
            SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity(getDataDefinition());
            searchCriteriaBuilder = searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(
                    getDataDefinition().getField(hasManyType.getFieldName()), entity.getId()));
            SearchCriteria searchCriteria = searchCriteriaBuilder.build();
            SearchResult rs = dataAccessService.find(searchCriteria);
            return new ViewEntity<ListData>(generateListData(rs));
        } else {
            SearchCriteriaBuilder searchCriteriaBuilder = SearchCriteriaBuilder.forEntity(getDataDefinition());
            SearchCriteria searchCriteria = searchCriteriaBuilder.build();
            SearchResult rs = dataAccessService.find(searchCriteria);
            return new ViewEntity<ListData>(generateListData(rs));
        }
    }

    private HasManyType getHasManyType(final DataDefinition dataDefinition, final String fieldPath) {
        checkState(!fieldPath.matches("\\."), "Grid doesn't support sequential path");
        DataFieldDefinition fieldDefinition = dataDefinition.getField(fieldPath);
        checkNotNull((fieldDefinition != null && fieldDefinition.getType() instanceof HasManyType),
                "Grid data definition cannot be found");
        return (HasManyType) fieldDefinition.getType();
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
