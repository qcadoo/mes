package com.qcadoo.mes.view.internal;

import java.util.HashMap;
import java.util.Map;

import com.qcadoo.mes.view.FieldEntityIdChangeListener;
import com.qcadoo.mes.view.ScopeEntityIdChangeListener;

public final class EntityIdChangeListenerHolder {

    private final Map<String, FieldEntityIdChangeListener> fieldEntityIdChangeListeners = new HashMap<String, FieldEntityIdChangeListener>();

    private final Map<String, ScopeEntityIdChangeListener> scopeEntityIdChangeListeners = new HashMap<String, ScopeEntityIdChangeListener>();

    public void addFieldEntityIdChangeListener(final String field, final FieldEntityIdChangeListener listener) {
        fieldEntityIdChangeListeners.put(field, listener);
    }

    public void addScopeEntityIdChangeListener(final String scope, final ScopeEntityIdChangeListener listener) {
        scopeEntityIdChangeListeners.put(scope, listener);
    }

    public void notifyEntityIdChangeListeners(final Long entityId) {
        for (FieldEntityIdChangeListener listener : fieldEntityIdChangeListeners.values()) {
            listener.onFieldEntityIdChange(entityId);
        }
        for (ScopeEntityIdChangeListener listener : scopeEntityIdChangeListeners.values()) {
            listener.onScopeEntityIdChange(entityId);
        }
    }

    public Map<String, FieldEntityIdChangeListener> getFieldEntityIdChangeListeners() {
        return fieldEntityIdChangeListeners;
    }

    public Map<String, ScopeEntityIdChangeListener> getScopeEntityIdChangeListeners() {
        return scopeEntityIdChangeListeners;
    }
}