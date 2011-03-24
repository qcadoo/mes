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

import javax.sql.DataSource;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.application.TestDataLoader;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.plugin.api.Module;
import com.qcadoo.plugin.api.ModuleFactory;

@Component
public final class DatabasePreparationModuleFactory implements ModuleFactory<Module> {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TestDataLoader testDataLoader;

    @Value("${loadTestData}")
    private boolean addTestData;

    @Value("${addHardAdminPass}")
    private boolean addHardAdminPass;

    @Override
    public void init() {
        // empty
    }

    @Override
    public Module parse(final String pluginIdentifier, final Element element) {
        DatabasePreparationModule module = new DatabasePreparationModule();
        module.setDataSource(dataSource);
        module.setDataDefinitionService(dataDefinitionService);
        module.setTestDataLoader(testDataLoader);
        module.setAddTestData(addTestData);
        module.setAddHardAdminPass(addHardAdminPass);
        return module;
    }

    @Override
    public String getIdentifier() {
        return "databasePreparation";
    }

}
