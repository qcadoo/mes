package com.qcadoo.mes.view.internal.module;

import com.qcadoo.mes.internal.InternalMenuService;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class MenuModule extends Module {

    private final InternalMenuService menuService;

    private final String menuName;

    private final String menuCategory;

    private final String menuUrl;

    private final String pluginIdentifier;

    private final String menuViewPluginIdentifier;

    private final String menuViewName;

    public MenuModule(final InternalMenuService menuService, final String pluginIdentifier, final String menuName,
            final String menuCategory, final String menuViewPluginIdentifier, final String menuViewName, final String menuUrl) {
        this.menuService = menuService;
        this.pluginIdentifier = pluginIdentifier;
        this.menuName = menuName;
        this.menuCategory = menuCategory;
        this.menuViewPluginIdentifier = menuViewPluginIdentifier;
        this.menuViewName = menuViewName;
        this.menuUrl = menuUrl;
    }

    @Override
    public void init(final PluginState state) {
        if (menuUrl != null) {
            menuService.createViewIfNotExists(pluginIdentifier, menuName, null, menuUrl);
            menuService.createItemIfNotExists(pluginIdentifier, pluginIdentifier + "." + menuName, menuCategory,
                    pluginIdentifier, menuName);
        } else {
            // TODO menu view be created in ViewModule
            menuService.createViewIfNotExists(menuViewPluginIdentifier, menuName, menuViewName, null);
            menuService.createItemIfNotExists(pluginIdentifier, pluginIdentifier + "." + menuName, menuCategory,
                    menuViewPluginIdentifier, menuViewName);
        }

    }

    @Override
    public void enable() {
        if (menuUrl != null) {
            menuService.enableView(pluginIdentifier, menuName);
        }
        menuService.enableItem(pluginIdentifier, menuName);
    }

    @Override
    public void disable() {
        if (menuUrl != null) {
            menuService.disableView(pluginIdentifier, menuName);
        }
        menuService.disableItem(pluginIdentifier, menuName);
    }
}
