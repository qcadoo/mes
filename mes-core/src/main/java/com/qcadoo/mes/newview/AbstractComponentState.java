package com.qcadoo.mes.newview;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private final Map<String, EventHandler> eventHandlers = new HashMap<String, EventHandler>();

    private boolean requestRender;

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

    public void registemCustomEvent(final String name, final Object obj, final String method) {
        eventHandlers.put(name, new EventHandler(obj, method, true));
    }

    protected void registerEvent(final String name, final Object obj, final String method) {
        eventHandlers.put(name, new EventHandler(obj, method, false));
    }

    @Override
    public final void performEvent(final String event, final String... args) {
        if (!eventHandlers.containsKey(event)) {
            throw new IllegalStateException("Event with given name doesn't exist");
        } else {
            eventHandlers.get(event).invokeEvent(args);
        }
    }

    @Override
    public JSONObject render() throws JSONException {
        JSONObject json = new JSONObject();

        // wypełnic wpólne pola

        if (requestRender) {
            json.put("content", renderContent());
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

    protected final Map<String, FieldEntityIdChangeListener> getFieldEntityIdChangeListeners() {
        return fieldEntityIdChangeListeners;
    }

    protected final void requestRender() {
        requestRender = true;
    }

    public void addFieldEntityIdChangeListener(final String field, final FieldEntityIdChangeListener listener) {
        fieldEntityIdChangeListeners.put(field, listener);
    }

    public void addScopeEntityIdChangeListener(final ScopeEntityIdChangeListener listener) {
        scopeEntityIdChangeListeners.add(listener);
    }

    @Override
    public void onFieldEntityIdChange(final Long entityId) {
        // implements if you want
    }

    @Override
    public void onScopeEntityIdChange(final Long entityId) {
        // implements if you want
    }

    private class EventHandler {

        private final Method method;

        private final Object obj;

        private final boolean isCustom;

        public EventHandler(final Object obj, final String method, final boolean isCustom) {
            this.isCustom = isCustom;
            this.obj = obj;
            try {
                if (isCustom) {
                    this.method = obj.getClass().getDeclaredMethod(method, ComponentState.class, String[].class);
                } else {
                    this.method = obj.getClass().getDeclaredMethod(method, String[].class);
                }
            } catch (SecurityException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        public void invokeEvent(final String[] args) {
            try {
                if (isCustom) {
                    method.invoke(obj, AbstractComponentState.this, args);
                } else {
                    method.invoke(obj, new Object[] { args });
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

}
