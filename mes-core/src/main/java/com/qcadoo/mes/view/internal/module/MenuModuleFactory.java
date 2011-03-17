package com.qcadoo.mes.view.internal.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Node;

import com.qcadoo.mes.internal.MenuService;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class MenuModuleFactory implements ModuleFactory<MenuModule> {

    @Autowired
    private MenuService menuService;

    @Override
    public void postInitialize() {
        menuService.init();
    }

    @Override
    public MenuModule parse(final String pluginIdentifier, final Node node) {
        // TODO Auto-generated method stub
        return new MenuModule();
    }

    @Override
    public String getIdentifier() {
        return "menu";
    }

}
