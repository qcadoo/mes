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

package com.qcadoo.mes.internal.module;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.application.TestDataLoader;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.internal.api.Module;
import com.qcadoo.plugin.internal.api.ModuleFactory;

@Component
public final class DatabasePreparationModuleFactory implements ModuleFactory<Module> {

    private static final Logger LOG = LoggerFactory.getLogger(DatabasePreparationModuleFactory.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TestDataLoader testDataLoader;

    @Value("${loadTestData}")
    private boolean addTestData;

    @Value("${showSystemInfo}")
    private boolean showSystemInfo;

    private final Map<String, Entity> menuCategories = new HashMap<String, Entity>();

    private Entity adminGroup;

    private Entity supervisorsGroup;

    @Value("${addAdministrationMenuToDatabase}")
    private boolean addAdministrationMenuToDatabase;

    @Value("${addHardAdminPass}")
    private boolean addHardAdminPass;

    @Override
    @Transactional
    public void init() {
        if (databaseHasToBePrepared()) {
            LOG.info("Database has to be prepared ...");

            createPersistenceLogins();

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
    public Module parse(final String pluginIdentifier, final Element element) {
        throw new IllegalStateException("Cannot create module for databasePreparation");
    }

    @Override
    public String getIdentifier() {
        return "#databasePreparation";
    }

    private void createPersistenceLogins() {
        new JdbcTemplate(dataSource).execute(JdbcTokenRepositoryImpl.CREATE_TABLE_SQL);
    }

    private void addGroups() {
        adminGroup = addGroup("Admins", "ROLE_ADMIN");
        supervisorsGroup = addGroup("Supervisors", "ROLE_SUPERVISOR");
        addGroup("Users", "ROLE_USER");
    }

    private void addParameters() {
        LOG.info("Adding parameters");
        Entity parameter = dataDefinitionService.get("basic", "parameter").create();
        parameter.setField("checkDoneOrderForQuality", false);
        parameter.setField("autoGenerateQualityControl", false);
        parameter.setField("batchForDoneOrder", "01none");
        dataDefinitionService.get("basic", "parameter").save(parameter);
    }

    private void addUsers() {
        addUser("demo", "demo@email.com", "Demo", "Demo", "demo", supervisorsGroup);
        if (addHardAdminPass) {
            // TODO password
            addUser("admin", "admin@email.com", "Admin", "Admin", "qwe123", adminGroup);
        } else {
            addUser("admin", "admin@email.com", "Admin", "Admin", "admin", adminGroup);
        }
    }

    private void addUser(final String login, final String email, final String firstName, final String lastName,
            final String password, final Entity group) {
        LOG.info("Adding \"" + login + "\" user");
        Entity entity = dataDefinitionService.get("users", "user").create();
        entity.setField("userName", login);
        entity.setField("userGroup", group);
        entity.setField("email", email);
        entity.setField("firstName", firstName);
        entity.setField("lastName", lastName);
        entity.setField("password", password);
        entity.setField("passwordConfirmation", password);
        entity.setField("enabled", true);
        dataDefinitionService.get("users", "user").save(entity);
    }

    private Entity addGroup(final String name, final String role) {
        LOG.info("Adding group \"" + name + "\" with role \"" + role + "\"");
        Entity entity = dataDefinitionService.get("users", "group").create();
        entity.setField("name", name);
        entity.setField("role", role);
        return dataDefinitionService.get("users", "group").save(entity);
    }

    private boolean databaseHasToBePrepared() {
        return dataDefinitionService.get("users", "user").find().list().getTotalNumberOfEntities() == 0;
    }

}
