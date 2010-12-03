package com.qcadoo.mes.view.components;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.view.components.GridComponentPattern.Column;
import com.qcadoo.mes.view.states.AbstractComponentState;

public class GridComponentState extends AbstractComponentState {

    public static final String JSON_SELECTED_ENTITY_ID = "selectedEntityId";

    public static final String JSON_SCOPE_ENTITY_ID = "scopeEntityId";

    public static final String JSON_FIRST_ENTITY = "firstEntity";

    public static final String JSON_MAX_ENTITIES = "maxEntities";

    public static final String JSON_TOTAL_ENTITIES = "totalEntities";

    public static final String JSON_ORDER = "order";

    public static final String JSON_FILTERS = "filters";

    public static final String JSON_FILTERS_ENABLED = "filtersEnabled";

    public static final String JSON_ENTITIES = "entities";

    private final GridEventPerformer eventPerformer = new GridEventPerformer();

    private final Collection<Column> columns;

    private final FieldDefinition scopeField;

    private Long selectedEntityId;

    private Long scopeEntityId;

    private List<Entity> entities;

    private int totalEntities;

    private int firstResult;

    private int maxResults = Integer.MAX_VALUE;

    private boolean filtersEnabled = false;

    private final Map<String, Boolean> order = new HashMap<String, Boolean>();

    private final Map<String, String> filters = new HashMap<String, String>();

    public GridComponentState(final FieldDefinition scopeField, final Collection<Column> columns) {
        this.scopeField = scopeField;
        this.columns = columns;
        registerEvent("changeSelectedEntityId", eventPerformer, "changeSelectedEntityId");
        registerEvent("removeSelectedEntityId", eventPerformer, "removeSelectedEntityId");
        registerEvent("moveUpSelectedEntityId", eventPerformer, "moveUpSelectedEntityId");
        registerEvent("moveDownSelectedEntityId", eventPerformer, "moveDownSelectedEntityId");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initializeContent(final JSONObject json) throws JSONException {
        if (json.has(JSON_SELECTED_ENTITY_ID) && !json.isNull(JSON_SELECTED_ENTITY_ID)) {
            selectedEntityId = json.getLong(JSON_SELECTED_ENTITY_ID);
        }
        if (json.has(JSON_SCOPE_ENTITY_ID) && !json.isNull(JSON_SCOPE_ENTITY_ID)) {
            scopeEntityId = json.getLong(JSON_SCOPE_ENTITY_ID);
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
            if (orderJson.length() == 1) {
                String column = (String) orderJson.keys().next();
                order.put(column, orderJson.getBoolean(column));
            }
        }
        if (json.has(JSON_FILTERS) && !json.isNull(JSON_FILTERS)) {
            JSONObject filtersJson = json.getJSONObject(JSON_FILTERS);
            Iterator<String> filtersKeys = filtersJson.keys();
            while (filtersKeys.hasNext()) {
                String column = filtersKeys.next();
                filters.put(column, filtersJson.getString(column));
            }
        }

        if (scopeField != null && scopeEntityId == null) {
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
        if (scopeField != null) {
            this.scopeEntityId = scopeEntityId;
            setEnabled(true);
        } else {
            throw new IllegalStateException("Grid doesn't have scopeField, it cannot set scopeEntityId");
        }
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        if (entities == null) {
            eventPerformer.refresh();
        }

        JSONObject json = new JSONObject();
        json.put(JSON_SELECTED_ENTITY_ID, selectedEntityId);
        json.put(JSON_SCOPE_ENTITY_ID, scopeEntityId);
        json.put(JSON_FIRST_ENTITY, firstResult);
        json.put(JSON_MAX_ENTITIES, maxResults);
        json.put(JSON_FILTERS_ENABLED, filtersEnabled);
        json.put(JSON_TOTAL_ENTITIES, totalEntities);

        JSONObject jsonOrder = new JSONObject();
        for (Map.Entry<String, Boolean> entry : order.entrySet()) {
            jsonOrder.put(entry.getKey(), entry.getValue());
            break;
        }

        json.put(JSON_ORDER, jsonOrder);

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
        for (Column column : columns) {
            fields.put(column.getName(), column.getValue(entity, getLocale()));
        }
        json.put("fields", fields);

        return json;
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

    protected class GridEventPerformer {

        public void changeSelectedEntityId(final String[] args) {
            notifyEntityIdChangeListeners(getSelectedEntityId());
        }

        public void removeSelectedEntityId(final String[] args) {
            try {
                getDataDefinition().delete(selectedEntityId);
                addMessage("TODO - usunięto", MessageType.SUCCESS); // TODO masz
            } catch (IllegalStateException e) {
                addMessage("TODO - nieusunięto - " + e.getMessage(), MessageType.FAILURE); // TODO masz
            }
        }

        public void moveUpSelectedEntityId(final String[] args) {
            try {
                getDataDefinition().move(selectedEntityId, -1);
                addMessage("TODO - przesunięto", MessageType.SUCCESS); // TODO masz
            } catch (IllegalStateException e) {
                addMessage("TODO - nieprzesunięto - " + e.getMessage(), MessageType.FAILURE); // TODO masz
            }
        }

        public void moveDownSelectedEntityId(final String[] args) {
            try {
                getDataDefinition().move(selectedEntityId, 1);
                addMessage("TODO - przesunięto", MessageType.SUCCESS); // TODO masz
            } catch (IllegalStateException e) {
                addMessage("TODO - nieprzesunięto - " + e.getMessage(), MessageType.FAILURE); // TODO masz
            }
        }

        public void refresh() {
            if (isEnabled() && (scopeField == null || scopeEntityId != null)) {
                SearchCriteriaBuilder criteria = getDataDefinition().find();
                if (scopeField != null) {
                    criteria.restrictedWith(Restrictions.belongsTo(scopeField, scopeEntityId));
                }

                // TODO restrictions, orders, paging

                SearchResult result = criteria.list();

                entities = result.getEntities();
                totalEntities = result.getTotalNumberOfEntities();
            } else {
                entities = Collections.emptyList();
                totalEntities = 0;
            }
        }
    }

}
