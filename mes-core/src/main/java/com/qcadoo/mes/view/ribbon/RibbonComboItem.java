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

package com.qcadoo.mes.view.ribbon;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents ribbon item that contains dropdown submenu with other items
 */
public final class RibbonComboItem extends RibbonActionItem {

    private final List<RibbonActionItem> items = new LinkedList<RibbonActionItem>();

    /**
     * get list of dropdown items
     * 
     * @return list of dropdown items
     */
    public List<RibbonActionItem> getItems() {
        return items;
    }

    /**
     * add dropdown item
     * 
     * @param item
     *            dropdown item
     */
    public void addItem(final RibbonActionItem item) {
        items.add(item);
    }

    @Override
    public JSONObject getAsJson() throws JSONException {
        JSONObject itemObject = super.getAsJson();
        JSONArray itemsArray = new JSONArray();
        for (RibbonActionItem item : items) {
            itemsArray.put(item.getAsJson());
        }
        itemObject.put("items", itemsArray);
        return itemObject;
    }

    @Override
    public RibbonComboItem getCopy() {
        RibbonComboItem copy = new RibbonComboItem();
        copyFields(copy);
        for (RibbonActionItem item : items) {
            copy.addItem(item.getCopy());
        }
        return copy;
    }

}
