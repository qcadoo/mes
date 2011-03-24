/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.internal;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.view.menu.MenuDefinition;
import com.qcadoo.mes.view.menu.MenulItemsGroup;
import com.qcadoo.mes.view.menu.items.UrlMenuItem;
import com.qcadoo.mes.view.menu.items.ViewDefinitionMenuItemItem;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.aop.Monitorable;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.plugin.api.PluginAccessor;

@Service
public final class MenuServiceImpl implements InternalMenuService {

    @Autowired
    private PluginAccessor pluginAccessor;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Value("${showAdministrationMenu}")
    private boolean showAdministrationMenu;

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public MenuDefinition getMenu(final Locale locale) {

        MenuDefinition menuDefinition = new MenuDefinition();

        List<Entity> menuCategories = dataDefinitionService.get("menu", "category").find().orderAscBy("succession").list()
                .getEntities();

        MenulItemsGroup administrationCategory = null;

        boolean hasMenuManagement = false;

        for (Entity menuCategory : menuCategories) {
            String label = menuCategory.getStringField("name");

            if (menuCategory.getStringField("pluginIdentifier") != null) {
                label = translationService.translate(
                        menuCategory.getStringField("pluginIdentifier") + ".menu." + menuCategory.getStringField("name"), locale);
            }

            MenulItemsGroup category = new MenulItemsGroup(menuCategory.getStringField("name"), label);

            List<Entity> menuItems = dataDefinitionService
                    .get("menu", "item")
                    .find()
                    .restrictedWith(
                            Restrictions.belongsTo(dataDefinitionService.get("menu", "item").getField("category"), menuCategory))
                    .orderAscBy("succession").list().getEntities();

            for (Entity menuItem : menuItems) {
                if (!(Boolean) menuItem.getField("active")) {
                    continue;
                }

                Entity menuView = menuItem.getBelongsToField("view");

                String itemLabel = menuItem.getStringField("name");

                if (menuItem.getStringField("pluginIdentifier") != null) {
                    itemLabel = translationService.translate(menuItem.getStringField("pluginIdentifier") + ".menu."
                            + menuCategory.getStringField("name") + "." + menuItem.getStringField("name"), locale);
                }

                if (menuView.getStringField("url") != null) {
                    category.addItem(new UrlMenuItem(menuItem.getStringField("name"), itemLabel, null, menuView
                            .getStringField("url")));
                } else if (belongsToActivePlugin(menuView.getStringField("pluginIdentifier"))) {
                    category.addItem(new ViewDefinitionMenuItemItem(menuItem.getStringField("name"), itemLabel, menuView
                            .getStringField("pluginIdentifier"), menuView.getStringField("name")));
                }

                if ("menu".equals(menuView.getStringField("pluginIdentifier"))
                        && "menuCategories".equals(menuView.getStringField("view"))) {
                    hasMenuManagement = true;
                }
            }

            if ("administration".equals(menuCategory.getStringField("name"))) {
                administrationCategory = category;
            } else if (!category.getItems().isEmpty()) {
                menuDefinition.addItem(category);
            }
        }

        if (showAdministrationMenu) {
            if (!hasMenuManagement) {
                if (administrationCategory == null) {
                    administrationCategory = new MenulItemsGroup("administration", translationService.translate(
                            "basic.menu.administration", locale));
                }
                administrationCategory.addItem(new ViewDefinitionMenuItemItem("menuCategories", translationService.translate(
                        "menu.menu.administration.menu", locale), "menu", "menuCategories"));
            }

            if (administrationCategory != null) {
                menuDefinition.addItem(administrationCategory);
            }
        }

        return menuDefinition;
    }

    private boolean belongsToActivePlugin(final String pluginIdentifier) {
        return pluginAccessor.getEnabledPlugin(pluginIdentifier) != null;
    }

    @Override
    @Transactional
    public void createViewIfNotExists(final String pluginIdentifier, final String viewName, final String view, final String url) {
        Entity menuView = getView(pluginIdentifier, viewName);

        if (menuView != null) {
            return;
        }

        menuView = dataDefinitionService.get("menu", "view").create();
        menuView.setField("pluginIdentifier", pluginIdentifier);
        menuView.setField("name", viewName);
        menuView.setField("url", url);
        menuView.setField("view", view);
        dataDefinitionService.get("menu", "view").save(menuView);
    }

