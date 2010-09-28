package com.qcadoo.mes.core.view.elements;

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

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.api.TranslationService;
import com.qcadoo.mes.core.model.DataDefinition;
import com.qcadoo.mes.core.model.FieldDefinition;
import com.qcadoo.mes.core.search.Restrictions;
import com.qcadoo.mes.core.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.search.SearchResult;
import com.qcadoo.mes.core.types.HasManyType;
import com.qcadoo.mes.core.view.AbstractComponent;
import com.qcadoo.mes.core.view.ComponentOption;
import com.qcadoo.mes.core.view.ContainerComponent;
import com.qcadoo.mes.core.view.ViewValue;
import com.qcadoo.mes.core.view.elements.grid.ColumnAggregationMode;
import com.qcadoo.mes.core.view.elements.grid.ColumnDefinition;
import com.qcadoo.mes.core.view.elements.grid.ListData;

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

    private boolean multiselect = false;

    private boolean paginable = false;

    public GridComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public String getType() {
        return "grid";
    }

    public Set<FieldDefinition> getSearchableFields() {
        return searchableFields;
    }

    public Set<FieldDefinition> getOrderableFields() {
        return orderableFields;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public String getCorrespondingViewName() {
        return correspondingViewName;
    }

    @Override
    public void initializeComponent() {
        for (ComponentOption option : getRawOptions()) {
            if ("header".equals(option.getName())) {
                header = Boolean.parseBoolean(option.getValue());
            } else if ("correspondingView".equals(option.getName())) {
                correspondingViewName = option.getValue();
            } else if ("paginable".equals(option.getName())) {
                paginable = Boolean.parseBoolean(option.getValue());
            } else if ("multiselect".equals(option.getName())) {
                multiselect = Boolean.parseBoolean(option.getValue());
            } else if ("height".equals(option.getName())) {
                addOption("height", Integer.parseInt(option.getValue()));
            } else if ("searchable".equals(option.getName())) {
                for (FieldDefinition field : getFields(option.getValue())) {
                    searchableFields.add(field);
                }
            } else if ("orderable".equals(option.getName())) {
                for (FieldDefinition field : getFields(option.getValue())) {
                    orderableFields.add(field);
                }
            } else if ("column".equals(option.getName())) {
                ColumnDefinition columnDefinition = new ColumnDefinition(option.getAtrributeValue("name"));
                for (FieldDefinition field : getFields(option.getAtrributeValue("fields"))) {
                    columnDefinition.addField(field);
                }
                columnDefinition.setExpression(option.getAtrributeValue("expression"));
                String width = option.getAtrributeValue("width");
                if (width != null) {
                    columnDefinition.setWidth(Integer.valueOf(width));
                }
                if ("sum".equals(option.getAtrributeValue("aggregation"))) {
                    columnDefinition.setAggregationMode(ColumnAggregationMode.SUM);
                } else {
                    columnDefinition.setAggregationMode(ColumnAggregationMode.NONE);
                }
                columns.add(columnDefinition);
            }
        }

        addOption("multiselect", multiselect);
        addOption("correspondingView", correspondingViewName);
        addOption("paginable", paginable);
        addOption("header", header);
        addOption("columns", getColumnsForOptions());
        addOption("fields", getFieldsForOptions(getDataDefinition().getFields()));
        addOption("sortable", !orderableFields.isEmpty());
        addOption("filter", !searchableFields.isEmpty());
    }

    private Set<FieldDefinition> getFields(final String fields) {
        Set<FieldDefinition> set = new HashSet<FieldDefinition>();
        for (String field : fields.split("\\s*,\\s*")) {
            set.add(getDataDefinition().getField(field));
        }
        return set;
    }

    @Override
    public ViewValue<ListData> castComponentValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        JSONObject value = viewObject.getJSONObject("value");

        ListData listData = new ListData();

        if (value != null) {
            String selectedEntityId = value.getString("selectedEntityId");

            if (selectedEntityId != null && !"null".equals(selectedEntityId)) {
                Entity selectedEntity = getDataDefinition().get(Long.parseLong(selectedEntityId));
                selectedEntities.put(getPath(), selectedEntity);
                listData.setSelectedEntityId(Long.parseLong(selectedEntityId));
            }
        }

        return new ViewValue<ListData>(listData);
    }

    @Override
    public ViewValue<ListData> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<ListData> viewValue, final Set<String> pathsToUpdate) {
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
                    getDataDefinition().getField(hasManyType.getJoinFieldName()), entity.getId()));
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
            messageCodes.add(translationService.getEntityFieldMessageCode(getDataDefinition(), column.getName()));
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

}
