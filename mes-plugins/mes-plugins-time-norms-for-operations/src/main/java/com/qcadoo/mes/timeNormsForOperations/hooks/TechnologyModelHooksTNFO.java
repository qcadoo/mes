/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.timeNormsForOperations.hooks;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class TechnologyModelHooksTNFO {

    public void setDefaultValuesIfEmpty(final DataDefinition dataDefinition, final Entity technology) {
        if (technology.getId() == null) {
            return;
        }
        Entity savedTechnology = dataDefinition.get(technology.getId());
        if ("02accepted".equals(technology.getStringField("state")) && "01draft".equals(savedTechnology.getStringField("state"))) {

            final EntityTree operationComponents = savedTechnology.getTreeField("operationComponents");

            for (Entity operationComponent : operationComponents) {
                setOperationComponentDefaults(operationComponent);
            }
        }
    }

    private void setOperationComponentDefaults(final Entity operationComponent) {
        if (operationComponent.getField("tpz") == null) {
            operationComponent.setField("tpz", 0);
        }
        if (operationComponent.getField("timeNextOperation") == null) {
            operationComponent.setField("timeNextOperation", 0);
        }
        if (operationComponent.getField("productionInOneCycle") == null) {
            operationComponent.setField("productionInOneCycle", BigDecimal.ONE);
        }
        if (operationComponent.getField("machineUtilization") == null) {
            operationComponent.setField("machineUtilization", BigDecimal.ONE);
        }
        if (operationComponent.getField("laborUtilization") == null) {
            operationComponent.setField("laborUtilization", BigDecimal.valueOf(1L));
        }
        if (operationComponent.getField("countRealized") == null) {
            operationComponent.setField("countRealized", "01all");
        }
        if (operationComponent.getField("countMachine") == null) {
            operationComponent.setField("countMachine", BigDecimal.ZERO);
        }

        operationComponent.getDataDefinition().save(operationComponent);

        if (!operationComponent.isValid()) {
            throw new IllegalStateException("Saved Technology operation component entity is invalid!");
        }
    }

}
