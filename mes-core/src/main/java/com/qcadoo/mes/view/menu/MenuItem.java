/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view.menu;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents single menu item
 */
public abstract class MenuItem {

    private final String name;

    private final String label;

    private final String pluginIdentifier;

    /**
     * @param name
     *            identifier of item
     * @param label
     *            item label to display
     * @param pluginIdentifier
     *            plugin identifier of this item
     */
    public MenuItem(final String name, final String label, final String pluginIdentifier) {
        this.name = name;
        this.label = label;
        this.pluginIdentifier = pluginIdentifier;
    }

    /**
     * get identifier of item
     * 
     * @return identifier of item
     */
    public final String getName() {
        return name;
    }

    /**
     * get item label to display
     * 
     * @return item label to display
     */
    public final String getLabel() {
        return label;
    }

    /**
     * get plugin identifier of this item
     * 
     * @return plugin identifier of this item
     */
    public final String getPluginIdentifier() {
        return pluginIdentifier;
    }

    /**
     * get URL that this item leads to
     * 
     * @return URL that this item leads to
     */
    public abstract String getPage();

    /**
     * generates JSON representation of this item
     * 
     * @return JSON representation of this item
     * @throws JSONException
     */
    public final JSONObject getAsJson() throws JSONException {
        JSONObject itemObject = new JSONObject();
        itemObject.put("name", getName());
        itemObject.put("label", getLabel());
        itemObject.put("page", getPage());
        return itemObject;
    }

}
