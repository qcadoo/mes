package com.qcadoo.mes.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.enums.PluginStatus;
import com.qcadoo.mes.model.aop.internal.Monitorable;
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

    @Autowired
    private TranslationService translationService;

    private final Map<String, ViewDefinition> viewDefinitions = new HashMap<String, ViewDefinition>();

    @Override
    @Transactional(readOnly = true)
    @Monitorable
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
    @Monitorable
    public List<ViewDefinition> list() {
        return new ArrayList<ViewDefinition>(viewDefinitions.values());
    }

    @Override
    @Transactional
    @Monitorable
    public void save(final ViewDefinition viewDefinition) {
        viewDefinitions.put(viewDefinition.getPluginIdentifier() + "." + viewDefinition.getName(), viewDefinition);
    }

    @Override
    @Transactional
    @Monitorable
    public void delete(final String pluginIdentifier, final String viewName) {
        viewDefinitions.remove(pluginIdentifier + "." + viewName);
    }

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public MenuDefinition getMenu(final Locale locale) {

        MenuDefinition baseMenuDefinition = new MenuDefinition();

        MenulItemsGroup homeItem = new MenulItemsGroup("home", translationService.translate("core.menu.home", locale));
        homeItem.addItem(new UrlMenuItem("home", translationService.translate("core.menu.home", locale), null, "homePage.html"));
        homeItem.addItem(new UrlMenuItem("about", translationService.translate("core.menu.about", locale), null,
                "http://qcadoo.com/"));
        baseMenuDefinition.addItem(homeItem);

        MenulItemsGroup productsItem = new MenulItemsGroup("products", translationService.translate("core.menu.products", locale));

        productsItem.addItem(new ViewDefinitionMenuItemItem("instructions", translationService.translate(
                "products.menu.products.instructions", locale), "products", "instructionGridView"));
        productsItem.addItem(new ViewDefinitionMenuItemItem("productionOrders", translationService.translate(
                "products.menu.products.productionOrders", locale), "products", "orderGridView"));
        productsItem.addItem(new ViewDefinitionMenuItemItem("materialRequirements", translationService.translate(
                "products.menu.products.materialRequirements", locale), "products", "materialRequirementGridView"));
        baseMenuDefinition.addItem(productsItem);

        MenulItemsGroup administrationItem = new MenulItemsGroup("administration", translationService.translate(
                "core.menu.administration", locale));
        administrationItem.addItem(new ViewDefinitionMenuItemItem("dictionaries", translationService.translate(
                "dictionaries.menu.administration.dictionaries", locale), "dictionaries", "dictionaryGridView"));
        administrationItem.addItem(new ViewDefinitionMenuItemItem("users", translationService.translate(
                "users.menu.administration.users", locale), "users", "userGridView"));
        administrationItem.addItem(new ViewDefinitionMenuItemItem("groups", translationService.translate(
                "users.menu.administration.groups", locale), "users", "groupGridView"));
        administrationItem.addItem(new ViewDefinitionMenuItemItem("plugins", translationService.translate(
                "plugins.menu.administration.plugins", locale), "plugins", "pluginGridView"));
        baseMenuDefinition.addItem(administrationItem);

        MenulItemsGroup masterDataItem = new MenulItemsGroup("masterData", translationService.translate("core.menu.masterData",
                locale));
        masterDataItem.addItem(new ViewDefinitionMenuItemItem("products", translationService.translate(
                "products.menu.products.products", locale), "products", "productGridView"));
        masterDataItem.addItem(new ViewDefinitionMenuItemItem("machines", translationService.translate(
                "machines.menu.machines.machines", locale), "machines", "machinesGrid"));
        baseMenuDefinition.addItem(masterDataItem);

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
