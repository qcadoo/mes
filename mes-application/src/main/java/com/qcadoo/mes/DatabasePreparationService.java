package com.qcadoo.mes;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.beans.dictionaries.DictionariesDictionary;
import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.beans.users.UsersGroup;
import com.qcadoo.mes.beans.users.UsersUser;

@Component
public final class DatabasePreparationService implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(DatabasePreparationService.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TestDataConverter testDataConverter;

    @Value("${loadTestData}")
    private boolean addTestData;

    private UsersGroup adminGroup;

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
        // TODO Auto-generated method stub
    }

    private void addGroups() {
        adminGroup = addGroup("Admins", "ROLE_ADMIN");
        addGroup("Supervisors", "ROLE_SUPERVISOR");
        addGroup("Users", "ROLE_USER");
    }

    private UsersGroup addGroup(final String name, final String role) {
        LOG.info("Adding group \"" + name + "\" with role \"" + role + "\"");
        UsersGroup group = new UsersGroup();
        group.setName(name);
        group.setRole(role);
        group.setDescription("");
        sessionFactory.getCurrentSession().save(group);
        return group;
    }

    private void addUsers() {
        addUser("admin", "", "", "", "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918", adminGroup);
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
        user.setDescription("");
        user.setPassword(password);
        sessionFactory.getCurrentSession().save(user);
    }

    private void addDictionaries() {
        addDictionary("categories");
    }

    private void addDictionary(final String name) {
        LOG.info("Adding dictionary \"" + name + "\"");
        DictionariesDictionary dictionary = new DictionariesDictionary();
        dictionary.setName(name);
        sessionFactory.getCurrentSession().save(dictionary);
    }

    private void addPlugins() {
        addPlugin("users", "Qcadoo MES :: Plugins :: User Management", true);
        addPlugin("dictionaries", "Qcadoo MES :: Plugins :: Dictionary Management", true);
        addPlugin("plugins", "Qcadoo MES :: Plugins :: Plugin Management", true);
        addPlugin("menu", "Qcadoo MES :: Plugins :: Menu Management", true);
        addPlugin("crud", "Qcadoo MES :: Plugins :: CRUD", true);
        addPlugin("products", "Qcadoo MES :: Plugins :: Products", false);
    }

    private void addPlugin(final String identifier, final String name, final boolean base) {
        LOG.info("Adding plugin \"" + identifier + "\"");
        PluginsPlugin plugin = new PluginsPlugin();
        plugin.setBase(base);
        plugin.setFileName("mes-plugins-" + name + "-0.1-SNAPSHOT.jar");
        plugin.setIdentifier(identifier);
        plugin.setName(name);
        plugin.setPackageName("com.qcadoo.mes." + name);
        plugin.setStatus("active");
        plugin.setVendor("Qcadoo Limited");
        plugin.setVersion("0.1-SNAPSHOT");
        plugin.setDescription("");
        sessionFactory.getCurrentSession().save(plugin);
    }

    private void addTestData() {
        testDataConverter.loadTestData();
    }

    private boolean databaseHasToBePrepared() {
        return sessionFactory.getCurrentSession().createCriteria(UsersUser.class).list().size() == 0;
    }

}
