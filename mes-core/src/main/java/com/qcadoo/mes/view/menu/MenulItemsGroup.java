package com.qcadoo.mes.view.menu;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class MenulItemsGroup {

    private final String name;

    private final String label;

    private final List<MenuItem> items;

    public MenulItemsGroup(final String name, final String label) {
        super();
        this.name = name;
        this.label = label;
        items = new LinkedList<MenuItem>();
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public void addItem(final MenuItem item) {
        items.add(item);
    }

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
