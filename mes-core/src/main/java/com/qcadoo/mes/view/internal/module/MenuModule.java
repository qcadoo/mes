package com.qcadoo.mes.view.internal.module;

import java.util.List;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class MenuModule extends Module {

    private final DataDefinitionService dataDefinitionService;

    private final String menuName;

    private final String menuCategory;

    private final String menuView;

    private final String menuUrl;

    private final String pluginIdentifier;

    public MenuModule(final DataDefinitionService dataDefinitionService, final String pluginIdentifier, final String menuName,
            final String menuCategory, final String menuView, final String menuUrl) {
        this.dataDefinitionService = dataDefinitionService;
        this.pluginIdentifier = pluginIdentifier;
        this.menuName = menuName;
        this.menuCategory = menuCategory;
        this.menuView = menuView;
        this.menuUrl = menuUrl;
    }

    @Override
    public void init(final PluginState state) {
        // empty
    }

    @Override
    public void enable() {
        Entity item = getOrCreateItem();
        item.setField("active", true);
        item = dataDefinitionService.get("menu", "menuViewDefinitionItem").save(item);
    }

    @Override
    public void disable() {
        Entity item = getOrCreateItem();
        item.setField("active", false);
        item = dataDefinitionService.get("menu", "menuViewDefinitionItem").save(item);
    }

    private Entity getOrCreateItem() {
        Entity category = getCategory();
        List<Entity> entities = dataDefinitionService
                .get("menu", "menuViewDefinitionItem")
                .find()
                .restrictedWith(Restrictions.eq("name", menuName))
                .restrictedWith(
                        Restrictions.belongsTo(
                                dataDefinitionService.get("menu", "menuViewDefinitionItem").getField("menuCategory"), category))
                .withMaxResults(1).list().getEntities();

        if (entities.size() == 1) {
            return entities.get(0);
        } else {
            Entity item = dataDefinitionService.get("menu", "menuViewDefinitionItem").create();
            item.setField("itemOrder", getOrder(category));
            item.setField("menuCategory", category);
            item.setField("name", menuName);
            item.setField("active", true);
            item.setField("translationName", pluginIdentifier + ".menu." + menuCategory + "." + menuName);
            item.setField("viewDefinition", createView());
            return dataDefinitionService.get("menu", "menuViewDefinitionItem").save(item);
        }
    }

    private Entity createView() {
        Entity view = dataDefinitionService.get("menu", "viewDefinition").create();
        view.setField("menuName", menuName);
        view.setField("pluginIdentifier", pluginIdentifier);
        if (menuView != null) {
            view.setField("viewName", menuView);
            view.setField("url", false);
        } else {
            view.setField("viewName", menuUrl);
            view.setField("url", true);
        }

        return dataDefinitionService.get("menu", "viewDefinition").save(view);
    }

    private Entity getCategory() {
        List<Entity> entities = dataDefinitionService.get("menu", "menuCategory").find()
                .restrictedWith(Restrictions.eq("name", menuCategory)).withMaxResults(1).list().getEntities();

        if (entities.size() == 1) {
            return entities.get(0);
        } else {
            throw new IllegalStateException("Cannot find category " + menuCategory);
        }
    }

    private Object getOrder(final Entity category) {
        return dataDefinitionService
                .get("menu", "menuViewDefinitionItem")
                .find()
                .restrictedWith(
                        Restrictions.belongsTo(
                                dataDefinitionService.get("menu", "menuViewDefinitionItem").getField("menuCategory"), category))
                .list().getTotalNumberOfEntities();
    }

}
