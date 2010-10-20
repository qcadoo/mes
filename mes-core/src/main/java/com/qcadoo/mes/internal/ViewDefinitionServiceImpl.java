package com.qcadoo.mes.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.enums.PluginStatus;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.menu.MenuDefinition;
import com.qcadoo.mes.view.menu.MenuItem;
import com.qcadoo.mes.view.menu.MenulItemsGroup;
import com.qcadoo.mes.view.menu.items.UrlMenuItem;
import com.qcadoo.mes.view.menu.items.ViewDefinitionMenuItemItem;

@Service
public final class ViewDefinitionServiceImpl implements ViewDefinitionService {

    @Autowired
    private PluginManagementService pluginManagementService;

    private final Map<String, ViewDefinition> viewDefinitions = new HashMap<String, ViewDefinition>();

    @Override
    @Transactional(readOnly = true)
    public ViewDefinition get(final String pluginIdentifier, final String viewName) {
        ViewDefinition viewDefinition = viewDefinitions.get(pluginIdentifier + "." + viewName);
        if (viewDefinition != null && belongsToActivePlugin(viewDefinition.getPluginIdentifier())) {
            return viewDefinition;
        } else {
            return viewDefinition;
        }
    }

    private boolean belongsToActivePlugin(final String pluginIdentifier) {
        if (pluginIdentifier == null) {
            return true;
        }
        PluginsPlugin plugin = pluginManagementService.getByIdentifierAndStatus(pluginIdentifier, PluginStatus.ACTIVE.getValue());
        return (plugin != null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewDefinition> list() {
        return new ArrayList<ViewDefinition>(viewDefinitions.values());
    }

    @Override
    @Transactional
    public void save(final ViewDefinition viewDefinition) {
        viewDefinitions.put(viewDefinition.getPluginIdentifier() + "." + viewDefinition.getName(), viewDefinition);
    }

    @Override
    @Transactional
    public void delete(final String pluginIdentifier, final String viewName) {
        viewDefinitions.remove(pluginIdentifier + "." + viewName);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuDefinition getMenu() {

        MenuDefinition baseMenuDefinition = new MenuDefinition();

        MenulItemsGroup homeItem = new MenulItemsGroup("home", "start");
        homeItem.addItem(new UrlMenuItem("home", "start", null, "homePage.html"));
        homeItem.addItem(new UrlMenuItem("google", "google", null, "http://www.google.pl"));
        baseMenuDefinition.addItem(homeItem);

        MenulItemsGroup productsItem = new MenulItemsGroup("products", "Zarządzanie Produktami");
        productsItem.addItem(new ViewDefinitionMenuItemItem("products", "Produkty", "products", "productGridView"));
        productsItem.addItem(new ViewDefinitionMenuItemItem("instructions", "Instrukcje materiałowe", "products",
                "instructionGridView"));
        productsItem.addItem(new ViewDefinitionMenuItemItem("productionOrders", "Zlecenia produkcyjne", "products",
                "orderGridView"));
        productsItem.addItem(new ViewDefinitionMenuItemItem("materialRequirements", "Zapotrzebowania materiałowe", "products",
                "materialRequirementGridView"));
        baseMenuDefinition.addItem(productsItem);

        MenulItemsGroup administrationItem = new MenulItemsGroup("administration", "Administracja");
        administrationItem.addItem(new ViewDefinitionMenuItemItem("dictionaries", "Słowniki", "dictionaries",
                "dictionaryGridView"));
        administrationItem.addItem(new ViewDefinitionMenuItemItem("users", "Użytkownicy", "users", "userGridView"));
        administrationItem.addItem(new ViewDefinitionMenuItemItem("groups", "Grupy", "users", "groupGridView"));
        administrationItem.addItem(new ViewDefinitionMenuItemItem("plugins", "Pluginy", "plugins", "pluginGridView"));
        baseMenuDefinition.addItem(administrationItem);

        MenuDefinition menuDef = new MenuDefinition();
        for (MenulItemsGroup baseGroup : baseMenuDefinition.getItems()) {
            MenulItemsGroup itemGroup = new MenulItemsGroup(baseGroup.getName(), baseGroup.getLabel());
            for (MenuItem baseItem : baseGroup.getItems()) {
                if (!belongsToActivePlugin(baseItem.getPluginIdentifier())) {
                    continue;
                }
                itemGroup.addItem(baseItem);
            }
            if (itemGroup.getItems().size() > 0) {
                menuDef.addItem(itemGroup);
            }
        }

        return menuDef;
    }
}
