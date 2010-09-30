package com.qcadoo.mes.model.menu;

import org.json.JSONException;
import org.json.JSONObject;

public class SecondLevelItem {

    private final String name;

    private final String label;

    private final String page;

    public SecondLevelItem(String name, String label, String page) {
        super();
        this.name = name;
        this.label = label;
        this.page = page;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getPage() {
        return page;
    }

    public JSONObject getAsJson() throws JSONException {
        JSONObject itemObject = new JSONObject();
        itemObject.put("name", name);
        itemObject.put("label", label);
        itemObject.put("page", page);
        return itemObject;
    }
}
