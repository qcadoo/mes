package com.qcadoo.mes.view.internal.module;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.internal.InternalMenuService;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class MenuUrlModuleFactory implements ModuleFactory<MenuModule> {

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
        String menuUrl = element.getAttributeValue("url");

        if (menuName == null) {
            throw new IllegalStateException("Missing name attribute of menu-item-url module");
        }

        if (menuCategory == null) {
            throw new IllegalStateException("Missing category attribute of menu-item-url module");
        }

        if (menuUrl == null) {
            throw new IllegalStateException("Missing url attribute of menu-item-url module");
        }

        return new MenuModule(menuService, pluginIdentifier, menuName, menuCategory, null, null, menuUrl);
    }

    @Override
    public String getIdentifier() {
        return "menu-item-url";
    }

}
