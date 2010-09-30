package com.qcadoo.mes.view.menu;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class SecondLevelItem {

    private final String name;

    private final String label;

    public SecondLevelItem(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public abstract String getPage();

    public JSONObject getAsJson() throws JSONException {
        JSONObject itemObject = new JSONObject();
        itemObject.put("name", getName());
        itemObject.put("label", getLabel());
        itemObject.put("page", getPage());
        return itemObject;
    }

}
