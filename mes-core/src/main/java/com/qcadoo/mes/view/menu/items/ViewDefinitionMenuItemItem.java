package com.qcadoo.mes.view.menu.items;

import com.qcadoo.mes.view.menu.MenuItem;

public class ViewDefinitionMenuItemItem extends MenuItem {

    private final String viewName;

    public ViewDefinitionMenuItemItem(String name, String label, String pluginIdentifier, String viewName) {
        super(name, label, pluginIdentifier);
        this.viewName = viewName;
    }

    @Override
    public String getPage() {
        return "page/" + getPluginIdentifier() + "/" + viewName + ".html";
    }

}
