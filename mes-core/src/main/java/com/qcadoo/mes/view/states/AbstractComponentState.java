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
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.internal.EntityIdChangeListenerHolder;
import com.qcadoo.mes.view.internal.EventHandlerHolder;
import com.qcadoo.mes.view.internal.MessageHolder;

public abstract class AbstractComponentState implements ComponentState, FieldEntityIdChangeListener, ScopeEntityIdChangeListener {

    private final EntityIdChangeListenerHolder listenerHolder = new EntityIdChangeListenerHolder();

    private final EventHandlerHolder eventHandlerHolder = new EventHandlerHolder(this);

    private MessageHolder messageHolder;

    private String name;

    private Locale locale;

    private DataDefinition dataDefinition;

    private TranslationService translationService;

    private boolean requestRender;

    private boolean requestUpdateState;

    private boolean enabled = true;

    private boolean visible = true;

    private boolean hasError = false;

    private String translationPath;

    @Override
    public final String getName() {
        return name;
    };

    public final void setName(final String name) {
        this.name = name;
    }

    public final void setDataDefinition(final DataDefinition dataDefinition) {
        this.dataDefinition = dataDefinition;
    }

    protected final DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    public final void setTranslationService(final TranslationService translationService) {
        this.translationService = translationService;
    }

    protected final TranslationService getTranslationService() {
        return translationService;
    }

    public final void setTranslationPath(final String translationPath) {
        this.translationPath = translationPath;
    }

    protected final String getTranslationPath() {
        return translationPath;
    }

    @Override
    public final void addMessage(final String message, final MessageType type) {
        addMessage(message, type, true);
    }

    @Override
    public final void addMessage(final String message, final MessageType type, final boolean autoClose) {
        messageHolder.addMessage(null, message, type, autoClose);
        if (MessageType.FAILURE.equals(type)) {
            hasError = true;
        }
    }

    @Override
    public boolean isHasError() {
        return hasError;
    }

    @Override
    public void initialize(final JSONObject json, final Locale locale) throws JSONException {
        this.locale = locale;
        this.messageHolder = new MessageHolder(translationService, locale);

        if (json.has(JSON_ENABLED)) {
            setEnabled(json.getBoolean(JSON_ENABLED));
        }

        if (json.has(JSON_VISIBLE)) {
            setVisible(json.getBoolean(JSON_VISIBLE));
        }

        if (json.has(JSON_CONTENT)) {
            initializeContent(json.getJSONObject(JSON_CONTENT));
        }

        if (json.has(JSON_CONTEXT)) {
            initializeContext(json.getJSONObject(JSON_CONTEXT));
        }
    }

    @Override
    public final Locale getLocale() {
        return locale;
    }

    protected abstract void initializeContent(final JSONObject json) throws JSONException;

    public final void registerCustomEvent(final String name, final Object obj, final String method) {
        eventHandlerHolder.registemCustomEvent(name, obj, method);
    }

    protected final void registerEvent(final String name, final Object obj, final String method) {
        eventHandlerHolder.registemEvent(name, obj, method);
    }

    @Override
    public final void performEvent(final ViewDefinitionState viewDefinitionState, final String event, final String... args) {
        eventHandlerHolder.performEvent(viewDefinitionState, event, args);
    }

    @Override
    public JSONObject render() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ENABLED, isEnabled());
        json.put(JSON_VISIBLE, isVisible());
        if (messageHolder != null) {
            json.put(JSON_MESSAGES, messageHolder.renderMessages());
        }

        if (requestRender) {
            json.put(JSON_CONTENT, renderContent());
            json.put(JSON_UPDATE_STATE, requestUpdateState);
        } else {
            json.put(JSON_UPDATE_STATE, false);
        }

        return json;
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

    public final void addFieldEntityIdChangeListener(final String field, final FieldEntityIdChangeListener listener) {
        listenerHolder.addFieldEntityIdChangeListener(field, listener);
    }

    public final void addScopeEntityIdChangeListener(final String scope, final ScopeEntityIdChangeListener listener) {
        listenerHolder.addScopeEntityIdChangeListener(scope, listener);
    }

    @Override
    public final boolean isVisible() {
        return visible;
    }

    @Override
    public final void setVisible(final boolean visible) {
        this.visible = visible;
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final void setEnabled(final boolean enabled) {
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

    protected void initializeContext(final JSONObject json) throws JSONException {
        // implements if you want
    }

    @Override
    public void setFieldValue(final Object value) {
        // implements if you want
    }

    @Override
    public Object getFieldValue() {
        return null; // implements if you want
    }

}
