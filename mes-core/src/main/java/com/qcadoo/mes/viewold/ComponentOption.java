/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold;

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
