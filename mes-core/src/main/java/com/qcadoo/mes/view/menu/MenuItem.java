package com.qcadoo.mes.view.menu;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class MenuItem {

    private final String name;

    private final String label;

    private final String pluginIdentifier;

    public MenuItem(final String name, final String label, final String pluginIdentifier) {
        this.name = name;
        this.label = label;
        this.pluginIdentifier = pluginIdentifier;
    }

    public final String getName() {
        return name;
    }

    public final String getLabel() {
        return label;
    }

    public final String getPluginIdentifier() {
        return pluginIdentifier;
    }

    public abstract String getPage();

    public final JSONObject getAsJson() throws JSONException {
        JSONObject itemObject = new JSONObject();
        itemObject.put("name", getName());
        itemObject.put("label", getLabel());
        itemObject.put("page", getPage());
        return itemObject;
    }

}
