/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.aop.internal.Monitorable;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.plugins.internal.enums.PluginStatus;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.menu.MenuDefinition;
import com.qcadoo.mes.view.menu.MenulItemsGroup;
import com.qcadoo.mes.view.menu.items.UrlMenuItem;
import com.qcadoo.mes.view.menu.items.ViewDefinitionMenuItemItem;

@Service
public final class MenuServiceImpl implements MenuService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PluginManagementService pluginManagementService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Override
    @Transactional
    @Monitorable
    public void updateViewDefinitionDatabase() {
        DataDefinition viewDefinitionDD = dataDefinitionService.get("menu", "viewDefinition");

        List<ViewDefinition> menuableViews = viewDefinitionService.listForMenu();
        for (ViewDefinition view : menuableViews) {
            int existingViewsNumber = viewDefinitionDD.find()
                    .restrictedWith(Restrictions.eq("pluginIdentifier", view.getPluginIdentifier()))
                    .restrictedWith(Restrictions.eq("viewName", view.getName())).list().getTotalNumberOfEntities();
            if (existingViewsNumber == 0) {
                Entity viewDefinitionEntity = new DefaultEntity("menu", "viewDefinition", null);
                viewDefinitionEntity.setField("menuName", view.getName());
                viewDefinitionEntity.setField("viewName", view.getName());
                viewDefinitionEntity.setField("pluginIdentifier", view.getPluginIdentifier());
                viewDefinitionDD.save(viewDefinitionEntity);
            }
        }

        for (Entity viewDefinitionEntity : viewDefinitionDD.find().list().getEntities()) {
            ViewDefinition vd = viewDefinitionService.getWithoutSession(viewDefinitionEntity.getStringField("pluginIdentifier"),
                    viewDefinitionEntity.getStringField("viewName"));
            if (vd == null || !vd.isMenuable()) {
                viewDefinitionDD.deleteHard(viewDefinitionEntity.getId());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public MenuDefinition getMenu(final Locale locale) {

        MenuDefinition menuDef = new MenuDefinition();

        MenulItemsGroup homeItem = new MenulItemsGroup("home", translationService.translate("core.menu.home", locale));
        homeItem.addItem(new UrlMenuItem("home", translationService.translate("core.menu.home", locale), null, "homePage.html"));
        homeItem.addItem(new UrlMenuItem("about", translationService.translate("core.menu.about", locale), null,
                "http://qcadoo.com/"));
        homeItem.addItem(new UrlMenuItem("profile", translationService.translate("core.menu.profile", locale), null,
                "userProfile.html"));
        homeItem.addItem(new UrlMenuItem("systemInfo", translationService.translate("core.menu.systemInfo", locale), null,
                "systemInfo.html"));
        menuDef.addItem(homeItem);

        DataDefinition menuDD = dataDefinitionService.get("menu", "menuCategory");
        List<Entity> categories = menuDD.find().list().getEntities();

        MenulItemsGroup administrationCategory = null;
        boolean hasMenuCategoryGridView = false;

        for (Entity categoryEntity : categories) {
            if (!(Boolean) categoryEntity.getField("active")) {
                continue;
            }
            String categoryName = categoryEntity.getStringField("name");
            String categoryTranslationName = categoryEntity.getStringField("translationName");

            MenulItemsGroup category = new MenulItemsGroup(categoryName, getLabel(categoryName, categoryTranslationName, locale));
            if ("core.menu.administration".equals(categoryTranslationName)) {
                administrationCategory = category;
            }

            for (Entity itemEntity : categoryEntity.getHasManyField("viewDefinitionItems")) {
                if (!(Boolean) itemEntity.getField("active")) {
                    continue;
                }
                String itemName = itemEntity.getStringField("name");
                Entity viewDefinitionEntity = itemEntity.getBelongsToField("viewDefinition");
                String viewName = viewDefinitionEntity.getStringField("viewName");
                String pluginIdentifier = viewDefinitionEntity.getStringField("pluginIdentifier");
                if (belongsToActivePlugin(pluginIdentifier)) {
                    String itemTranslationName = itemEntity.getStringField("translationName");
                    category.addItem(new ViewDefinitionMenuItemItem(itemName, getLabel(itemName, itemTranslationName, locale),
                            pluginIdentifier, viewName));
                }
                if ("menu".equals(pluginIdentifier) && "menuCategoryGridView".equals(viewName)) {
                    hasMenuCategoryGridView = true;
                }
            }

            menuDef.addItem(category);
        }

        if (!hasMenuCategoryGridView) {
            if (administrationCategory == null) {
                administrationCategory = new MenulItemsGroup("administration", getLabel("administration",
                        "core.menu.administration", locale));
                menuDef.addItem(administrationCategory);
            }
            administrationCategory.addItem(new ViewDefinitionMenuItemItem("menuCategoryGridView", getLabel(
                    "menuCategoryGridView", "menu.menu.administration.menu", locale), "menu", "menuCategoryGridView"));
        }

        return menuDef;
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
        return pluginManagementService.getByIdentifierAndStatus(pluginIdentifier, PluginStatus.ACTIVE.getValue()) != null;
    }
}
