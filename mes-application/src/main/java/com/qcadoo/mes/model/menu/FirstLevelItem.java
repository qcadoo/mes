package com.qcadoo.mes.model.menu;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FirstLevelItem {

    private final String name;

    private final String label;

    private final List<SecondLevelItem> items;

    public FirstLevelItem(String name, String label) {
        super();
        this.name = name;
        this.label = label;
        items = new LinkedList<SecondLevelItem>();
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public List<SecondLevelItem> getItems() {
        return items;
    }

    public void addItem(SecondLevelItem item) {
        items.add(item);
    }

    public JSONObject getAsJson() throws JSONException {
        JSONObject itemObject = new JSONObject();
        itemObject.put("name", name);
        itemObject.put("label", label);
        JSONArray itemsArray = new JSONArray();
        for (SecondLevelItem item : items) {
            itemsArray.put(item.getAsJson());
        }
        itemObject.put("items", itemsArray);
        return itemObject;
    }
}
