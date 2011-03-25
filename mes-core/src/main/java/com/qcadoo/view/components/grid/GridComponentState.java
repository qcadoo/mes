/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.view.components.grid;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.Restriction;
import com.qcadoo.model.api.search.RestrictionOperator;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.api.types.BelongsToType;
import com.qcadoo.model.api.utils.DateUtils;
import com.qcadoo.view.internal.components.grid.GridComponentColumn;
import com.qcadoo.view.internal.states.AbstractComponentState;

public final class GridComponentState extends AbstractComponentState {

    public static final String JSON_SELECTED_ENTITY_ID = "selectedEntityId";

    public static final String JSON_BELONGS_TO_ENTITY_ID = "belongsToEntityId";

    public static final String JSON_FIRST_ENTITY = "firstEntity";

    public static final String JSON_MAX_ENTITIES = "maxEntities";

    public static final String JSON_TOTAL_ENTITIES = "totalEntities";

    public static final String JSON_ORDER = "order";

    public static final String JSON_ORDER_COLUMN = "column";

    public static final String JSON_ORDER_DIRECTION = "direction";

    public static final String JSON_FILTERS = "filters";

    public static final String JSON_FILTERS_ENABLED = "filtersEnabled";

    public static final String JSON_ENTITIES = "entities";

    public static final String JSON_EDITABLE = "isEditable";

    public static final String JSON_MULTISELECT_MODE = "multiselectMode";

    public static final String JSON_SELECTED_ENTITIES = "selectedEntities";

    public static final String JSON_ENTITIES_TO_MARK_AS_NEW = "entitiesToMarkAsNew";

    private final GridEventPerformer eventPerformer = new GridEventPerformer();

    private final Map<String, GridComponentColumn> columns;

    private final FieldDefinition belongsToFieldDefinition;

    private Long selectedEntityId;

    private Long belongsToEntityId;

    private List<Entity> entities;

    private int totalEntities;

    private int firstResult;

    private int maxResults = Integer.MAX_VALUE;

    private boolean filtersEnabled = true;

    private String orderColumn;

    private String orderDirection;

    private boolean multiselectMode = false;

    private Boolean isEditable = null;

    private Set<Long> selectedEntities = new HashSet<Long>();

    private Set<Long> entitiesToMarkAsNew = new HashSet<Long>();

    private CustomRestriction customRestriction;

    private final Map<String, String> filters = new HashMap<String, String>();

