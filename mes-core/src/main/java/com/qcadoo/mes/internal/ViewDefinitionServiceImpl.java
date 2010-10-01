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
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.menu.FirstLevelItem;
import com.qcadoo.mes.view.menu.MenuDefinition;
import com.qcadoo.mes.view.menu.secondLevel.UrlSecondLevelItem;
import com.qcadoo.mes.view.menu.secondLevel.ViewDefinitionSecondLevelItem;

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
        PluginsPlugin plugin = pluginManagementService.getByIdentifierAndStatus(pluginIdentifier, "active");
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
        MenuDefinition menuDef = new MenuDefinition();

        FirstLevelItem homeItem = new FirstLevelItem("home", "start");
        homeItem.addItem(new UrlSecondLevelItem("home", "start", "homePage.html"));
        homeItem.addItem(new UrlSecondLevelItem("google", "google", "http://www.google.pl"));
        menuDef.addItem(homeItem);

        if (belongsToActivePlugin("products")) {
            FirstLevelItem productsItem = new FirstLevelItem("products", "Zarządzanie Produktami");
            productsItem.addItem(new ViewDefinitionSecondLevelItem("products", "Produkty", "products", "productGridView"));
            productsItem.addItem(new ViewDefinitionSecondLevelItem("instructions", "Instrukcje materiałowe", "products",
                    "instructionGridView"));
            productsItem.addItem(new ViewDefinitionSecondLevelItem("productionOrders", "Zlecenia produkcyjne", "products",
                    "orderGridView"));
            menuDef.addItem(productsItem);
        }

        FirstLevelItem administrationItem = new FirstLevelItem("administration", "Administracja");
        if (belongsToActivePlugin("dictionaries")) {
            administrationItem.addItem(new ViewDefinitionSecondLevelItem("dictionaries", "Słowniki", "dictionaries",
                    "dictionaryGridView"));
        }
        if (belongsToActivePlugin("users")) {
            administrationItem.addItem(new ViewDefinitionSecondLevelItem("users", "Użytkownicy", "users", "userGridView"));
            administrationItem.addItem(new ViewDefinitionSecondLevelItem("groups", "Grupy", "users", "groupGridView"));
        }
        if (belongsToActivePlugin("plugins")) {
            administrationItem.addItem(new ViewDefinitionSecondLevelItem("plugins", "Pluginy", "plugins", "pluginGridView"));
        }
        menuDef.addItem(administrationItem);

        return menuDef;
    }
}
