package com.qcadoo.mes.view.internal.module;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class MenuCategoryModule implements Module {

    private final DataDefinition dataDefinition;

    private final String menuCategoryName;

    private final String pluginIdentifier;

    private final boolean isAdministrationMenu;

    private final boolean showAdministrationMenu;

    private Entity category;

    public MenuCategoryModule(final DataDefinition dataDefinition, final String pluginIdentifier, final String menuCategoryName,
            final boolean isAdministrationMenu, final boolean showAdministrationMenu) {
        this.dataDefinition = dataDefinition;
        this.pluginIdentifier = pluginIdentifier;
        this.menuCategoryName = menuCategoryName;
        this.isAdministrationMenu = isAdministrationMenu;
        this.showAdministrationMenu = showAdministrationMenu;
    }

    @Override
    public void init(final PluginState state) {
        category = createCategory();
    }

    @Override
    public void enable() {
        category.setField("active", canBeActive());
        category = dataDefinition.save(category);
    }

    @Override
    public void disable() {
        category.setField("active", false);
        category = dataDefinition.save(category);
    }

    private Entity createCategory() {
        Entity category = dataDefinition.create();
        category.setField("name", menuCategoryName);
        category.setField("active", canBeActive());
        category.setField("translationName", pluginIdentifier + ".menu." + menuCategoryName);
        category.setField("categoryOrder", getOrder());
        return dataDefinition.save(category);
    }

    private int getOrder() {
        return dataDefinition.find().list().getTotalNumberOfEntities();
    }

    private boolean canBeActive() {
        return showAdministrationMenu || !isAdministrationMenu;
    }

}