    public GridComponentState(final FieldDefinition scopeField, final Map<String, GridComponentColumn> columns,
            final String orderColumn, final String orderDirection) {
        this.belongsToFieldDefinition = scopeField;
        this.columns = columns;
        this.orderColumn = orderColumn;
        this.orderDirection = orderDirection;
        registerEvent("refresh", eventPerformer, "refresh");
        registerEvent("select", eventPerformer, "selectEntity");
        registerEvent("remove", eventPerformer, "removeSelectedEntity");
        registerEvent("moveUp", eventPerformer, "moveUpSelectedEntity");
        registerEvent("moveDown", eventPerformer, "moveDownSelectedEntity");
        registerEvent("copy", eventPerformer, "copySelectedEntity");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initializeContext(final JSONObject json) throws JSONException {
        Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            String field = iterator.next();
            if (JSON_BELONGS_TO_ENTITY_ID.equals(field)) {
                onScopeEntityIdChange(json.getLong(field));
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initializeContent(final JSONObject json) throws JSONException {
        if (json.has(JSON_SELECTED_ENTITY_ID) && !json.isNull(JSON_SELECTED_ENTITY_ID)) {
            selectedEntityId = json.getLong(JSON_SELECTED_ENTITY_ID);
        }
        if (json.has(JSON_MULTISELECT_MODE) && !json.isNull(JSON_MULTISELECT_MODE)) {
            multiselectMode = json.getBoolean(JSON_MULTISELECT_MODE);
        }
        if (json.has(JSON_SELECTED_ENTITIES) && !json.isNull(JSON_SELECTED_ENTITIES)) {
            JSONObject selectedEntitiesObj = json.getJSONObject(JSON_SELECTED_ENTITIES);
            JSONArray selectedEntitiesIds = selectedEntitiesObj.names();
            if (selectedEntitiesIds != null) {
                for (int i = 0; i < selectedEntitiesIds.length(); i++) {
                    String key = selectedEntitiesIds.getString(i);
                    boolean isSelected = false;
                    if (selectedEntitiesObj.has(key) && !selectedEntitiesObj.isNull(key)) {
                        isSelected = selectedEntitiesObj.getBoolean(key);
                    }
                    if (isSelected) {
                        selectedEntities.add(Long.parseLong(key));
                    }
                }
            }
        }
        if (json.has(JSON_BELONGS_TO_ENTITY_ID) && !json.isNull(JSON_BELONGS_TO_ENTITY_ID)) {
            belongsToEntityId = json.getLong(JSON_BELONGS_TO_ENTITY_ID);
        }
        if (json.has(JSON_FIRST_ENTITY) && !json.isNull(JSON_FIRST_ENTITY)) {
            firstResult = json.getInt(JSON_FIRST_ENTITY);
        }
        if (json.has(JSON_MAX_ENTITIES) && !json.isNull(JSON_MAX_ENTITIES)) {
            maxResults = json.getInt(JSON_MAX_ENTITIES);
        }
        if (json.has(JSON_FILTERS_ENABLED) && !json.isNull(JSON_FILTERS_ENABLED)) {
            filtersEnabled = json.getBoolean(JSON_FILTERS_ENABLED);
        }
        if (json.has(JSON_ORDER) && !json.isNull(JSON_ORDER)) {
            JSONObject orderJson = json.getJSONObject(JSON_ORDER);
            if (orderJson.has(JSON_ORDER_COLUMN) && orderJson.has(JSON_ORDER_DIRECTION)) {
                orderColumn = orderJson.getString(JSON_ORDER_COLUMN);
                orderDirection = orderJson.getString(JSON_ORDER_DIRECTION);
            }
        }
        if (json.has(JSON_FILTERS) && !json.isNull(JSON_FILTERS)) {
            JSONObject filtersJson = json.getJSONObject(JSON_FILTERS);
            Iterator<String> filtersKeys = filtersJson.keys();
            while (filtersKeys.hasNext()) {
                String column = filtersKeys.next();
                filters.put(column, filtersJson.getString(column).trim());
            }
        }

        if (belongsToFieldDefinition != null && belongsToEntityId == null) {
            setEnabled(false);
        }

        requestRender();
        requestUpdateState();
    }

    @Override
    public void onFieldEntityIdChange(final Long entityId) {
        setSelectedEntityId(entityId);
    }

    @Override
    public void onScopeEntityIdChange(final Long scopeEntityId) {
        if (belongsToFieldDefinition != null) {
            if (belongsToEntityId != null && !belongsToEntityId.equals(scopeEntityId)) {
                setSelectedEntityId(null);
                selectedEntities = new HashSet<Long>();
                multiselectMode = false;
            }
            belongsToEntityId = scopeEntityId;
            setEnabled(scopeEntityId != null);
        } else {
            throw new IllegalStateException("Grid doesn't have scopeField, it cannot set scopeEntityId");
        }
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        if (entities == null) {
            eventPerformer.reload();
        }

        if (entities == null) {
            throw new IllegalStateException("Cannot load entities for grid component");
        }

        JSONObject json = new JSONObject();
        json.put(JSON_SELECTED_ENTITY_ID, selectedEntityId);
        json.put(JSON_BELONGS_TO_ENTITY_ID, belongsToEntityId);
        json.put(JSON_FIRST_ENTITY, firstResult);
        json.put(JSON_MAX_ENTITIES, maxResults);
        json.put(JSON_FILTERS_ENABLED, filtersEnabled);
        json.put(JSON_TOTAL_ENTITIES, totalEntities);

        json.put(JSON_MULTISELECT_MODE, multiselectMode);
        JSONObject selectedEntitiesJson = new JSONObject();
        for (Long entityId : selectedEntities) {
            selectedEntitiesJson.put(entityId.toString(), true);
        }
        json.put(JSON_SELECTED_ENTITIES, selectedEntitiesJson);

        if (isEditable != null) {
            json.put(JSON_EDITABLE, isEditable);
        }

        if (entitiesToMarkAsNew.size() > 0) {
            JSONObject entitiesToMarkAsNewJson = new JSONObject();
            for (Long entityId : entitiesToMarkAsNew) {
                entitiesToMarkAsNewJson.put(entityId.toString(), true);
            }
            json.put(JSON_ENTITIES_TO_MARK_AS_NEW, entitiesToMarkAsNewJson);
        }

        if (orderColumn != null) {
            JSONObject jsonOrder = new JSONObject();
            jsonOrder.put(JSON_ORDER_COLUMN, orderColumn);
            jsonOrder.put(JSON_ORDER_DIRECTION, orderDirection);
            json.put(JSON_ORDER, jsonOrder);
        }

        JSONObject jsonFilters = new JSONObject();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            jsonFilters.put(entry.getKey(), entry.getValue());
        }

        json.put(JSON_FILTERS, jsonFilters);

        JSONArray jsonEntities = new JSONArray();
        for (Entity entity : entities) {
            jsonEntities.put(convertEntityToJson(entity));
        }

        json.put(JSON_ENTITIES, jsonEntities);

        return json;
    }

    private JSONObject convertEntityToJson(final Entity entity) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", entity.getId());
        JSONObject fields = new JSONObject();
        for (GridComponentColumn column : columns.values()) {
            fields.put(column.getName(), column.getValue(entity, getLocale()));
        }
        json.put("fields", fields);

        return json;
    }

    public Set<Long> getSelectedEntitiesId() {
        return selectedEntities;
    }

    public void setIsEditable(final boolean isEditable) {
        this.isEditable = isEditable;
    }

    public void setSelectedEntitiesId(final Set<Long> selectedEntities) {
        this.selectedEntities = selectedEntities;
        if (selectedEntities == null || selectedEntities.size() < 2) {
            multiselectMode = false;
        } else {
            multiselectMode = true;
        }
    }

    public void setEntities(final List<Entity> entities) {
        this.entities = entities;
        totalEntities = entities.size();
    }

    public void setSelectedEntityId(final Long selectedEntityId) {
        this.selectedEntityId = selectedEntityId;
        notifyEntityIdChangeListeners(selectedEntityId);
    }

    public Long getSelectedEntityId() {
        return selectedEntityId;
    }

    @Override
    public Object getFieldValue() {
        return getSelectedEntityId();
    }

    @Override
    public void setFieldValue(final Object value) {
        setSelectedEntityId((Long) value);
    }

    private String translateMessage(final String key) {
        List<String> codes = Arrays.asList(new String[] { getTranslationPath() + "." + key, "core.message." + key });
        return getTranslationService().translate(codes, getLocale());
    }

    public void setCustomRestriction(final CustomRestriction customRestriction) {
        this.customRestriction = customRestriction;
    }

    protected class GridEventPerformer {

        public void refresh(final String[] args) {
            // nothing interesting here
        }

        public void selectEntity(final String[] args) {
            notifyEntityIdChangeListeners(getSelectedEntityId());
        }

        public void removeSelectedEntity(final String[] args) {
            getDataDefinition().delete(selectedEntities);
            if (selectedEntities.size() == 1) {
                addMessage(translateMessage("deleteMessage"), MessageType.SUCCESS);
            } else {
                addMessage(selectedEntities.size() + " " + translateMessage("deleteMessages"), MessageType.SUCCESS);
            }
            setSelectedEntityId(null);
            multiselectMode = false;
            selectedEntities = new HashSet<Long>();
        }

        public void moveUpSelectedEntity(final String[] args) {
            getDataDefinition().move(selectedEntityId, -1);
            addMessage(translateMessage("moveMessage"), MessageType.SUCCESS);
        }

        public void copySelectedEntity(final String[] args) {
            entitiesToMarkAsNew = getDataDefinition().copy(selectedEntities);
            if (selectedEntities.size() == 1) {
                addMessage(translateMessage("copyMessage"), MessageType.SUCCESS);
            } else {
                addMessage(selectedEntities.size() + " " + translateMessage("copyMessages"), MessageType.SUCCESS);
            }
        }

        public void moveDownSelectedEntity(final String[] args) {
            getDataDefinition().move(selectedEntityId, 1);
            addMessage(translateMessage("moveMessage"), MessageType.SUCCESS);
        }

        private void reload() {
            if (belongsToFieldDefinition == null || belongsToEntityId != null) {
                SearchCriteriaBuilder criteria = getDataDefinition().find();
                if (belongsToFieldDefinition != null) {
                    criteria.restrictedWith(Restrictions.belongsTo(belongsToFieldDefinition, belongsToEntityId));
                }

                try {
                    if (filtersEnabled) {
                        addFilters(criteria);
                    }

                    if (customRestriction != null) {
                        customRestriction.addRestriction(criteria);
                    }

                    addOrder(criteria);
                    addPaging(criteria);

                    SearchResult result = criteria.list();

                    if (repeatWithFixedFirstResult(result)) {
                        addPaging(criteria);
                        result = criteria.list();
                    }

                    entities = result.getEntities();
                    totalEntities = result.getTotalNumberOfEntities();
                } catch (ParseException e) {
                    entities = Collections.emptyList();
                    totalEntities = 0;
                }
            } else {
                entities = Collections.emptyList();
                totalEntities = 0;
            }
        }

        private void addPaging(final SearchCriteriaBuilder criteria) {
            criteria.withFirstResult(firstResult);
            criteria.withMaxResults(maxResults);
        }

        private void addFilters(final SearchCriteriaBuilder criteria) throws ParseException {
            for (Map.Entry<String, String> filter : filters.entrySet()) {
                String field = getFieldNameByColumnName(filter.getKey());

                if (field != null) {
                    FieldDefinition fieldDefinition = getFieldDefinition(field);

                    Map.Entry<RestrictionOperator, String> parsedFilterValue = parseFilterValue(filter.getValue());

                    if ("".equals(parsedFilterValue.getValue())) {
                        continue;
                    }

                    if (fieldDefinition != null && String.class.isAssignableFrom(fieldDefinition.getType().getType())) {

                        criteria.restrictedWith(getRestrictionsToString(parsedFilterValue, fieldDefinition));

                    } else if (fieldDefinition != null && Boolean.class.isAssignableFrom(fieldDefinition.getType().getType())) {
                        criteria.restrictedWith(Restrictions.forOperator(parsedFilterValue.getKey(), fieldDefinition,
                                "1".equals(parsedFilterValue.getValue())));
                    } else if (fieldDefinition != null && Date.class.isAssignableFrom(fieldDefinition.getType().getType())) {

                        criteria.restrictedWith(getRestrictionsToDate(parsedFilterValue, fieldDefinition));

                    } else {
                        criteria.restrictedWith(Restrictions.forOperator(parsedFilterValue.getKey(), fieldDefinition,
                                parsedFilterValue.getValue()));
                    }
                }
            }
        }

        private Restriction getRestrictionsToString(final Map.Entry<RestrictionOperator, String> parsedFilterValue,
                final FieldDefinition fieldDefinition) {
            if (parsedFilterValue.getKey().equals(RestrictionOperator.EQ)) {
                return Restrictions.forOperator(parsedFilterValue.getKey(), fieldDefinition, parsedFilterValue.getValue() + "*");
            } else if (parsedFilterValue.getKey().equals(RestrictionOperator.NE)) {
                return Restrictions.not(Restrictions.eq(fieldDefinition, parsedFilterValue.getValue() + "*"));
            } else if (parsedFilterValue.getKey().equals(RestrictionOperator.GT)) {
                return Restrictions.and(Restrictions.gt(fieldDefinition, parsedFilterValue.getValue()),
                        Restrictions.not(Restrictions.eq(fieldDefinition, parsedFilterValue.getValue() + "*")));
            } else if (parsedFilterValue.getKey().equals(RestrictionOperator.GE)) {
                return Restrictions.ge(fieldDefinition, parsedFilterValue.getValue());
            } else if (parsedFilterValue.getKey().equals(RestrictionOperator.LT)) {
                return Restrictions.lt(fieldDefinition, parsedFilterValue.getValue());
            } else if (parsedFilterValue.getKey().equals(RestrictionOperator.LE)) {
                return Restrictions.or(Restrictions.le(fieldDefinition, parsedFilterValue.getValue()),
                        Restrictions.eq(fieldDefinition, parsedFilterValue.getValue() + "*"));
            }
            throw new IllegalStateException("unknown operator");
        }

        private Restriction getRestrictionsToDate(final Map.Entry<RestrictionOperator, String> parsedFilterValue,
                final FieldDefinition fieldDefinition) throws ParseException {
            Date minDate = DateUtils.parseDate(parsedFilterValue.getValue(), false);
            Date maxDate = DateUtils.parseDate(parsedFilterValue.getValue(), true);
            if (minDate == null || maxDate == null) {
                throw new ParseException("wrong date", 1);
            }
            if (parsedFilterValue.getKey().equals(RestrictionOperator.EQ)) {
                return Restrictions.and(Restrictions.ge(fieldDefinition, minDate), Restrictions.le(fieldDefinition, maxDate));
            } else if (parsedFilterValue.getKey().equals(RestrictionOperator.NE)) {
                return Restrictions.or(Restrictions.lt(fieldDefinition, minDate), Restrictions.gt(fieldDefinition, maxDate));
            } else if (parsedFilterValue.getKey().equals(RestrictionOperator.GT)) {
                return Restrictions.gt(fieldDefinition, maxDate);
            } else if (parsedFilterValue.getKey().equals(RestrictionOperator.GE)) {
                return Restrictions.ge(fieldDefinition, minDate);
            } else if (parsedFilterValue.getKey().equals(RestrictionOperator.LT)) {
                return Restrictions.lt(fieldDefinition, minDate);
            } else if (parsedFilterValue.getKey().equals(RestrictionOperator.LE)) {
                return Restrictions.le(fieldDefinition, maxDate);
            }
            throw new IllegalStateException("unknown operator");
        }

        private Map.Entry<RestrictionOperator, String> parseFilterValue(final String filterValue) {
            RestrictionOperator operator = RestrictionOperator.EQ;
            String value;
            if (filterValue.charAt(0) == '>') {
                if (filterValue.length() > 1 && filterValue.charAt(1) == '=') {
                    operator = RestrictionOperator.GE;
                    value = filterValue.substring(2);
                } else if (filterValue.length() > 1 && filterValue.charAt(1) == '<') {
                    operator = RestrictionOperator.NE;
                    value = filterValue.substring(2);
                } else {
                    operator = RestrictionOperator.GT;
                    value = filterValue.substring(1);
                }
            } else if (filterValue.charAt(0) == '<') {
                if (filterValue.length() > 1 && filterValue.charAt(1) == '=') {
                    operator = RestrictionOperator.LE;
                    value = filterValue.substring(2);
                } else if (filterValue.length() > 1 && filterValue.charAt(1) == '>') {
                    operator = RestrictionOperator.NE;
                    value = filterValue.substring(2);
                } else {
                    operator = RestrictionOperator.LT;
                    value = filterValue.substring(1);
                }
            } else if (filterValue.charAt(0) == '=') {
                if (filterValue.length() > 1 && filterValue.charAt(1) == '<') {
                    operator = RestrictionOperator.LE;
                    value = filterValue.substring(2);
                } else if (filterValue.length() > 1 && filterValue.charAt(1) == '>') {
                    operator = RestrictionOperator.GE;
                    value = filterValue.substring(2);
                } else if (filterValue.length() > 1 && filterValue.charAt(1) == '=') {
                    value = filterValue.substring(2);
                } else {
                    value = filterValue.substring(1);
                }
            } else {
                value = filterValue;
            }
            return Collections.singletonMap(operator, value.trim()).entrySet().iterator().next();
        }

        private FieldDefinition getFieldDefinition(final String field) {
            String[] path = field.split("\\.");

            DataDefinition dataDefinition = getDataDefinition();

            for (int i = 0; i < path.length; i++) {
                if (dataDefinition.getField(path[i]) == null) {
                    return null;
                }

                FieldDefinition fieldDefinition = dataDefinition.getField(path[i]);

                if (i < path.length - 1) {
                    if (fieldDefinition.getType() instanceof BelongsToType) {
                        dataDefinition = ((BelongsToType) fieldDefinition.getType()).getDataDefinition();
                        continue;
                    } else {
                        return null;
                    }
                }

                return fieldDefinition;
            }

            return null;
        }

        private void addOrder(final SearchCriteriaBuilder criteria) {
            if (orderColumn != null) {
                String field = getFieldNameByColumnName(orderColumn);

                if (field != null) {
                    if ("asc".equals(orderDirection)) {
                        criteria.orderAscBy(field);
                    } else {
                        criteria.orderDescBy(field);
                    }
                }
            }
        }

        private String getFieldNameByColumnName(final String columnName) {
            GridComponentColumn column = columns.get(columnName);

            if (column == null) {
                return null;
            }

            if (StringUtils.hasText(column.getExpression())) {
                Matcher matcher = Pattern.compile("#(\\w+)\\['(\\w+)'\\]").matcher(column.getExpression());
                if (matcher.matches()) {
                    return matcher.group(1) + "." + matcher.group(2);
                }
            } else if (column.getFields().size() == 1) {
                return column.getFields().get(0).getName();
            }

            return null;
        }

        private boolean repeatWithFixedFirstResult(final SearchResult result) {
            if (result.getEntities().isEmpty() && result.getTotalNumberOfEntities() > 0) {
                while (firstResult >= result.getTotalNumberOfEntities()) {
                    firstResult -= maxResults;
                }
                return true;
            } else {
                return false;
            }
        }
    }

}
