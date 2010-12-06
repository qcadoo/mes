package com.qcadoo.mes.view.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;

public final class EventHandlerHolder {

    private final ComponentState owner;

    public EventHandlerHolder(final ComponentState owner) {
        this.owner = owner;
    }

    private final Map<String, List<EventHandler>> eventHandlers = new HashMap<String, List<EventHandler>>();

    public void registemCustomEvent(final String event, final Object obj, final String method) {
        registemEvent(event, new EventHandler(obj, method, true));
    }

    public void registemEvent(final String event, final Object obj, final String method) {
        registemEvent(event, new EventHandler(obj, method, false));
    }

    private void registemEvent(final String event, final EventHandler eventHandler) {
        if (!eventHandlers.containsKey(event)) {
            eventHandlers.put(event, new ArrayList<EventHandler>());
        }
        eventHandlers.get(event).add(eventHandler);
    }

    public void performEvent(final ViewDefinitionState viewDefinitionState, final String event, final String... args) {
        if (!eventHandlers.containsKey(event)) {
            return; // TODO masz throw new IllegalStateException("Event with given name doesn't exist");
        } else {
            for (EventHandler eventHandler : eventHandlers.get(event)) {
                eventHandler.invokeEvent(viewDefinitionState, args);
            }
        }
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
                    this.method = obj.getClass().getDeclaredMethod(method, ViewDefinitionState.class, ComponentState.class,
                            String[].class);
                } else {
                    this.method = obj.getClass().getDeclaredMethod(method, String[].class);
                }
            } catch (SecurityException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        public void invokeEvent(final ViewDefinitionState viewDefinitionState, final String[] args) {
            try {
                if (isCustom) {
                    method.invoke(obj, viewDefinitionState, owner, args);
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