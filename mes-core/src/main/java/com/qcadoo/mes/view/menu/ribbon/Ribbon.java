/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view.menu.ribbon;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents ribbon in application
 */
public final class Ribbon {

    private String name;

    private final List<RibbonGroup> groups = new LinkedList<RibbonGroup>();

    /**
     * get identifier of this ribbon
     * 
     * @return identifier of this ribbon
     */
    public String getName() {
        return name;
    }

    /**
     * set identifier of this ribbon
     * 
     * @param name
     *            identifier of this ribbon
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * get groups of this ribbon
     * 
     * @return groups of this ribbon
     */
    public List<RibbonGroup> getGroups() {
        return groups;
    }

    /**
     * add group to this ribbon
     * 
     * @param group
     *            group to add
     */
    public void addGroup(final RibbonGroup group) {
        this.groups.add(group);
    }

    /**
     * generates JSON string that contains all ribbon definition
     * 
     * @return JSON ribbon definition
     */
    public JSONObject getAsJson() {
        JSONObject ribbonJson = new JSONObject();
        try {
            ribbonJson.put("name", name);
            JSONArray groupsArray = new JSONArray();
            for (RibbonGroup group : groups) {
                groupsArray.put(group.getAsJson());
            }
            ribbonJson.put("groups", groupsArray);
            return ribbonJson;
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
