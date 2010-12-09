/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.viewold.components;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Order;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.internal.BooleanType;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.viewold.AbstractComponent;
import com.qcadoo.mes.viewold.ContainerComponent;
import com.qcadoo.mes.viewold.ListData;
import com.qcadoo.mes.viewold.SelectableComponent;
import com.qcadoo.mes.viewold.ViewValue;
import com.qcadoo.mes.viewold.components.grid.ColumnAggregationMode;
import com.qcadoo.mes.viewold.components.grid.ColumnDefinition;

/**
 * Represents grid.<br/>
 * <br/>
 * XML declaration: <br/>
 * 
 * <pre>
 *      {@code <component type="grid" name="{identifier of component}" source="{source of component content}">}
 * </pre>
 * 
 * XML options:
 * <ul>
 * <li>column - integer - definition of column (can be more than one). Suboptions:
 * <ul>
 * <li>name - String - name of column</li>
 * <li>fields - String - list of column fields</li>
 * <li>expression - String - expression which describes how to display value in cell</li>
 * <li>width - integer - width of column (actual width will be calculate dynamically, this options define proportion between
 * columns widths)</li>
 * <li>link - [true|false] - true when values of this column should be links to entity details view</li>
 * </ul>
 * </li>
 * <li>correspondingView - {pluginName}/{viewName} - defines content entity details view</li>
 * <li>height - integer - height of component</li>
 * <li>width - integer - width of component</li>
 * <li>fullScreen - [true|false] - true when grid should expand to full screen</li>
 * <li>orderable - comma separated list of column names - list of column names which sorting is enabled</li>
 * <li>
 * searchable - comma separated list of column names - list of column names which searching is enabled</li>
 * <li>paginable - [true|false] - true when grid should have paging</li>
 * <li>creatable - [true|false] - true when grid should have 'create' button</li>
 * <li>deletable - [true|false] - true when grid should have 'delete' button</li>
 * </ul>
 */
public final class GridComponent extends AbstractComponent<ListData> implements SelectableComponent {

    private final Set<FieldDefinition> searchableFields = new HashSet<FieldDefinition>();

    private Set<String> orderableColumns = new HashSet<String>();

    private final Map<String, ColumnDefinition> columns = new LinkedHashMap<String, ColumnDefinition>();

    private String correspondingView;

    private boolean header = true;

    private boolean multiselect = false;

    private boolean paginable = false;

    private boolean deletable = false;

    private boolean creatable = false;

