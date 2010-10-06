package com.qcadoo.mes.view.menu.items;

import com.qcadoo.mes.view.menu.MenuItem;

public final class UrlMenuItem extends MenuItem {

    private final String pageUrl;

    public UrlMenuItem(final String name, final String label, final String pluginIdentifier, final String pageUrl) {
        super(name, label, pluginIdentifier);
        this.pageUrl = pageUrl;
    }

    @Override
    public String getPage() {
        return pageUrl;
    }
}
