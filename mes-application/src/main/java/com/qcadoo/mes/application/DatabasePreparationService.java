/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.application;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.beans.dictionaries.DictionariesDictionary;
import com.qcadoo.mes.beans.menu.MenuMenuCategory;
import com.qcadoo.mes.beans.menu.MenuMenuViewDefinitionItem;
import com.qcadoo.mes.beans.menu.MenuViewDefinition;
import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.beans.users.UsersGroup;
import com.qcadoo.mes.beans.users.UsersUser;

@Component
public final class DatabasePreparationService implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(DatabasePreparationService.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TestDataLoader testDataLoader;

    @Value("${loadTestData}")
    private boolean addTestData;

    private UsersGroup adminGroup;

    private UsersGroup supervisorsGroup;

    @Value("${addAdministrationMenuToDatabase}")
    private boolean addAdministrationMenuToDatabase;

    @Value("${addHardAdminPass}")
    private boolean addHardAdminPass;

    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (databaseHasToBePrepared()) {
            LOG.info("Database has to be prepared ...");

            addMenus();
            addGroups();
            addUsers();
            // TODO masz plugins should be added automatically using plugin.xml
            addPlugins();
            addDictionaries();

            if (addTestData) {
                addTestData();
            }
        } else {
            LOG.info("Database has been already prepared, skipping");
        }
    }

    private void addMenus() {
        MenuViewDefinition menuCategoryGridView = getMenuViewDefinition("menuCategories");
        MenuViewDefinition technologyGridView = getMenuViewDefinition("technologies");
        MenuViewDefinition orderGridView = getMenuViewDefinition("orders");
        MenuViewDefinition pluginGridView = getMenuViewDefinition("plugins");
        MenuViewDefinition userGridView = getMenuViewDefinition("users");
        MenuViewDefinition dictionaryGridView = getMenuViewDefinition("dictionaries");
        MenuViewDefinition materialRequirementGridView = getMenuViewDefinition("materialRequirements");
        MenuViewDefinition productGridView = getMenuViewDefinition("products");
        MenuViewDefinition groupGridView = getMenuViewDefinition("groups");
        MenuViewDefinition machineGridView = getMenuViewDefinition("machineGridView");
        MenuViewDefinition staffGridView = getMenuViewDefinition("staffGridView");
        MenuViewDefinition workPlanGridView = getMenuViewDefinition("workPlans");
        MenuViewDefinition operationGridView = getMenuViewDefinition("operations");


        MenuMenuCategory menuCategoryBasicData = addMenuCategory("basic", "core.menu.basic", 1);
        MenuMenuCategory menuCategoryTechnology = addMenuCategory("technology", "core.menu.technology", 2);
        MenuMenuCategory menuCategoryAdministration = addMenuCategory("administration", "core.menu.administration", 3);

        addMenuViewDefinitionItem("technologies", "products.menu.products.technologies", menuCategoryTechnology,
                technologyGridView, 1);
        addMenuViewDefinitionItem("products", "products.menu.products.products", menuCategoryBasicData , productGridView, 2);
        addMenuViewDefinitionItem("productionOrders", "products.menu.products.productionOrders", menuCategoryTechnology,
                orderGridView, 3);
        addMenuViewDefinitionItem("materialRequirements", "products.menu.products.materialRequirements", menuCategoryTechnology,
                materialRequirementGridView, 4);
        addMenuViewDefinitionItem("operations", "products.menu.products.operations", menuCategoryTechnology, operationGridView, 5);
        addMenuViewDefinitionItem("workPlans", "products.menu.products.workPlans", menuCategoryTechnology, workPlanGridView, 6);

        if (addAdministrationMenuToDatabase) {
            addMenuViewDefinitionItem("users", "users.menu.administration.users", menuCategoryAdministration, userGridView, 2);
            addMenuViewDefinitionItem("groups", "users.menu.administration.groups", menuCategoryAdministration, groupGridView, 3);
            addMenuViewDefinitionItem("plugins", "plugins.menu.administration.plugins", menuCategoryAdministration,
                    pluginGridView, 3);
            addMenuViewDefinitionItem("menu", "menu.menu.administration.menu", menuCategoryAdministration, menuCategoryGridView,
                    4);
        }

        addMenuViewDefinitionItem("dictionaries", "dictionaries.menu.administration.dictionaries", menuCategoryBasicData,
                dictionaryGridView, 1);
        addMenuViewDefinitionItem("machines", "basic.menu.machines", menuCategoryBasicData, machineGridView, 2);
        addMenuViewDefinitionItem("staff", "basic.menu.staff", menuCategoryBasicData, staffGridView, 3);
    }

    private void addMenuViewDefinitionItem(final String name, final String translation, final MenuMenuCategory menuCategory,
            final MenuViewDefinition menuViewDefinition, final int order) {
        LOG.info("Adding menu view item \"" + name + "\"");
        MenuMenuViewDefinitionItem menuItem = new MenuMenuViewDefinitionItem();
        menuItem.setItemOrder(order);
        menuItem.setMenuCategory(menuCategory);
        menuItem.setName(name);
        menuItem.setActive(true);
        menuItem.setTranslationName(translation);
        menuItem.setViewDefinition(menuViewDefinition);
        sessionFactory.getCurrentSession().save(menuItem);
    }

    private MenuViewDefinition getMenuViewDefinition(final String name) {
        return (MenuViewDefinition) sessionFactory.getCurrentSession().createCriteria(MenuViewDefinition.class)
                .add(Restrictions.eq("menuName", name)).setMaxResults(1).uniqueResult();
    }

    private MenuMenuCategory addMenuCategory(final String name, final String translation, final int order) {
        LOG.info("Adding menu category \"" + name + "\"");
        MenuMenuCategory category = new MenuMenuCategory();
        category.setName(name);
        category.setActive(true);
        category.setTranslationName(translation);
        category.setCategoryOrder(order);
        sessionFactory.getCurrentSession().save(category);
        return category;
    }

    private void addGroups() {
        adminGroup = addGroup("Admins", "ROLE_ADMIN");
        supervisorsGroup = addGroup("Supervisors", "ROLE_SUPERVISOR");
        addGroup("Users", "ROLE_USER");
    }

    private UsersGroup addGroup(final String name, final String role) {
        LOG.info("Adding group \"" + name + "\" with role \"" + role + "\"");
        UsersGroup group = new UsersGroup();
        group.setName(name);
        group.setRole(role);
        group.setDescription(null);
        sessionFactory.getCurrentSession().save(group);
        return group;
    }

    private void addUsers() {
        addUser("demo", "demo@email.com", "Demo", "Demo", "2a97516c354b68848cdbd8f54a226a0a55b21ed138e207ad6c5cbb9c00aa5aea",
                supervisorsGroup);
        if (addHardAdminPass) {
            addUser("admin", "admin@email.com", "Admin", "Admin",
                    "6b63dcb740cd63e4497883ae1fb645c5880face17b6483b468ce4c50f93698be", adminGroup);
        } else {
            addUser("admin", "admin@email.com", "Admin", "Admin",
                    "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918", adminGroup);
        }
    }

    private void addUser(final String login, final String email, final String firstName, final String lastName,
            final String password, final UsersGroup group) {
        LOG.info("Adding \"" + login + "\" user");
        UsersUser user = new UsersUser();
        user.setUserName(login);
        user.setUserGroup(group);
        user.setDescription("");
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setDescription(null);
        user.setPassword(password);
        sessionFactory.getCurrentSession().save(user);
    }

    private void addDictionaries() {
        addDictionary("categories");
        addDictionary("machines");
    }

    private void addDictionary(final String name) {
        LOG.info("Adding dictionary \"" + name + "\"");
        DictionariesDictionary dictionary = new DictionariesDictionary();
        dictionary.setName(name);
        sessionFactory.getCurrentSession().save(dictionary);
    }

    private void addPlugins() {
        addPlugin("users", "Qcadoo MES :: Plugins :: User Management", false, "mes-plugins-user-management-0.2.0-SNAPSHOT.jar");
        addPlugin("dictionaries", "Qcadoo MES :: Plugins :: Dictionary Management", false,
                "mes-plugins-dictionary-management-0.2.0-SNAPSHOT.jar");
        addPlugin("plugins", "Qcadoo MES :: Plugins :: Plugin Management", true,
                "mes-plugins-plugin-management-0.2.0-SNAPSHOT.jar");
        addPlugin("menu", "Qcadoo MES :: Plugins :: Menu Management", true, "mes-plugins-menu-management-0.2.0-SNAPSHOT.jar");
        addPlugin("crud", "Qcadoo MES :: Plugins :: CRUD", true, "mes-plugins-crud-0.2.0-SNAPSHOT.jar");
        addPlugin("products", "Qcadoo MES :: Plugins :: Products", false, "mes-plugins-products-0.2.0-SNAPSHOT.jar");
        addPlugin("basic", "Qcadoo MES :: Plugins :: Basic", false, "mes-plugins-basic-management-0.2.0-SNAPSHOT.jar");
    }

    private void addPlugin(final String identifier, final String name, final boolean base, final String fileName) {
        LOG.info("Adding plugin \"" + identifier + "\"");
        PluginsPlugin plugin = new PluginsPlugin();
        plugin.setBase(base);
        plugin.setFileName(fileName);
        plugin.setIdentifier(identifier);
        plugin.setName(name);
        plugin.setPackageName("com.qcadoo.mes." + name);
        plugin.setStatus("active");
        plugin.setVendor("Qcadoo Limited");
        plugin.setVersion("0.2.0-SNAPSHOT");
        plugin.setDescription(null);
        sessionFactory.getCurrentSession().save(plugin);
    }

    private void addTestData() {
        testDataLoader.loadTestData();
    }

    private boolean databaseHasToBePrepared() {
        return sessionFactory.getCurrentSession().createCriteria(UsersUser.class).list().size() == 0;
    }

}
