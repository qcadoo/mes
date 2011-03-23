package com.qcadoo.mes.view.internal.module;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.internal.InternalMenuService;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class MenuCategoryModuleFactory implements ModuleFactory<MenuCategoryModule> {

    @Autowired
    private InternalMenuService menuService;

    @Override
    public void init() {
        // empty
    }

    @Override
    public MenuCategoryModule parse(final String pluginIdentifier, final Element element) {
        String menuCategoryName = element.getAttributeValue("name");

        if (menuCategoryName == null) {
            throw new IllegalStateException("Missing name attribute of menu-category module");
        }

        return new MenuCategoryModule(menuService, pluginIdentifier, menuCategoryName);
    }

    @Override
    public String getIdentifier() {
        return "menu-category";
    }

}
