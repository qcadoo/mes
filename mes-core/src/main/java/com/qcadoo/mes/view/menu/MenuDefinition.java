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
 * Represents top menu in application
 */
public final class MenuDefinition {

    private final List<MenulItemsGroup> items;

    public MenuDefinition() {
        items = new LinkedList<MenulItemsGroup>();
    }

    /**
     * get all menu groups
     * 
     * @return menu groups
     */
    public List<MenulItemsGroup> getItems() {
        return items;
    }

    /**
     * add group to menu
     * 
     * @param item
     */
    public void addItem(final MenulItemsGroup item) {
        items.add(item);
    }

    /**
     * generates JSON string that contains all menu definition
     * 
     * @return JSON menu definition
     */
    public String getAsJson() {
        try {
            JSONArray menuItems = new JSONArray();
            for (MenulItemsGroup item : items) {
                menuItems.put(item.getAsJson());
            }
            JSONObject menuStructure = new JSONObject();
            menuStructure.put("menuItems", menuItems);
            return menuStructure.toString();
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
