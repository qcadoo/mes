package com.qcadoo.mes.core.data.view.elements;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.TranslationService;
import com.qcadoo.mes.core.data.internal.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.internal.types.HasManyType;
import com.qcadoo.mes.core.data.model.DataDefinition;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.view.AbstractComponent;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewValue;
import com.qcadoo.mes.core.data.view.elements.grid.ColumnDefinition;
import com.qcadoo.mes.core.data.view.elements.grid.ListData;

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
public final class GridComponent extends AbstractComponent<ListData> {

    private final Set<FieldDefinition> searchableFields = new HashSet<FieldDefinition>();

    private final Set<FieldDefinition> orderableFields = new HashSet<FieldDefinition>();

    private final List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();

    private String correspondingViewName;

    private boolean header = true;

    public GridComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public String getType() {
        return "grid";
    }

    public final Set<FieldDefinition> getSearchableFields() {
        return searchableFields;
    }

    public final void addSearchableField(final FieldDefinition searchableField) {
        this.searchableFields.add(searchableField);
    }

    public final Set<FieldDefinition> getOrderableFields() {
        return orderableFields;
    }

    public final void addOrderableField(final FieldDefinition orderableField) {
        this.orderableFields.add(orderableField);
    }

    public final List<ColumnDefinition> getColumns() {
        return columns;
    }

    public final void addColumn(final ColumnDefinition column) {
        this.columns.add(column);
    }

    public final String getCorrespondingViewName() {
        return correspondingViewName;
    }

    public final void setCorrespondingViewName(final String correspondingViewName) {
        this.correspondingViewName = correspondingViewName;
    }

    @Override
    public void addComponentOptions(final Map<String, Object> viewOptions) {
        viewOptions.put("correspondingViewName", correspondingViewName);
        viewOptions.put("columns", getColumnsForOptions());
        viewOptions.put("fields", getFieldsForOptions(getDataDefinition().getFields()));
    }

    @Override
    public ViewValue<ListData> castComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        JSONObject value = viewObject.getJSONObject("value");

        if (value != null) {
            String selectedEntityId = value.getString("selectedEntityId");

            if (selectedEntityId != null && !"null".equals(selectedEntityId)) {
                Entity selectedEntity = getDataDefinition().get(Long.parseLong(selectedEntityId));
                selectedEntities.put(getPath(), selectedEntity);
            }
        }

        return new ViewValue<ListData>();
    }

    @Override
    public ViewValue<ListData> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<ListData> viewEntity, final Set<String> pathsToUpdate) {
        if (getSourceFieldPath() != null || getFieldPath() != null) {
            if (entity == null) {
                return new ViewValue<ListData>(new ListData(0, Collections.<Entity> emptyList()));
            }
            DataDefinition gridDataDefinition = getParentContainer().getDataDefinition();
            if (getSourceComponent() != null) {
                gridDataDefinition = getSourceComponent().getDataDefinition();
            }
            HasManyType hasManyType = null;
            if (getFieldPath() != null) {
                hasManyType = getHasManyType(gridDataDefinition, getFieldPath());
            } else {
                hasManyType = getHasManyType(gridDataDefinition, getSourceFieldPath());
            }
            checkState(hasManyType.getDataDefinition().getName().equals(getDataDefinition().getName()),
                    "Grid and hasMany relation have different data definitions");
            SearchCriteriaBuilder searchCriteriaBuilder = getDataDefinition().find();
            searchCriteriaBuilder = searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(
                    getDataDefinition().getField(hasManyType.getFieldName()), entity.getId()));
            SearchResult rs = searchCriteriaBuilder.list();
            return new ViewValue<ListData>(generateListData(rs));
        } else {
            SearchResult rs = getDataDefinition().find().list();
            return new ViewValue<ListData>(generateListData(rs));
        }
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        if (header) {
            String messageCode = getViewName() + "." + getPath() + ".header";
            translationsMap.put(messageCode, translationService.translate(messageCode, locale));
        }
        for (ColumnDefinition column : columns) {
            List<String> messageCodes = new LinkedList<String>();
            messageCodes.add(getViewName() + "." + getPath() + ".column." + column.getName());
            messageCodes.add(getDataDefinition().getName() + "." + column.getName() + ".label");
            translationsMap.put(messageCodes.get(0), translationService.translate(messageCodes, locale));
        }

    }

    private Object getFieldsForOptions(final Map<String, FieldDefinition> fields) {
        return new ArrayList<String>(fields.keySet());
    }

    private List<String> getColumnsForOptions() {
        List<String> columnsForOptions = new ArrayList<String>();
        for (ColumnDefinition column : columns) {
            columnsForOptions.add(column.getName());
        }
        return columnsForOptions;
    }

    private HasManyType getHasManyType(final DataDefinition dataDefinition, final String fieldPath) {
        checkState(!fieldPath.matches("\\."), "Grid doesn't support sequential path");
        FieldDefinition fieldDefinition = dataDefinition.getField(fieldPath);
        if (fieldDefinition != null && fieldDefinition.getType() instanceof HasManyType) {
            return (HasManyType) fieldDefinition.getType();
        } else {
            throw new IllegalStateException("Grid data definition cannot be found");
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

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

}
