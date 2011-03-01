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