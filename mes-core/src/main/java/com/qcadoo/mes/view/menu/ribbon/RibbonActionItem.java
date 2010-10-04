package com.qcadoo.mes.view.menu.ribbon;

import org.json.JSONException;
import org.json.JSONObject;

public class RibbonActionItem {

    public static enum Type {
        BIG_BUTTON, SMALL_BUTTON, CHECKBOX, COMBOBOX
    }

    private String name;

    private Type type;

    private String icon;

    private String clickAction;

    public final String getAction() {
        return clickAction;
    }

    public final void setAction(final String clickAction) {
        this.clickAction = clickAction;
    }

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
        itemObject.put("clickAction", clickAction);
        return itemObject;
    }

}
