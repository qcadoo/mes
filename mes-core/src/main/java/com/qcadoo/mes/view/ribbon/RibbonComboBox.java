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
