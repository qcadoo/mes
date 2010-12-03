/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.view.menu;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents single menu item
 */
public abstract class MenuItem {

    private final String name;

    private final String label;

    private final String pluginIdentifier;

    /**
     * @param name
     *            identifier of item
     * @param label
     *            item label to display
     * @param pluginIdentifier
     *            plugin identifier of this item
     */
    public MenuItem(final String name, final String label, final String pluginIdentifier) {
        this.name = name;
        this.label = label;
        this.pluginIdentifier = pluginIdentifier;
    }

    /**
     * get identifier of item
     * 
     * @return identifier of item
     */
    public final String getName() {
        return name;
    }

    /**
     * get item label to display
     * 
     * @return item label to display
     */
    public final String getLabel() {
        return label;
    }

    /**
     * get plugin identifier of this item
     * 
     * @return plugin identifier of this item
     */
    public final String getPluginIdentifier() {
        return pluginIdentifier;
    }

    /**
     * get URL that this item leads to
     * 
     * @return URL that this item leads to
     */
    public abstract String getPage();

    /**
     * generates JSON representation of this item
     * 
     * @return JSON representation of this item
     * @throws JSONException
     */
    public final JSONObject getAsJson() throws JSONException {
        JSONObject itemObject = new JSONObject();
        itemObject.put("name", getName());
        itemObject.put("label", getLabel());
        itemObject.put("page", getPage());
        return itemObject;
    }

}
