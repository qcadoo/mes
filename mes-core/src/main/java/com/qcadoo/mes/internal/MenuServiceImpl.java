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

import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.plugins.internal.enums.PluginStatus;
import com.qcadoo.mes.utils.Pair;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.menu.MenuDefinition;
import com.qcadoo.mes.view.menu.MenuItem;
import com.qcadoo.mes.view.menu.MenulItemsGroup;
import com.qcadoo.mes.view.menu.items.UrlMenuItem;
import com.qcadoo.mes.view.menu.items.ViewDefinitionMenuItemItem;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.Monitorable;

@Service
public final class MenuServiceImpl implements MenuService {

    // private static final Logger LOG = LoggerFactory.getLogger(MenuServiceImpl.class);

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PluginManagementService pluginManagementService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Value("${showAdministrationMenu}")
    private boolean showAdministrationMenu;

    @Override
    @Transactional
    @Monitorable
    public void updateViewDefinitionDatabase() {
        DataDefinition viewDefinitionDD = dataDefinitionService.get("menu", "viewDefinition");

        List<Pair<String, String>> menuViews = viewDefinitionService.listForMenu();
        for (Pair<String, String> view : menuViews) {
            int existingViewsNumber = viewDefinitionDD.find().restrictedWith(Restrictions.eq("pluginIdentifier", view.getKey()))
                    .restrictedWith(Restrictions.eq("viewName", view.getValue())).list().getTotalNumberOfEntities();
            if (existingViewsNumber == 0) {
                Entity viewDefinitionEntity = viewDefinitionDD.create();
                viewDefinitionEntity.setField("menuName", view.getValue());
                viewDefinitionEntity.setField("viewName", view.getValue());
                viewDefinitionEntity.setField("pluginIdentifier", view.getKey());
                viewDefinitionEntity.setField("url", false);
                viewDefinitionDD.save(viewDefinitionEntity);
            }
        }

        for (Entity viewDefinitionEntity : viewDefinitionDD.find().list().getEntities()) {
            if ((Boolean) viewDefinitionEntity.getField("url")) {
                continue;
            }
            ViewDefinition vd = viewDefinitionService.getWithoutSession(viewDefinitionEntity.getStringField("pluginIdentifier"),
                    viewDefinitionEntity.getStringField("viewName"));
            if (vd == null || !vd.isMenuAccessible()) {
                viewDefinitionDD.delete(viewDefinitionEntity.getId());
            }
        }

        addUrlItem("homePage", "homePage.html", "crud");
        addUrlItem("profile", "userProfile.html", "crud");
        addUrlItem("systemInfoView", "systemInfo.html", "crud");

        addUrlItem("systemParameters", "parameter.html", "basic");
        addUrlItem("genealogyAttributes", "genealogyAttribute.html", "genealogies");
    }

    private void addUrlItem(final String name, final String url, final String pluginIdentifier) {
        DataDefinition viewDefinitionDD = dataDefinitionService.get("menu", "viewDefinition");

        int existingViewsNumber = viewDefinitionDD.find().restrictedWith(Restrictions.eq("pluginIdentifier", pluginIdentifier))
                .restrictedWith(Restrictions.eq("viewName", url)).list().getTotalNumberOfEntities();
        if (existingViewsNumber == 0) {
            Entity viewDefinitionEntity = viewDefinitionDD.create();
            viewDefinitionEntity.setField("menuName", name);
            viewDefinitionEntity.setField("viewName", url);
            viewDefinitionEntity.setField("pluginIdentifier", pluginIdentifier);
            viewDefinitionEntity.setField("url", true);
            viewDefinitionDD.save(viewDefinitionEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public MenuDefinition getMenu(final Locale locale) {

        MenuDefinition menuDef = new MenuDefinition();

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
                boolean isUrl = (Boolean) viewDefinitionEntity.getField("url");
                if (belongsToActivePlugin(pluginIdentifier)) {
                    String itemTranslationName = itemEntity.getStringField("translationName");
                    MenuItem item;
                    String itemLabel = getLabel(itemName, itemTranslationName, locale);
                    if (isUrl) {
                        item = new UrlMenuItem(itemName, itemLabel, pluginIdentifier, viewName);
                    } else {
                        item = new ViewDefinitionMenuItemItem(itemName, itemLabel, pluginIdentifier, viewName);
                    }
                    category.addItem(item);
                }
                if ("menu".equals(pluginIdentifier) && "menuCategories".equals(viewName)) {
                    hasMenuCategoryGridView = true;
                }
            }

            menuDef.addItem(category);
        }

        if (!hasMenuCategoryGridView && showAdministrationMenu) {
            if (administrationCategory == null) {
                administrationCategory = new MenulItemsGroup("administration", getLabel("administration",
                        "core.menu.administration", locale));
                menuDef.addItem(administrationCategory);
            }
            administrationCategory.addItem(new ViewDefinitionMenuItemItem("menuCategories", getLabel("menuCategories",
                    "menu.menu.administration.menu", locale), "menu", "menuCategories"));
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
