package com.qcadoo.mes.view.internal.module;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class MenuCategoryModuleFactory implements ModuleFactory<MenuCategoryModule> {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Value("${showAdministrationMenu}")
    private boolean showAdministrationMenu;

    @Override
    public void init() {
        // empty
    }

    @Override
    public MenuCategoryModule parse(final String pluginIdentifier, final Element element) {
        String menuCategoryName = element.getAttributeValue("name");
        String admin = element.getAttributeValue("admin");

        if (menuCategoryName == null) {
            throw new IllegalStateException("Missing name attribute of menu-category module");
        }

        return new MenuCategoryModule(dataDefinitionService.get("menu", "menuCategory"), pluginIdentifier, menuCategoryName,
                "true".equals(admin), showAdministrationMenu);
    }

    @Override
    public String getIdentifier() {
        return "menu-category";
    }

}
