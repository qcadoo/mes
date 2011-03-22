package com.qcadoo.mes.view.internal.module;

import java.util.List;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class MenuCategoryModule implements Module {

    private final DataDefinitionService dataDefinitionService;

    private final String menuCategoryName;

    private final String pluginIdentifier;

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
        // empty
    }

    @Override
    public void enable() {
        Entity category = getOrCreateCategory();
        category.setField("active", canBeEnabled);
        category = dataDefinitionService.get("menu", "menuCategory").save(category);
    }

    @Override
    public void disable() {
        Entity category = getOrCreateCategory();
        category.setField("active", false);
        category = dataDefinitionService.get("menu", "menuCategory").save(category);
    }

    private Entity getOrCreateCategory() {
        List<Entity> entities = dataDefinitionService.get("menu", "menuCategory").find()
                .restrictedWith(Restrictions.eq("name", menuCategoryName)).withMaxResults(1).list().getEntities();

        if (entities.size() == 1) {
            return entities.get(0);
        } else {
            Entity category = dataDefinitionService.get("menu", "menuCategory").create();
            category.setField("name", menuCategoryName);
            category.setField("translationName", pluginIdentifier + ".menu." + menuCategoryName);
            category.setField("categoryOrder", getOrder());
            return category;
        }
    }

    private int getOrder() {
        return dataDefinitionService.get("menu", "menuCategory").find().list().getTotalNumberOfEntities();
    }

}
