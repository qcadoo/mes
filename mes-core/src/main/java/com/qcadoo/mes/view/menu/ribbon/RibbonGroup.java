package com.qcadoo.mes.view.menu.ribbon;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class RibbonGroup {

    private String name;

    private final List<RibbonItem> items = new ArrayList<RibbonItem>();

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<RibbonItem> getItems() {
        return items;
    }

    public void addItems(final RibbonItem item) {
        this.items.add(item);
    }

    public JSONObject getAsJson() throws JSONException {
        JSONObject groupObject = new JSONObject();
        groupObject.put("name", name);
        JSONArray itemsArray = new JSONArray();
        for (RibbonItem item : items) {
            itemsArray.put(item.getAsJson());
        }
        groupObject.put("items", itemsArray);
        return groupObject;
    }

}
