package com.qcadoo.mes.newview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.utils.Pair;

public abstract class AbstractComponentState implements ComponentState, FieldEntityIdChangeListener, ScopeEntityIdChangeListener {

    private final Map<String, FieldEntityIdChangeListener> fieldEntityIdChangeListeners = new HashMap<String, FieldEntityIdChangeListener>();

    private final Set<ScopeEntityIdChangeListener> scopeEntityIdChangeListeners = new HashSet<ScopeEntityIdChangeListener>();

    private final EventHandlerHolder eventHandlerHolder = new EventHandlerHolder(this);

    private final List<Pair<String, MessageType>> messages = new ArrayList<Pair<String, MessageType>>();

    private String name;

    private Locale locale;

    private DataDefinition dataDefinition;

    private TranslationService translationService;

    private boolean requestRender;

    private boolean requestUpdateState;

    @Override
    public String getName() {
        return name;
    };

    public void setName(final String name) {
        this.name = name;
    }

    public void setDataDefinition(final DataDefinition dataDefinition) {
        this.dataDefinition = dataDefinition;
    }

    protected DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    public void setTranslationService(final TranslationService translationService) {
        this.translationService = translationService;
    }

    protected TranslationService getTranslationService() {
        return translationService;
    }

    @Override
    public void addMessage(final String message, final MessageType type) {
        messages.add(new Pair<String, ComponentState.MessageType>(message, type));
        requestRender();
    }

    @Override
    public void initialize(final JSONObject json, final Locale locale) throws JSONException {
        this.locale = locale;

        // wypełnic wpólne pola

        initializeContent(json.getJSONObject(JSON_CONTENT));
    }

    protected Locale getLocale() {
        return locale;
    }

    protected abstract void initializeContent(final JSONObject json) throws JSONException;

    public void registemCustomEvent(final String name, final Object obj, final String method) {
        eventHandlerHolder.registemCustomEvent(name, obj, method);
    }

    protected void registerEvent(final String name, final Object obj, final String method) {
        eventHandlerHolder.registemEvent(name, obj, method);
    }

    @Override
    public final void performEvent(final String event, final String... args) {
        eventHandlerHolder.performEvent(event, args);
    }

    @Override
    public JSONObject render() throws JSONException {
        JSONObject json = new JSONObject();

        // wypełnic wpólne pola

        if (requestRender) {
            json.put(JSON_CONTENT, renderContent());
            json.put(JSON_MESSAGES, renderMessages());
            json.put(JSON_UPDATE_STATE, requestUpdateState);
        }

        return json;
    }

    private JSONArray renderMessages() throws JSONException {
        JSONArray json = new JSONArray();

        for (Pair<String, MessageType> message : messages) {
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put(JSON_MESSAGE_BODY, message.getKey());
            jsonMessage.put(JSON_MESSAGE_TYPE, message.getValue().ordinal());
            json.put(jsonMessage);
        }

        return json;
    }

    protected abstract JSONObject renderContent() throws JSONException;

    protected final void notifyEntityIdChangeListeners(final Long entityId) {
        for (FieldEntityIdChangeListener listener : fieldEntityIdChangeListeners.values()) {
            listener.onFieldEntityIdChange(entityId);
        }
        for (ScopeEntityIdChangeListener listener : scopeEntityIdChangeListeners) {
            listener.onScopeEntityIdChange(entityId);
        }
    }

    protected final void requestRender() {
        requestRender = true;
    }

    protected final void requestUpdateState() {
        requestUpdateState = true;
    }

    public void addFieldEntityIdChangeListener(final String field, final FieldEntityIdChangeListener listener) {
        fieldEntityIdChangeListeners.put(field, listener);
    }

    public void addScopeEntityIdChangeListener(final ScopeEntityIdChangeListener listener) {
        scopeEntityIdChangeListeners.add(listener);
    }

    protected Map<String, FieldEntityIdChangeListener> getFieldEntityIdChangeListeners() {
        return fieldEntityIdChangeListeners;
    }

    @Override
    public void onFieldEntityIdChange(final Long entityId) {
        // implements if you want
    }

    @Override
    public void onScopeEntityIdChange(final Long entityId) {
        // implements if you want
    }

}
