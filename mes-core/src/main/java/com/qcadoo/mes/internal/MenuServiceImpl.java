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

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.menu.MenuDefinition;
import com.qcadoo.mes.view.menu.MenulItemsGroup;
import com.qcadoo.mes.view.menu.items.UrlMenuItem;
import com.qcadoo.mes.view.menu.items.ViewDefinitionMenuItemItem;
import com.qcadoo.model.api.aop.Monitorable;
import com.qcadoo.model.beans.menu.MenuCategory;
import com.qcadoo.model.beans.menu.MenuItem;
import com.qcadoo.model.beans.menu.MenuView;
import com.qcadoo.plugin.api.PluginAccessor;

@Service
public final class MenuServiceImpl implements InternalMenuService {

    @Autowired
    private PluginAccessor pluginAccessor;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SessionFactory sessionFactory;

    // @Value("${showAdministrationMenu}")
    // private boolean showAdministrationMenu;

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    @Monitorable
    public MenuDefinition getMenu(final Locale locale) {

        MenuDefinition menuDefinition = new MenuDefinition();

        List<MenuCategory> menuCategories = sessionFactory.getCurrentSession().createCriteria(MenuCategory.class)
                .add(Restrictions.eq("active", true)).list();

        // MenulItemsGroup administrationCategory = null;
        // boolean hasMenuCategoryGridView = false;

        for (MenuCategory menuCategory : menuCategories) {
            MenulItemsGroup category = new MenulItemsGroup(menuCategory.getName(), getLabel(menuCategory.getPluginIdentifier(),
                    menuCategory.getName(), locale));

            // if ("administration".equals(menuCategory.getName())) {
            // administrationCategory = category;
            // }

            for (MenuItem menuItem : menuCategory.getItems()) {
                if (!menuItem.isActive()) {
                    continue;
                }

                MenuView menuView = menuItem.getView();

                String itemLabel = getLabel(menuItem.getPluginIdentifier(), menuItem.getName(), locale);
                if (menuView.getUrl() != null) {
                    category.addItem(new UrlMenuItem(menuItem.getName(), itemLabel, null, menuView.getUrl()));
                } else if (belongsToActivePlugin(menuView.getPluginIdentifier())) {
                    category.addItem(new ViewDefinitionMenuItemItem(menuItem.getName(), itemLabel,
                            menuView.getPluginIdentifier(), menuView.getName()));
                }

                // if ("menu".equals(pluginIdentifier) && "menuCategories".equals(viewName)) {
                // hasMenuCategoryGridView = true;
                // }
            }

            menuDefinition.addItem(category);
        }

        // if (!hasMenuCategoryGridView && showAdministrationMenu) {
        // if (administrationCategory == null) {
        // administrationCategory = new MenulItemsGroup("administration", getLabel("administration",
        // "core.menu.administration", locale));
        // menuDef.addItem(administrationCategory);
        // }
        // administrationCategory.addItem(new ViewDefinitionMenuItemItem("menuCategories", getLabel("menuCategories",
        // "menu.menu.administration.menu", locale), "menu", "menuCategories"));
        // }

        return menuDefinition;
    }

    private String getLabel(final String name, final String translationName, final Locale locale) {
        if (translationName == null) {
            return name;
        } else {
            String[] translationNameParts = translationName.split("\\.");
            String lastPart = translationNameParts[translationNameParts.length - 1];
            if (name.equals(lastPart)) {
                return translationService.translate(translationName, locale);
            } else {
                return name;
            }
        }
    }

    private boolean belongsToActivePlugin(final String pluginIdentifier) {
        return pluginAccessor.getEnabledPlugin(pluginIdentifier) != null;
    }

    @Override
    public void createViewIfNotExists(final String pluginIdentifier, final String viewName, final String view, final String url) {
        MenuView menuView = getView(pluginIdentifier, viewName);

        if (menuView != null) {
            return;
        }

        menuView = new MenuView();
        menuView.setPluginIdentifier(pluginIdentifier);
        menuView.setName(viewName);
        menuView.setUrl(url);
        menuView.setView(view);
        sessionFactory.getCurrentSession().save(menuView);
    }

