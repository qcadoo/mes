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

package com.qcadoo.view.components.layout;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.internal.ComponentPattern;
import com.qcadoo.view.internal.components.layout.SmallTabLayoutPatternTab;
import com.qcadoo.view.internal.states.AbstractContainerState;

public class SmallTabLayoutState extends AbstractContainerState {

    private final List<SmallTabLayoutPatternTab> tabs;

    public SmallTabLayoutState(final List<SmallTabLayoutPatternTab> tabs) {
        this.tabs = tabs;
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        // empty
        requestRender();
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject state = new JSONObject();
        List<String> errorTabs = new LinkedList<String>();
        for (Map.Entry<String, ComponentState> child : getChildren().entrySet()) {
            if (child.getValue().isHasError()) {
                for (SmallTabLayoutPatternTab tab : tabs) {
                    boolean found = false;
                    for (ComponentPattern tabComponents : tab.getComponents()) {
                        if (tabComponents.getName().equals(child.getValue().getName())) {
                            errorTabs.add(tab.getName());
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }

                }
            }
        }
        JSONArray errors = new JSONArray();
        for (String tabName : errorTabs) {
            errors.put(tabName);
        }
        state.put("errors", errors);

        return state;
    }
}
