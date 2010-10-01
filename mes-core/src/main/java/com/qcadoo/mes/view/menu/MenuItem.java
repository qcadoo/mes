package com.qcadoo.mes.view.menu;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class MenuItem {

    private final String name;

    private final String label;

    private final String pluginIdentifier;

    public MenuItem(String name, String label, String pluginIdentifier) {
        this.name = name;
        this.label = label;
        this.pluginIdentifier = pluginIdentifier;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getPluginIdentifier() {
        return pluginIdentifier;
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
