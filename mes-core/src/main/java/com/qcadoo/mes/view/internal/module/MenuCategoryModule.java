package com.qcadoo.mes.view.internal.module;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class MenuCategoryModule implements Module {

    private final DataDefinitionService dataDefinitionService;

    private final String menuCategoryName;

    private final String pluginIdentifier;

    private Entity category;

    private final boolean canBeEnabled;

    public MenuCategoryModule(final DataDefinitionService dataDefinitionService, final String pluginIdentifier,
            final String menuCategoryName, final boolean canBeEnabled) {
        this.dataDefinitionService = dataDefinitionService;
        this.pluginIdentifier = pluginIdentifier;
        this.menuCategoryName = menuCategoryName;
        this.canBeEnabled = canBeEnabled;
    }

    @Override
    public void init(final PluginState state) {
        category = createCategory(PluginState.ENABLED.equals(state) && canBeEnabled);
    }

    @Override
    public void enable() {
        category.setField("active", canBeEnabled);
        category = dataDefinitionService.get("menu", "menuCategory").save(category);
    }

    @Override
    public void disable() {
        category.setField("active", false);
        category = dataDefinitionService.get("menu", "menuCategory").save(category);
    }

    private Entity createCategory(final boolean active) {
        Entity category = dataDefinitionService.get("menu", "menuCategory").create();
        category.setField("name", menuCategoryName);
        category.setField("active", active);
        category.setField("translationName", pluginIdentifier + ".menu." + menuCategoryName);
        category.setField("categoryOrder", getOrder());
        return dataDefinitionService.get("menu", "menuCategory").save(category);
    }

    private int getOrder() {
        return dataDefinitionService.get("menu", "menuCategory").find().list().getTotalNumberOfEntities();
    }

}