    @Override
    @Transactional
    public void enableView(final String pluginIdentifier, final String viewName) {
        // ignore

    }

    @Override
    @Transactional
    public void disableView(final String pluginIdentifier, final String viewName) {
        // ignore
    }

    @Override
    @Transactional
    public void createCategoryIfNotExists(final String pluginIdentifier, final String categoryName) {
        Entity menuCategory = getCategory(pluginIdentifier, categoryName);

        if (menuCategory != null) {
            return;
        }

        menuCategory = dataDefinitionService.get("menu", "category").create();
        menuCategory.setField("pluginIdentifier", pluginIdentifier);
        menuCategory.setField("name", categoryName);
        menuCategory.setField("succession", getTotalNumberOfCategories());
        dataDefinitionService.get("menu", "category").save(menuCategory);
    }

    @Override
    @Transactional
    public void createItemIfNotExists(final String pluginIdentifier, final String name, final String category,
            final String viewPluginIdentifier, final String viewName) {
        Entity menuItem = getItem(pluginIdentifier, name);

        if (menuItem != null) {
            return;
        }

        Entity menuCategory = getCategory(category);
        Entity menuView = getView(viewPluginIdentifier, viewName);

        if (menuCategory == null) {
            throw new IllegalStateException("Cannot find menu category " + category + " for item " + pluginIdentifier + "."
                    + name);
        }

        if (menuView == null) {
            throw new IllegalStateException("Cannot find menu view " + viewPluginIdentifier + "." + viewName + " for item "
                    + pluginIdentifier + "." + name);
        }

        menuItem = dataDefinitionService.get("menu", "item").create();
        menuItem.setField("pluginIdentifier", pluginIdentifier);
        menuItem.setField("name", name);
        menuItem.setField("active", true);
        menuItem.setField("category", menuCategory);
        menuItem.setField("view", menuView);
        menuItem.setField("succession", getTotalNumberOfItems(menuCategory));
        dataDefinitionService.get("menu", "item").save(menuItem);
    }

    @Override
    @Transactional
    public void enableItem(final String pluginIdentifier, final String name) {
        Entity menuItem = getItem(pluginIdentifier, name);

        if (menuItem != null) {
            menuItem.setField("active", true);
            dataDefinitionService.get("menu", "item").save(menuItem);
        }
    }

    @Override
    @Transactional
    public void disableItem(final String pluginIdentifier, final String name) {
        Entity menuItem = getItem(pluginIdentifier, name);

        if (menuItem != null) {
            menuItem.setField("active", false);
            dataDefinitionService.get("menu", "item").save(menuItem);
        }
    }

    private int getTotalNumberOfItems(final Entity category) {
        return dataDefinitionService.get("menu", "item").find()
                .restrictedWith(Restrictions.belongsTo(dataDefinitionService.get("menu", "item").getField("category"), category))
                .list().getTotalNumberOfEntities() + 1;
    }

    private int getTotalNumberOfCategories() {
        return dataDefinitionService.get("menu", "category").find().list().getTotalNumberOfEntities() + 1;
    }

    private Entity getCategory(final String pluginIdentifier, final String categoryName) {
        return dataDefinitionService.get("menu", "category").find().restrictedWith(Restrictions.eq("name", categoryName))
                .restrictedWith(Restrictions.eq("pluginIdentifier", pluginIdentifier)).withMaxResults(1).uniqueResult();
    }

    private Entity getCategory(final String categoryName) {
        return dataDefinitionService.get("menu", "category").find().restrictedWith(Restrictions.eq("name", categoryName))
                .withMaxResults(1).uniqueResult();
    }

    private Entity getItem(final String pluginIdentifier, final String itemName) {
        return dataDefinitionService.get("menu", "item").find().restrictedWith(Restrictions.eq("name", itemName))
                .restrictedWith(Restrictions.eq("pluginIdentifier", pluginIdentifier)).withMaxResults(1).uniqueResult();
    }

    private Entity getView(final String pluginIdentifier, final String viewName) {
        return dataDefinitionService.get("menu", "view").find().restrictedWith(Restrictions.eq("name", viewName))
                .restrictedWith(Restrictions.eq("pluginIdentifier", pluginIdentifier)).withMaxResults(1).uniqueResult();
    }

}
