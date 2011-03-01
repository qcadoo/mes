/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.view.ribbon;

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
     * get group by name of this ribbon
     * 
     * @return group or null when no group witch such name
     */
    public RibbonGroup getGroupByName(String groupName) {
        for (RibbonGroup group : groups) {
            if (group.getName().equals(groupName)) {
                return group;
            }
        }
        return null;
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

    public Ribbon getCopy() {
        Ribbon copy = new Ribbon();
        copy.setName(name);
        for (RibbonGroup group : groups) {
            copy.addGroup(group.getCopy());
        }
        return copy;
    }

    public Ribbon getUpdate() {
        Ribbon diff = new Ribbon();
        boolean isDiffrence = false;
        diff.setName(name);

        for (RibbonGroup group : groups) {
            RibbonGroup diffGroup = group.getUpdate();
            if (diffGroup != null) {
                diff.addGroup(diffGroup);
                isDiffrence = true;
            }

        }
        if (isDiffrence) {
            return diff;
        }
        return null;
    }
}
