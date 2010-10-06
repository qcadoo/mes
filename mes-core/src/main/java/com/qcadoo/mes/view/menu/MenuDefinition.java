package com.qcadoo.mes.view.menu;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class MenuDefinition {

    private final List<MenulItemsGroup> items;

    public MenuDefinition() {
        items = new LinkedList<MenulItemsGroup>();
    }

    public List<MenulItemsGroup> getItems() {
        return items;
    }

    public void addItem(final MenulItemsGroup item) {
        items.add(item);
    }

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
