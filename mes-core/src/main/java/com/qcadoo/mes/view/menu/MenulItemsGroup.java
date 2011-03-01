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

package com.qcadoo.mes.view.menu;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents menu items group
 * 
 */
public final class MenulItemsGroup {

    private final String name;

    private final String label;

    private final List<MenuItem> items;

    /**
     * 
     * @param name
     *            identifier of group
     * @param label
     *            group label to display
     */
    public MenulItemsGroup(final String name, final String label) {
        super();
        this.name = name;
        this.label = label;
        items = new LinkedList<MenuItem>();
    }

    /**
     * get identifier of group
     * 
     * @return identifier of group
     */
    public String getName() {
        return name;
    }

    /**
     * get group label to display
     * 
     * @return group label to display
     */
    public String getLabel() {
        return label;
    }

    /**
     * get list of all items of group
     * 
     * @return list of all items of group
     */
    public List<MenuItem> getItems() {
        return items;
    }

    /**
     * add item to menu group
     * 
     * @param item
     *            item to add
     */
    public void addItem(final MenuItem item) {
        items.add(item);
    }

    /**
     * generates JSON representation of this menu group
     * 
     * @return JSON group representation
     * @throws JSONException
     */
    public JSONObject getAsJson() throws JSONException {
        JSONObject itemObject = new JSONObject();
        itemObject.put("name", name);
        itemObject.put("label", label);
        JSONArray itemsArray = new JSONArray();
        for (MenuItem item : items) {
            itemsArray.put(item.getAsJson());
        }
        itemObject.put("items", itemsArray);
        return itemObject;
    }
}
