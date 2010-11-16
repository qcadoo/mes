/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
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
