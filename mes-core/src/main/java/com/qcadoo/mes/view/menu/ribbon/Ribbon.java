package com.qcadoo.mes.view.menu.ribbon;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class Ribbon {

    private String name;

    private final List<RibbonGroup> groups = new LinkedList<RibbonGroup>();

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<RibbonGroup> getGroups() {
        return groups;
    }

    public void addGroup(final RibbonGroup group) {
        groups.add(group);
    }

    public String getAsJson() {
        JSONObject ribbonJson = new JSONObject();
        try {
            ribbonJson.put("name", name);
            JSONArray groupsArray = new JSONArray();
            for (RibbonGroup group : groups) {
                groupsArray.put(group.getAsJson());
            }
            ribbonJson.put("groups", groupsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ribbonJson.toString();
    }
}
