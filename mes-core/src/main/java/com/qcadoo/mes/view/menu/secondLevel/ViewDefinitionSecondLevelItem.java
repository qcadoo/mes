package com.qcadoo.mes.view.menu.secondLevel;

import com.qcadoo.mes.view.menu.SecondLevelItem;

public class ViewDefinitionSecondLevelItem extends SecondLevelItem {

    private final String pluginIdentifier;

    private final String viewName;

    public ViewDefinitionSecondLevelItem(String name, String label, String pluginIdentifier, String viewName) {
        super(name, label);
        this.pluginIdentifier = pluginIdentifier;
        this.viewName = viewName;
    }

    @Override
    public String getPage() {
        return "page/" + pluginIdentifier + "/" + viewName + ".html";
    }

}
