package com.qcadoo.mes.view.menu.ribbon;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class RibbonComboItem extends RibbonItem {

    private final List<RibbonActionItem> items = new LinkedList<RibbonActionItem>();

    public List<RibbonActionItem> getItems() {
        return items;
    }

    public void addItem(final RibbonActionItem item) {
        items.add(item);
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
