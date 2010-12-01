/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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

package com.qcadoo.mes.view;

import java.util.Map;

/**
 * Object holds option for component, defined in view.xml in tags &lt;option /&gt;.
 * 
 * E.g. below option has type equals to "column" and four attributes: "name", "fields", "link" and "width".
 * 
 * &lt;option type="column" name="number" fields="number" link="true" width="200" /&gt
 */
public final class ComponentOption {

    private final String type;

    private final Map<String, String> attributes;

    /**
     * Create new option with given type and attributes.
     * 
     * @param type
     *            type
     * @param attributes
     *            attributes
     */
    public ComponentOption(final String type, final Map<String, String> attributes) {
        this.type = type;
        this.attributes = attributes;
    }

    /**
     * Return attribute's value.
     * 
     * @param name
     *            attribute's name
     * @return value
     */
    public String getAtrributeValue(final String name) {
        return attributes.get(name);
    }

    /**
     * Return option's type.
     * 
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Return option's value - if there is only one attribute, its value will be returned, otherwise value of the "value"
     * attribute is returned.
     * 
     * @return value
     */
    public String getValue() {
        if (attributes.size() == 1) {
            return attributes.values().toArray(new String[1])[0];
        } else {
            return attributes.get("value");
        }
    }

}
