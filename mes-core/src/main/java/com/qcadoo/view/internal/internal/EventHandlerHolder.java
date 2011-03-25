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

package com.qcadoo.view.internal.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

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
            return;
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