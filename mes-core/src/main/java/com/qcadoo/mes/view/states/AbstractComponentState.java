package com.qcadoo.mes.view.states;

import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.FieldEntityIdChangeListener;
import com.qcadoo.mes.view.ScopeEntityIdChangeListener;
import com.qcadoo.mes.view.internal.EntityIdChangeListenerHolder;
import com.qcadoo.mes.view.internal.EventHandlerHolder;
import com.qcadoo.mes.view.internal.MessageHolder;

public abstract class AbstractComponentState implements ComponentState, FieldEntityIdChangeListener, ScopeEntityIdChangeListener {

    private final EntityIdChangeListenerHolder listenerHolder = new EntityIdChangeListenerHolder();

    private final EventHandlerHolder eventHandlerHolder = new EventHandlerHolder(this);

    private final MessageHolder messageHolder = new MessageHolder();

    private String name;

    private Locale locale;

    private DataDefinition dataDefinition;

    private TranslationService translationService;

    private boolean requestRender;

    private boolean requestUpdateState;

    private boolean enabled;

    private boolean visible;

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
        messageHolder.addMessage(message, type);
        requestRender();
    }

    @Override
    public void initialize(final JSONObject json, final Locale locale) throws JSONException {
        this.locale = locale;

        if (json.has(JSON_ENABLED)) {
            setEnabled(json.getBoolean(JSON_ENABLED));
        }

        if (json.has(JSON_VISIBLE)) {
            setVisible(json.getBoolean(JSON_VISIBLE));
        }

        if (json.has(JSON_CONTENT)) {
            initializeContent(json.getJSONObject(JSON_CONTENT));
        }
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
        json.put(JSON_ENABLED, isEnabled());
        json.put(JSON_VISIBLE, isVisible());

        if (requestRender) {
            json.put(JSON_CONTENT, renderContent());
            json.put(JSON_MESSAGES, messageHolder.renderMessages());
            json.put(JSON_UPDATE_STATE, requestUpdateState);
        }

        return json;
    }

    @Override
    public void beforeRender() {
        beforeRenderContent();
    }

    protected void beforeRenderContent() {
        // can be implemented
    }

    protected abstract JSONObject renderContent() throws JSONException;

    protected final void notifyEntityIdChangeListeners(final Long entityId) {
        listenerHolder.notifyEntityIdChangeListeners(entityId);
    }

    protected final Map<String, FieldEntityIdChangeListener> getFieldEntityIdChangeListeners() {
        return listenerHolder.getFieldEntityIdChangeListeners();
    }

    protected final Map<String, ScopeEntityIdChangeListener> getScopeEntityIdChangeListeners() {
        return listenerHolder.getScopeEntityIdChangeListeners();
    }

    protected final void requestRender() {
        requestRender = true;
    }

    protected final void requestUpdateState() {
        requestUpdateState = true;
    }

    public void addFieldEntityIdChangeListener(final String field, final FieldEntityIdChangeListener listener) {
        listenerHolder.addFieldEntityIdChangeListener(field, listener);
    }

    public void addScopeEntityIdChangeListener(final String scope, final ScopeEntityIdChangeListener listener) {
        listenerHolder.addScopeEntityIdChangeListener(scope, listener);
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
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
