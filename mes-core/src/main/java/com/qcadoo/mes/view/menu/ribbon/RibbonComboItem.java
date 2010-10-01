package com.qcadoo.mes.view.menu.ribbon;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class RibbonComboItem extends RibbonItem {

    private List<RibbonActionItem> items;

    public List<RibbonActionItem> getItems() {
        return items;
    }

    public void setItems(final List<RibbonActionItem> items) {
        this.items = items;
    }

    public JSONObject getAsJson() throws JSONException {
        JSONObject itemObject = super.getAsJson();
        JSONArray itemsArray = new JSONArray();
        for (RibbonActionItem item : items) {
            itemsArray.put(item.getAsJson());
        }
        itemObject.put("items", itemsArray);
        return itemObject;
    }

}
