package com.qcadoo.mes.view.menu.ribbon;

import org.json.JSONException;
import org.json.JSONObject;

public final class RibbonActionItem extends RibbonItem {

    private String clickAction;

    public String getAction() {
        return clickAction;
    }

    public void setAction(final String clickAction) {
        this.clickAction = clickAction;
    }

    public JSONObject getAsJson() throws JSONException {
        JSONObject itemObject = super.getAsJson();
        itemObject.put("clickAction", clickAction);
        return itemObject;
    }
}
