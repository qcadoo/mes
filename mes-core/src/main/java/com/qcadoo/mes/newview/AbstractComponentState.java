package com.qcadoo.mes.newview;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractComponentState implements ComponentState, FieldEntityIdChangeListener, ScopeEntityIdChangeListener {

    private final Map<String, FieldEntityIdChangeListener> fieldEntityIdChangeListeners = new HashMap<String, FieldEntityIdChangeListener>();

    private final Set<ScopeEntityIdChangeListener> scopeEntityIdChangeListeners = new HashSet<ScopeEntityIdChangeListener>();

    @Override
    public String getName() {
        // TODO masz
        return null;
    };

    @Override
    public void initialize(final JSONObject json, final Locale locale) throws JSONException {

        // wypełnic wpólne pola

        initializeContent(json.getJSONObject("content"), locale);
    }

    protected abstract void initializeContent(final JSONObject json, final Locale locale) throws JSONException;

    @Override
    public final void performEvent(final String event, final String... args) {
        // komponent rejestruje evenetu, a tutaj wywolujemy konkretną metodę
    }

    @Override
    public JSONObject render() throws JSONException {
        JSONObject json = new JSONObject();

        // wypełnic wpólne pola

        // sprawdzanie czy komponent ma sie wyrenderować {
        json.put("content", renderContent());
        // }

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
        // ustawia żądanie renderowania na true
    }

    public void addFieldEntityIdChangeListener(final String field, final FieldEntityIdChangeListener listener) {
        fieldEntityIdChangeListeners.put(field, listener);
    }

    public void addScopeEntityIdChangeListener(final ScopeEntityIdChangeListener listener) {
        scopeEntityIdChangeListeners.add(listener);
    }

    @Override
    public void onFieldEntityIdChange(final Long entityId) {

    }

    @Override
    public void onScopeEntityIdChange(final Long entityId) {

    }

}
