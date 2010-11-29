package com.qcadoo.mes.newview;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class EventHandlerHolder {

    private final ComponentState owner;

    public EventHandlerHolder(final ComponentState owner) {
        this.owner = owner;
    }

    private final Map<String, EventHandler> eventHandlers = new HashMap<String, EventHandler>();

    public void registemCustomEvent(final String name, final Object obj, final String method) {
        eventHandlers.put(name, new EventHandler(obj, method, true));
    }

    public void registemEvent(final String name, final Object obj, final String method) {
        eventHandlers.put(name, new EventHandler(obj, method, false));
    }

    public final void performEvent(final String event, final String... args) {
        if (!eventHandlers.containsKey(event)) {
            throw new IllegalStateException("Event with given name doesn't exist");
        } else {
            eventHandlers.get(event).invokeEvent(args);
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
                    method.invoke(obj, owner, args);
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