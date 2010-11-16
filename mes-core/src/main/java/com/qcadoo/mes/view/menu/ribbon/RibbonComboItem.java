/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view.menu.ribbon;

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

}