    public GridComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath, final TranslationService translationService) {
        super(name, parentContainer, fieldPath, sourceFieldPath, translationService);
    }

    @Override
    public String getType() {
        return "grid";
    }

    public Collection<ColumnDefinition> getColumns() {
        return columns.values();
    }

    @Override
    public void initializeComponent() {
        for (ComponentOption option : getRawOptions()) {
            if ("header".equals(option.getType())) {
                header = Boolean.parseBoolean(option.getValue());
            } else if ("correspondingView".equals(option.getType())) {
                correspondingView = option.getValue();
            } else if ("paginable".equals(option.getType())) {
                paginable = Boolean.parseBoolean(option.getValue());
            } else if ("multiselect".equals(option.getType())) {
                multiselect = Boolean.parseBoolean(option.getValue());
            } else if ("creatable".equals(option.getType())) {
                creatable = Boolean.parseBoolean(option.getValue());
            } else if ("deletable".equals(option.getType())) {
                deletable = Boolean.parseBoolean(option.getValue());
            } else if ("height".equals(option.getType())) {
                addOption("height", Integer.parseInt(option.getValue()));
            } else if ("width".equals(option.getType())) {
                addOption("width", Integer.parseInt(option.getValue()));
            } else if ("fullScreen".equals(option.getType())) {
                addOption("fullScreen", Boolean.parseBoolean(option.getValue()));
            } else if ("isLookup".equals(option.getType())) {
                addOption("isLookup", Boolean.parseBoolean(option.getValue()));
            } else if ("searchable".equals(option.getType())) {
                for (FieldDefinition field : getFields(option.getValue())) {
                    searchableFields.add(field);
                }
            } else if ("orderable".equals(option.getType())) {
                orderableColumns = getColumnNames(option.getValue());
            } else if ("column".equals(option.getType())) {
                ColumnDefinition columnDefinition = new ColumnDefinition(option.getAtrributeValue("name"),
                        getTranslationService());
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
                if (option.getAtrributeValue("link") != null) {
                    columnDefinition.setLink(Boolean.parseBoolean(option.getAtrributeValue("link")));
                }
                if (option.getAtrributeValue("hidden") != null) {
                    columnDefinition.setHidden(Boolean.parseBoolean(option.getAtrributeValue("hidden")));
                }
                columns.put(columnDefinition.getName(), columnDefinition);
            }
        }

        addOption("multiselect", multiselect);
        addOption("correspondingViewName", correspondingView);
        addOption("paginable", paginable);
        addOption("header", header);
        addOption("columns", getColumnsForOptions());
        addOption("fields", getFieldsForOptions(getDataDefinition().getFields()));
        addOption("sortable", !orderableColumns.isEmpty());
        addOption("sortColumns", orderableColumns);
        addOption("filter", !searchableFields.isEmpty());
        addOption("canDelete", deletable);
        addOption("canNew", creatable);
        addOption("prioritizable", getDataDefinition().isPrioritizable());

    }

    private Set<String> getColumnNames(final String columns) {
        Set<String> set = new HashSet<String>();
        for (String column : columns.split("\\s*,\\s*")) {
            set.add(column);
        }
        return set;
    }

    private Set<FieldDefinition> getFields(final String fields) {
        Set<FieldDefinition> set = new HashSet<FieldDefinition>();
        if (fields != null) {
            for (String field : fields.split("\\s*,\\s*")) {
                set.add(getDataDefinition().getField(field));
            }
        }
        return set;
    }

    @Override
    public ViewValue<ListData> castComponentValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        ListData listData = new ListData();

        JSONObject value = viewObject.getJSONObject("value");

        if (value != null) {

            if (!value.isNull("paging")) {
                if (!value.getJSONObject("paging").isNull("first")) {
                    listData.setFirstResult(value.getJSONObject("paging").getInt("first"));
                }
                if (!value.getJSONObject("paging").isNull("max")) {
                    listData.setMaxResults(value.getJSONObject("paging").getInt("max"));
                }
            }

            if (!value.isNull("sort")) {
                if (!value.getJSONObject("sort").isNull("column")) {
                    listData.setOrderColumn(value.getJSONObject("sort").getString("column"));
                }
                if (!value.getJSONObject("sort").isNull("order") && "asc".equals(value.getJSONObject("sort").getString("order"))) {
                    listData.setOrderAsc(true);
                } else {
                    listData.setOrderAsc(false);
                }
            }

            if (!value.isNull("searchEnabled")) {
                listData.setSearchEnabled(value.getBoolean("searchEnabled"));
            }

            if (!value.isNull("filters")) {
                JSONArray filters = value.getJSONArray("filters");
                for (int i = 0; i < filters.length(); i++) {
                    listData.addFilter(((JSONObject) filters.get(i)).getString("column"),
                            ((JSONObject) filters.get(i)).getString("value"));
                }
            }

            if (!value.isNull("selectedEntityId")) {
                String selectedEntityId = value.getString("selectedEntityId");

                if (selectedEntityId != null && !"null".equals(selectedEntityId)) {
                    Entity selectedEntity = getDataDefinition().get(Long.parseLong(selectedEntityId));
                    selectedEntities.put(getPath(), selectedEntity);
                    listData.setSelectedEntityId(Long.parseLong(selectedEntityId));
                }
            }
        }

        return new ViewValue<ListData>(listData);
    }

    @Override
    public ViewValue<ListData> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<ListData> viewValue, final Set<String> pathsToUpdate,
            final Locale locale) {

        Entity relatedEntity = entity;
        String joinFieldName = null;
        Long belongsToEntityId = null;
        SearchCriteriaBuilder searchCriteriaBuilder = null;

        if (getSourceFieldPath() != null || getFieldPath() != null) {
            if (getSourceComponent() != null && relatedEntity == null) {
                relatedEntity = selectedEntities.get(getSourceComponent().getPath());
            }

            if (parentEntity != null && relatedEntity == null && getSourceFieldPath() == null) {
                relatedEntity = parentEntity;
            }

            if (relatedEntity == null || relatedEntity.getId() == null) {
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
            searchCriteriaBuilder = getDataDefinition().find();
            searchCriteriaBuilder = searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(
                    getDataDefinition().getField(hasManyType.getJoinFieldName()), relatedEntity.getId()));
            joinFieldName = hasManyType.getJoinFieldName();
            belongsToEntityId = relatedEntity.getId();
        } else {
            searchCriteriaBuilder = getDataDefinition().find();
        }

        if (viewValue != null) {
            addRestrictionsOrderAndPaging(searchCriteriaBuilder, viewValue.getValue());

        }

        ListData listData = generateListData(searchCriteriaBuilder.list(), joinFieldName, belongsToEntityId, locale);

        if (viewValue != null) {
            copyRestrictionsOrderPagingAndSelectedEntityId(listData, viewValue.getValue());
        }

        return new ViewValue<ListData>(listData);
    }

    private void addRestrictionsOrderAndPaging(final SearchCriteriaBuilder searchCriteriaBuilder, final ListData listData) {
        if (listData.getFirstResult() != null) {
            searchCriteriaBuilder.withFirstResult(listData.getFirstResult());
        }
        if (listData.getMaxResults() != null) {
            searchCriteriaBuilder.withMaxResults(listData.getMaxResults());
        }
        if (listData.getOrderColumn() != null) {
            addOrder(searchCriteriaBuilder, listData, columns.get(listData.getOrderColumn()),
                    getFieldByColumnName(listData.getOrderColumn()));
        }
        if (listData.isSearchEnabled()) {
            for (Map<String, String> filter : listData.getFilters()) {
                addRestriction(searchCriteriaBuilder, filter, columns.get(filter.get("column")),
                        getFieldByColumnName(filter.get("column")));
            }
        }
    }

    private void addRestriction(final SearchCriteriaBuilder searchCriteriaBuilder, final Map<String, String> filter,
            final ColumnDefinition column, final FieldDefinition field) {
        if (field == null || column == null) {
            return;
        }

        String value = filter.get("value");

        if (StringUtils.hasText(column.getExpression())) {
            Matcher matcher = Pattern.compile("#(\\w+)\\['(\\w+)'\\]").matcher(column.getExpression());
            if (matcher.matches()) {
                searchCriteriaBuilder.restrictedWith(Restrictions.eq(matcher.group(1) + "." + matcher.group(2), value + "*"));
            }
        } else if (field.getType().isSearchable()) {
            if (field.getType() instanceof BooleanType) {
                searchCriteriaBuilder.restrictedWith(Restrictions.eq(field, value));
            } else {
                searchCriteriaBuilder.restrictedWith(Restrictions.eq(field, value + "*"));
            }
        }
    }

    private void addOrder(final SearchCriteriaBuilder searchCriteriaBuilder, final ListData listData,
            final ColumnDefinition column, final FieldDefinition field) {
        if (field == null || column == null) {
            return;
        }

        if (StringUtils.hasText(column.getExpression())) {
            Matcher matcher = Pattern.compile("#(\\w+)\\['(\\w+)'\\]").matcher(column.getExpression());
            if (matcher.matches()) {
                Order order = null;

                if (listData.isOrderAsc()) {
                    order = Order.asc(matcher.group(1) + "." + matcher.group(2));
                } else {
                    order = Order.desc(matcher.group(1) + "." + matcher.group(2));
                }

                // searchCriteriaBuilder.orderBy(order);
            }
        } else if (field.getType().isOrderable()) {
            Order order = null;

            if (listData.isOrderAsc()) {
                order = Order.asc(field.getName());
            } else {
                order = Order.desc(field.getName());
            }

            // searchCriteriaBuilder.orderBy(order);
        }
    }

    private FieldDefinition getFieldByColumnName(final String column) {
        List<FieldDefinition> fields = columns.get(column).getFields();

        if (fields.size() != 1) {
            return null;
        } else {
            return fields.get(0);
        }
    }

    private void copyRestrictionsOrderPagingAndSelectedEntityId(final ListData listData, final ListData oldListData) {
        listData.setFirstResult(oldListData.getFirstResult());
        listData.setMaxResults(oldListData.getMaxResults());
        listData.setOrderColumn(oldListData.getOrderColumn());
        listData.setOrderAsc(oldListData.isOrderAsc());
        listData.setSearchEnabled(oldListData.isSearchEnabled());
        listData.setSelectedEntityId(oldListData.getSelectedEntityId());
        for (Map<String, String> filter : oldListData.getFilters()) {
            listData.addFilter(filter.get("column"), filter.get("value"));
        }
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final Locale locale) {
        String messagePath = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath();
        translationsMap.put(messagePath + ".header", getTranslationService().translate(messagePath + ".header", locale));
        for (ColumnDefinition column : columns.values()) {
            List<String> messageCodes = new LinkedList<String>();
            messageCodes.add(getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath()
                    + ".column." + column.getName());
            messageCodes.add(getTranslationService().getEntityFieldBaseMessageCode(getDataDefinition(), column.getName())
                    + ".label");
            translationsMap.put(messageCodes.get(0), getTranslationService().translate(messageCodes, locale));

            column.addColumnTranslations(translationsMap, locale);
        }

        String[] gridMessages = new String[] { "addFilter", "removeFilter", "perPage", "outOfPages", "new", "delete", "up",
                "down", "noRowSelectedError", "confirmDeleteMessage", "wrongSearchCharacterError" };

        for (String gridMessage : gridMessages) {
            translationsMap.put(
                    messagePath + "." + gridMessage,
                    getTranslationService().translate(
                            Arrays.asList(new String[] { messagePath + "." + gridMessage, "core.grid." + gridMessage }), locale));
        }
    }

    private Object getFieldsForOptions(final Map<String, FieldDefinition> fields) {
        return new ArrayList<String>(fields.keySet());
    }

    private List<ColumnDefinition> getColumnsForOptions() {
        return new ArrayList<ColumnDefinition>(columns.values());
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

    private ListData generateListData(final SearchResult rs, final String contextFieldName, final Long contextId,
            final Locale locale) {
        List<Entity> entities = rs.getEntities();
        List<Entity> gridEntities = new LinkedList<Entity>();

        for (Entity entity : entities) {
            Entity gridEntity = new DefaultEntity(entity.getPluginIdentifier(), entity.getName(), entity.getId());
            for (ColumnDefinition column : columns.values()) {
                gridEntity.setField(column.getName(), column.getValue(entity, locale));
            }
            gridEntities.add(gridEntity);
        }

        int totalNumberOfEntities = rs.getTotalNumberOfEntities();
        return new ListData(totalNumberOfEntities, gridEntities, contextFieldName, contextId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long getSelectedEntityId(final ViewValue<Long> viewValue) {
        ViewValue<ListData> value = (ViewValue<ListData>) lookupViewValue(viewValue);
        return value.getValue().getSelectedEntityId();
    }

}
