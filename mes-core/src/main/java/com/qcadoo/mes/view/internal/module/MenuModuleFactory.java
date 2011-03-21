package com.qcadoo.mes.view.internal.module;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class MenuModuleFactory implements ModuleFactory<MenuModule> {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void init() {
        // empty
    }

    @Override
    public MenuModule parse(final String pluginIdentifier, final Element element) {
        String menuName = element.getAttributeValue("name");
        String menuCategory = element.getAttributeValue("category");
        String menuView = element.getAttributeValue("view");
        String menuUrl = element.getAttributeValue("url");

        if (menuName == null) {
            throw new IllegalStateException("Missing name attribute of menu-item module");
        }

        if (menuCategory == null) {
            throw new IllegalStateException("Missing category attribute of menu-item module");
        }

        if (menuView == null || menuUrl == null) {
            throw new IllegalStateException("Missing view and url attribute of menu-item module");
        }

        if (menuView != null && menuUrl != null) {
            throw new IllegalStateException("Cannot define both view and url attribute for menu-item module");
        }

        return new MenuModule(dataDefinitionService, pluginIdentifier, menuName, menuCategory, menuView, menuUrl);
    }

    @Override
    public String getIdentifier() {
        return "menu-item";
    }

}
