/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view.menu.items;

import com.qcadoo.mes.view.menu.MenuItem;

/**
 * Menu item that leads to specified URL.
 */
public final class UrlMenuItem extends MenuItem {

    private final String pageUrl;

    /**
     * @param name
     *            identifier of item
     * @param label
     *            item label to display
     * @param pluginIdentifier
     *            plugin identifier of this item
     * @param pageUrl
     *            URL of page that this item leads to
     */
    public UrlMenuItem(final String name, final String label, final String pluginIdentifier, final String pageUrl) {
        super(name, label, pluginIdentifier);
        this.pageUrl = pageUrl;
    }

    @Override
    public String getPage() {
        return pageUrl;
    }
}
