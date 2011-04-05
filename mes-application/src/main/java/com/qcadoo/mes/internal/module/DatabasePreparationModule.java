package com.qcadoo.mes.internal.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.application.TestDataLoader;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.Module;

public class DatabasePreparationModule extends Module {

    private static final Logger LOG = LoggerFactory.getLogger(DatabasePreparationModule.class);

    private DataDefinitionService dataDefinitionService;

    private TestDataLoader testDataLoader;

    private boolean addTestData;

    private Entity adminGroup;

    private Entity supervisorsGroup;

    private boolean addHardAdminPass;

    private void addParameters() {
        LOG.info("Adding parameters");
        Entity parameter = dataDefinitionService.get("basic", "parameter").create();
        parameter.setField("checkDoneOrderForQuality", false);
        parameter.setField("autoGenerateQualityControl", false);
        parameter.setField("batchForDoneOrder", "01none");
        dataDefinitionService.get("basic", "parameter").save(parameter);
    }

    private void addGroups() {
        adminGroup = addGroup("Admins", "ROLE_ADMIN");
        supervisorsGroup = addGroup("Supervisors", "ROLE_SUPERVISOR");
        addGroup("Users", "ROLE_USER");
    }

    private void addUsers() {
        addUser("demo", "demo@email.com", "Demo", "Demo", "demo", supervisorsGroup);
        if (addHardAdminPass) {
            addUser("admin", "admin@email.com", "Admin", "Admin", "Qland1Charon", adminGroup);
        } else {
            addUser("admin", "admin@email.com", "Admin", "Admin", "admin", adminGroup);
        }
    }

    private void addUser(final String login, final String email, final String firstName, final String lastName,
            final String password, final Entity group) {
        LOG.info("Adding \"" + login + "\" user");
        Entity entity = dataDefinitionService.get("qcadooSecurity", "user").create();
        entity.setField("userName", login);
        entity.setField("userGroup", group);
        entity.setField("email", email);
        entity.setField("firstName", firstName);
        entity.setField("lastName", lastName);
        entity.setField("password", password);
        entity.setField("passwordConfirmation", password);
        entity.setField("enabled", true);
        dataDefinitionService.get("qcadooSecurity", "user").save(entity);
    }

    private Entity addGroup(final String name, final String role) {
        LOG.info("Adding group \"" + name + "\" with role \"" + role + "\"");
        Entity entity = dataDefinitionService.get("qcadooSecurity", "group").create();
        entity.setField("name", name);
        entity.setField("role", role);
        return dataDefinitionService.get("qcadooSecurity", "group").save(entity);
    }

    private boolean databaseHasToBePrepared() {
        return dataDefinitionService.get("qcadooSecurity", "user").find().list().getTotalNumberOfEntities() == 0;
    }

    @Override
    @Transactional
    public void enable() {
        if (databaseHasToBePrepared()) {
            LOG.info("Database has to be prepared ...");

            addGroups();
            addUsers();
            addParameters();

            if (addTestData) {
                testDataLoader.loadTestData();
            }
        } else {
            LOG.info("Database has been already prepared, skipping");
        }
    }

    @Override
    public void disable() {
        // empty

    }

    public void setDataDefinitionService(final DataDefinitionService dataDefinitionService) {
        this.dataDefinitionService = dataDefinitionService;
    }

    public void setTestDataLoader(final TestDataLoader testDataLoader) {
        this.testDataLoader = testDataLoader;
    }

    public void setAddTestData(final boolean addTestData) {
        this.addTestData = addTestData;
    }

    public void setAddHardAdminPass(final boolean addHardAdminPass) {
        this.addHardAdminPass = addHardAdminPass;
    }

}
