package com.qcadoo.mes.view.ribbon;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents ribbon combo box
 */
public class RibbonComboBox extends RibbonActionItem {

    private List<String> options = new LinkedList<String>();

    public void addOption(String option) {
        options.add(option);
    }

    public JSONObject getAsJson() throws JSONException {
        JSONObject obj = super.getAsJson();
        JSONArray optionsArray = new JSONArray();
        for (String option : options) {
            optionsArray.put(option);
        }
        obj.put("options", optionsArray);
        return obj;
    }

    @Override
    public RibbonActionItem getCopy() {
        RibbonComboBox copy = new RibbonComboBox();
        copyFields(copy);
        for (String option : options) {
            copy.addOption(option);
        }
        return copy;
    }

}
