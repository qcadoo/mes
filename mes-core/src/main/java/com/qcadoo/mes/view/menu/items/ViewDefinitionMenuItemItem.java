/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view.menu.items;

import com.qcadoo.mes.view.menu.MenuItem;

/**
 * Menu item that leads to specified ViewDefinition.
 * 
 * @see com.qcadoo.mes.view.ViewDefinition
 */
public final class ViewDefinitionMenuItemItem extends MenuItem {

    private final String viewName;

    /**
     * @param name
     *            identifier of item
     * @param label
     *            item label to display
     * @param pluginIdentifier
     *            plugin identifier of this item
     * @param viewName
     *            name of view that this item leads to
     */
    public ViewDefinitionMenuItemItem(final String name, final String label, final String pluginIdentifier, final String viewName) {
        super(name, label, pluginIdentifier);
        this.viewName = viewName;
    }

    @Override
    public String getPage() {
        return "page/" + getPluginIdentifier() + "/" + viewName + ".html";
    }

}
