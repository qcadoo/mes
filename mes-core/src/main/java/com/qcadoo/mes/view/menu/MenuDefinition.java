package com.qcadoo.mes.view.menu;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MenuDefinition {

    private final List<FirstLevelItem> items;

    public MenuDefinition() {
        items = new LinkedList<FirstLevelItem>();
    }

    public List<FirstLevelItem> getItems() {
        return items;
    }

    public void addItem(FirstLevelItem item) {
        items.add(item);
    }

    public String getAsJson() {
        try {
            JSONArray menuItems = new JSONArray();
            for (FirstLevelItem item : items) {
                menuItems.put(item.getAsJson());
            }
            JSONObject menuStructure = new JSONObject();
            menuStructure.put("menuItems", menuItems);
            return menuStructure.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