    @Override
    public void enableView(final String pluginIdentifier, final String viewName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disableView(final String pluginIdentifier, final String viewName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void createCategoryIfNotExists(final String pluginIdentifier, final String categoryName) {
        MenuCategory menuCategory = getCategory(pluginIdentifier, categoryName);

        if (menuCategory != null) {
            return;
        }

        menuCategory = new MenuCategory();
        menuCategory.setPluginIdentifier(pluginIdentifier);
        menuCategory.setName(categoryName);
        menuCategory.setActive(true);
        menuCategory.setSuccession(getTotalNumberOfCategories());
        sessionFactory.getCurrentSession().save(menuCategory);
    }

    @Override
    public void enableCategory(final String pluginIdentifier, final String categoryName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disableCategory(final String pluginIdentifier, final String categoryName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void createItemIfNotExists(final String pluginIdentifier, final String name, final String category,
            final String viewPluginIdentifier, final String viewName) {
        MenuItem menuItem = getItem(pluginIdentifier, name);

        if (menuItem != null) {
            return;
        }

        MenuCategory menuCategory = getCategory(category);
        MenuView menuView = getView(viewPluginIdentifier, viewName);

        if (menuCategory == null) {
            throw new IllegalStateException("Cannot find menu category " + category + " for item " + pluginIdentifier + "."
                    + name);
        }

        if (menuView == null) {
            throw new IllegalStateException("Cannot find menu view " + viewPluginIdentifier + "." + viewName + " for item "
                    + pluginIdentifier + "." + name);
        }

        menuItem = new MenuItem();
        menuItem.setPluginIdentifier(pluginIdentifier);
        menuItem.setName(name);
        menuItem.setActive(true);
        menuItem.setCategory(menuCategory);
        menuItem.setView(menuView);
        menuItem.setSuccession(getTotalNumberOfItems(menuCategory));
        sessionFactory.getCurrentSession().save(menuItem);
    }

    @Override
    public void enableItem(final String pluginIdentifier, final String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disableItem(final String pluginIdentifier, final String name) {
        // TODO Auto-generated method stub

    }

    private int getTotalNumberOfItems(final MenuCategory category) {
        return ((Number) sessionFactory.getCurrentSession().createCriteria(MenuItem.class).setProjection(Projections.rowCount())
                .add(Restrictions.eq("category", category)).uniqueResult()).intValue();
    }

    private int getTotalNumberOfCategories() {
        return ((Number) sessionFactory.getCurrentSession().createCriteria(MenuCategory.class)
                .setProjection(Projections.rowCount()).uniqueResult()).intValue();
    }

    private MenuCategory getCategory(final String pluginIdentifier, final String categoryName) {
        return (MenuCategory) sessionFactory.getCurrentSession().createCriteria(MenuCategory.class)
                .add(Restrictions.eq("name", categoryName)).add(Restrictions.eq("pluginIdentifier", pluginIdentifier))
                .setMaxResults(1).uniqueResult();
    }

    private MenuCategory getCategory(final String categoryName) {
        return (MenuCategory) sessionFactory.getCurrentSession().createCriteria(MenuCategory.class)
                .add(Restrictions.eq("name", categoryName)).setMaxResults(1).uniqueResult();
    }

    private MenuItem getItem(final String pluginIdentifier, final String itemName) {
        return (MenuItem) sessionFactory.getCurrentSession().createCriteria(MenuItem.class)
                .add(Restrictions.eq("name", itemName)).add(Restrictions.eq("pluginIdentifier", pluginIdentifier))
                .setMaxResults(1).uniqueResult();
    }

    private MenuView getView(final String pluginIdentifier, final String viewName) {
        return (MenuView) sessionFactory.getCurrentSession().createCriteria(MenuView.class)
                .add(Restrictions.eq("name", viewName)).add(Restrictions.eq("pluginIdentifier", pluginIdentifier))
                .setMaxResults(1).uniqueResult();
    }

}
