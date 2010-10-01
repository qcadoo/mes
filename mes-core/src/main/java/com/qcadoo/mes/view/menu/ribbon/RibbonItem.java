package com.qcadoo.mes.view.menu.ribbon;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class RibbonItem {

    public static enum Type {
        BIG_BUTTON, SMALL_BUTTON, CHECKBOX, COMBOBOX
    }

    private String name;

    private Type type;

    private String icon;

    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final Type getType() {
        return type;
    }

    public final void setType(final Type type) {
        this.type = type;
    }

    public final String getIcon() {
        return icon;
    }

    public final void setIcon(final String icon) {
        this.icon = icon;
    }

    public JSONObject getAsJson() throws JSONException {
        JSONObject itemObject = new JSONObject();
        itemObject.put("name", name);
        itemObject.put("type", type);
        itemObject.put("icon", icon);
        return itemObject;
    }

}
