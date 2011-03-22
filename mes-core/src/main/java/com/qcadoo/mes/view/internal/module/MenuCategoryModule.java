package com.qcadoo.mes.view.internal.module;

import com.qcadoo.mes.internal.InternalMenuService;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class MenuCategoryModule extends Module {

    private final InternalMenuService menuService;

    private final String menuCategoryName;

    private final boolean canBeEnabled;

    private final String pluginIdentifier;

    public MenuCategoryModule(final InternalMenuService menuService, final String pluginIdentifier,
            final String menuCategoryName, final boolean canBeEnabled) {
        this.menuService = menuService;
        this.pluginIdentifier = pluginIdentifier;
        this.menuCategoryName = menuCategoryName;
        this.canBeEnabled = canBeEnabled;
    }

    @Override
    public void init(final PluginState state) {
        menuService.createCategoryIfNotExists(pluginIdentifier, menuCategoryName);
    }

    @Override
    public void enable() {
        if (canBeEnabled) {
            menuService.enableCategory(pluginIdentifier, menuCategoryName);
        } else {
            menuService.disableCategory(pluginIdentifier, menuCategoryName);
        }
    }

    @Override
    public void disable() {
        menuService.disableCategory(pluginIdentifier, menuCategoryName);
    }

}
