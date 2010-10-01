package com.qcadoo.mes.view.menu.items;

import com.qcadoo.mes.view.menu.MenuItem;

public class UrlMenuItem extends MenuItem {

    private final String pageUrl;

    public UrlMenuItem(String name, String label, String pluginIdentifier, String pageUrl) {
        super(name, label, pluginIdentifier);
        this.pageUrl = pageUrl;
    }

    @Override
    public String getPage() {
        return pageUrl;
    }
}
