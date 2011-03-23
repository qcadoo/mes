package com.qcadoo.mes.view.internal.module;

import com.qcadoo.mes.internal.InternalMenuService;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.api.Module;

public class MenuCategoryModule extends Module {

    private final InternalMenuService menuService;

    private final String menuCategoryName;

    private final String pluginIdentifier;

    public MenuCategoryModule(final InternalMenuService menuService, final String pluginIdentifier, final String menuCategoryName) {
        this.menuService = menuService;
        this.pluginIdentifier = pluginIdentifier;
        this.menuCategoryName = menuCategoryName;
    }

    @Override
    public void init(final PluginState state) {
        menuService.createCategoryIfNotExists(pluginIdentifier, menuCategoryName);
    }

    @Override
    public void enable() {
        // empty
    }

    @Override
    public void disable() {
        // empty
    }

}
