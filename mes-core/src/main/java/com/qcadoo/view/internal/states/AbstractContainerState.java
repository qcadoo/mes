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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ContainerState;

public abstract class AbstractContainerState extends AbstractComponentState implements ContainerState {

    private final Map<String, ComponentState> children = new HashMap<String, ComponentState>();

    @Override
    public final void initialize(final JSONObject json, final Locale locale) throws JSONException {
        super.initialize(json, locale);

        JSONObject childerJson = null;
        if (json.has(JSON_CHILDREN)) {
            childerJson = json.getJSONObject(JSON_CHILDREN);
        }

        for (Map.Entry<String, ComponentState> child : children.entrySet()) {
            if (childerJson == null) {
                child.getValue().initialize(new JSONObject(), locale);
            } else {
                child.getValue().initialize(childerJson.getJSONObject(child.getKey()), locale);
            }
        }
    }

    @Override
    public JSONObject render() throws JSONException {
        JSONObject json = super.render();

        JSONObject childerJson = new JSONObject();

        for (Map.Entry<String, ComponentState> child : children.entrySet()) {
            childerJson.put(child.getKey(), child.getValue().render());
        }

        json.put(JSON_CHILDREN, childerJson);

        return json;
    }

    @Override
    public boolean isHasError() {
        if (super.isHasError()) {
            return true;
        }
        for (ComponentState child : children.values()) {
            if (child.isHasError()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final Map<String, ComponentState> getChildren() {
        return children;
    }

    @Override
    public final ComponentState getChild(final String name) {
        return children.get(name);
    }

    @Override
    public final void addChild(final ComponentState state) {
        children.put(state.getName(), state);
    }

}
