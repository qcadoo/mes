package com.qcadoo.mes.view.internal.module;

import com.qcadoo.mes.internal.MenuService;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class MenuModule implements Module {

    private final MenuService menuService;

    public MenuModule(final MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    public void init(final PluginState state) {
        menuService.init();
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {
        // TODO Auto-generated method stub
    }

}
