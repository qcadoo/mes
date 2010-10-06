package com.qcadoo.mes.view.menu.items;

import com.qcadoo.mes.view.menu.MenuItem;

public final class ViewDefinitionMenuItemItem extends MenuItem {

    private final String viewName;

    public ViewDefinitionMenuItemItem(final String name, final String label, final String pluginIdentifier, final String viewName) {
        super(name, label, pluginIdentifier);
        this.viewName = viewName;
    }

    @Override
    public String getPage() {
        return "page/" + getPluginIdentifier() + "/" + viewName + ".html";
    }

}
