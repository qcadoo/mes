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

package com.qcadoo.mes.view.components.grid;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.model.api.utils.DateUtils;

public class PredefinedFilter {

    private String name;

    private Map<String, String> filterRestrictions = new HashMap<String, String>();

    private String orderColumn;

    private String orderDirection;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getFilterRestrictions() {
        return filterRestrictions;
    }

    public void addFilterRestriction(String column, String restriction) {
        filterRestrictions.put(column, restriction);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("label", name);
        object.put("orderColumn", orderColumn);
        object.put("orderDirection", orderDirection);
        JSONObject filterRestrictionsObject = new JSONObject();
        for (Entry<String, String> filterRestriction : filterRestrictions.entrySet()) {
            filterRestrictionsObject.put(filterRestriction.getKey(), parseRestriction(filterRestriction.getValue()));
        }
        object.put("filter", filterRestrictionsObject);
        return object;
    }

    private String parseRestriction(String restriction) {
        Pattern p = Pattern.compile("@\\{.*?\\}");
        Matcher m = p.matcher(restriction);
        int lastEnd = 0;
        StringBuilder result = new StringBuilder();
        while (m.find()) {
            String expression = restriction.substring(m.start() + 2, m.end() - 1);
            result.append(restriction.substring(lastEnd, m.start()));
            result.append(evalExpression(expression));
            lastEnd = m.end();
        }
        if (lastEnd > 0) {
            return result.toString();
        } else {
            return restriction;
        }
    }

    private String evalExpression(String expression) {
        DateTime today = new DateTime();
        DateTime date;
        if ("today".equals(expression)) {
            date = today;
        } else if ("yesterday".equals(expression)) {
            date = today.minusDays(1);
        } else if ("tomorrow".equals(expression)) {
            date = today.plusDays(1);
        } else {
            throw new IllegalStateException("unsupported predefined filter expression: '" + expression + "'");
        }
        return new SimpleDateFormat(DateUtils.DATE_FORMAT).format(date.toDate());
    }

    public String getOrderColumn() {
        return orderColumn;
    }

    public void setOrderColumn(String orderColumn) {
        this.orderColumn = orderColumn;
    }

    public String getOrderDirection() {
        return orderDirection;
    }

    public void setOrderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
    }
}
