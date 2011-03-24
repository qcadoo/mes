package com.qcadoo.mes.view.internal.module;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.internal.InternalMenuService;
import com.qcadoo.plugin.api.ModuleFactory;

public class MenuViewModuleFactory implements ModuleFactory<MenuModule> {

    @Autowired
    private InternalMenuService menuService;

    @Override
    public void init() {
        // empty
    }

    @Override
    public MenuModule parse(final String pluginIdentifier, final Element element) {
        String menuName = element.getAttributeValue("name");
        String menuCategory = element.getAttributeValue("category");
        String menuViewName = element.getAttributeValue("view");

        if (menuName == null) {
            throw new IllegalStateException("Missing name attribute of menu-item module");
        }

        if (menuCategory == null) {
            throw new IllegalStateException("Missing category attribute of menu-item module");
        }

        if (menuViewName == null) {
            throw new IllegalStateException("Missing view attribute of menu-item module");
        }

        return new MenuModule(menuService, pluginIdentifier, menuName, menuCategory, pluginIdentifier, menuViewName, null);
    }

    @Override
    public String getIdentifier() {
        return "menu-item";
    }

}
