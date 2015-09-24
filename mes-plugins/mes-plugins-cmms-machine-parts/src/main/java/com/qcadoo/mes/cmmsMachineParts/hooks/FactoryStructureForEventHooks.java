/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.productionLines.factoryStructure.FactoryStructureGenerationService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class FactoryStructureForEventHooks {

    private EntityTree generatedTree;

    @Autowired
    private FactoryStructureGenerationService factoryStructureGenerationService;

    // TODO move fields' names to constants
    public void generateFactoryStructure(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity maintenanceEvent = form.getEntity();
        EntityTree factoryStructure = factoryStructureGenerationService.generateFactoryStructureForEntity(maintenanceEvent,
                "maintenanceEvent");
        maintenanceEvent.setField(MaintenanceEventFields.FACTORY_STRUCTURE, factoryStructure);
        generatedTree = factoryStructure;
        form.setEntity(maintenanceEvent);
    }

    public EntityTree getGeneratedTree() {
        return generatedTree;
    }
}
