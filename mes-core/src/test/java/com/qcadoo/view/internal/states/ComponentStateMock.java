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

package com.qcadoo.view.internal.states;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.view.internal.ComponentState;
import com.qcadoo.view.internal.FieldEntityIdChangeListener;
import com.qcadoo.view.internal.ScopeEntityIdChangeListener;
import com.qcadoo.view.internal.states.AbstractComponentState;

public class ComponentStateMock extends AbstractComponentState {

    private final JSONObject render;

    private JSONObject content;

    public ComponentStateMock() {
        render = new JSONObject();
    }

    public ComponentStateMock(final JSONObject render) {
        this.render = render;
    }

    @Override
    protected void initializeContent(final JSONObject content) throws JSONException {
        requestRender();
        this.content = content;
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        return render;
    }

    public Map<String, FieldEntityIdChangeListener> getPublicFieldEntityIdChangeListeners() {
        return getFieldEntityIdChangeListeners();
    }

    public Map<String, ScopeEntityIdChangeListener> getPublicScopeFieldEntityIdChangeListeners() {
        return getScopeEntityIdChangeListeners();
    }

    public void registerTestEvent(final String name, final TestEvent obj) {
        registerEvent(name, obj, "invoke");
    }

    public void registerTestCustomEvent(final String name, final TestCustomEvent obj) {
        registerEvent(name, obj, "invoke");
    }

    public JSONObject getContent() {
        return content;
    }

    public static interface TestEvent {

        void invoke(String... args);

    }

    public static interface TestCustomEvent {

        void invoke(ComponentState componentState, String... args);

    }

}
