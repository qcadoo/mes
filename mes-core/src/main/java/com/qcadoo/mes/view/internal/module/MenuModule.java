package com.qcadoo.mes.view.internal.module;

import java.util.List;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class MenuModule implements Module {

    private final DataDefinition viewDataDefinition;

    private final DataDefinition categoryDataDefinition;

    private final DataDefinition itemDataDefinition;

    private final String menuName;

    private final String menuCategory;

    private final String menuView;

    private final String menuUrl;

    private final String pluginIdentifier;

    private Entity item;

    public MenuModule(final DataDefinitionService dataDefinitionService, final String pluginIdentifier, final String menuName,
            final String menuCategory, final String menuView, final String menuUrl) {
        this.pluginIdentifier = pluginIdentifier;
        this.menuName = menuName;
        this.menuCategory = menuCategory;
        this.menuView = menuView;
        this.menuUrl = menuUrl;
        this.viewDataDefinition = dataDefinitionService.get("menu", "viewDefinition");
        this.categoryDataDefinition = dataDefinitionService.get("menu", "menuCategory");
        this.itemDataDefinition = dataDefinitionService.get("menu", "menuViewDefinitionItem");
    }

    @Override
    public void init(final PluginState state) {
        Entity view = viewDataDefinition.create();
        view.setField("menuName", menuName);
        view.setField("pluginIdentifier", pluginIdentifier);
        if (menuView != null) {
            view.setField("viewName", menuView);
            view.setField("url", false);
        } else {
            view.setField("viewName", menuUrl);
            view.setField("url", true);
        }

        viewDataDefinition.save(view);

        item = createItem(view);
    }

    @Override
    public void enable() {
        item.setField("active", true);
        item = itemDataDefinition.save(item);
    }

    @Override
    public void disable() {
        item.setField("active", false);
        item = itemDataDefinition.save(item);
    }

    private Entity createItem(final Entity view) {
        Entity category = getCategory();
        Entity item = itemDataDefinition.create();
        item.setField("itemOrder", getOrder(category));
        item.setField("menuCategory", category);
        item.setField("name", menuName);
        item.setField("active", true);
        item.setField("translationName", pluginIdentifier + ".menu." + menuCategory + "." + menuName);
        item.setField("viewDefinition", view);
        return itemDataDefinition.save(item);
    }

    private Entity getCategory() {
        List<Entity> entities = categoryDataDefinition.find().restrictedWith(Restrictions.eq("name", menuCategory))
                .withMaxResults(1).list().getEntities();

        if (entities.size() == 1) {
            return entities.get(0);
        } else {
            throw new IllegalStateException("Cannot find category " + menuCategory);
        }
    }

    private Object getOrder(final Entity category) {
        return itemDataDefinition.find()
                .restrictedWith(Restrictions.belongsTo(itemDataDefinition.getField("menuCategory"), category)).list()
                .getTotalNumberOfEntities();
    }

}
