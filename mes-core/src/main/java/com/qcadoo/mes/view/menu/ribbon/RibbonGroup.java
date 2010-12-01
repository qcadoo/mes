/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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

package com.qcadoo.mes.view.menu.ribbon;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents ribbon items group
 */
public final class RibbonGroup {

    private String name;

    private final List<RibbonActionItem> items = new LinkedList<RibbonActionItem>();;

    /**
     * get identifier of this ribbon group
     * 
     * @return identifier of this ribbon group
     */
    public String getName() {
        return name;
    }

    /**
     * set identifier of this ribbon group
     * 
     * @param name
     *            identifier of this ribbon group
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * get items of this group
     * 
     * @return items of this group
     */
    public List<RibbonActionItem> getItems() {
        return items;
    }

    /**
     * add item to this group
     * 
     * @param item
     *            item to add
     */
    public void addItem(final RibbonActionItem item) {
        items.add(item);
    }

    /**
     * generates JSON representation of this ribbon group
     * 
     * @return JSON representation of this ribbon group
     * @throws JSONException
     */
    public JSONObject getAsJson() throws JSONException {
        JSONObject groupObject = new JSONObject();
        groupObject.put("name", name);
        JSONArray itemsArray = new JSONArray();
        for (RibbonActionItem item : items) {
            itemsArray.put(item.getAsJson());
        }
        groupObject.put("items", itemsArray);
        return groupObject;
    }

}
