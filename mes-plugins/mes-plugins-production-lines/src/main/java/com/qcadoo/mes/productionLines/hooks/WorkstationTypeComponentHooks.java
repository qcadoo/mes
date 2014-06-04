/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.productionLines.hooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionLines.constants.WorkstationTypeComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class WorkstationTypeComponentHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        checkIfThereIsTheSameWorkstation(dataDefinition, entity);
    }

    private void checkIfThereIsTheSameWorkstation(final DataDefinition dataDefinition, final Entity entity) {
        List<Entity> entities = dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_WORKSTATION_TYPE_COMPONENT)
                .find()
                .add(SearchRestrictions.ne("id", entity.getId()))
                .add(SearchRestrictions.and(
                        SearchRestrictions.belongsTo(WorkstationTypeComponentFields.PRODUCTIONLINE,
                                entity.getBelongsToField(WorkstationTypeComponentFields.PRODUCTIONLINE)),
                        SearchRestrictions.belongsTo(WorkstationTypeComponentFields.WORKSTATIONTYPE,
                                entity.getBelongsToField(WorkstationTypeComponentFields.WORKSTATIONTYPE)))).list().getEntities();

        if (!entities.isEmpty()) {
            entity.addError(dataDefinition.getField(WorkstationTypeComponentFields.WORKSTATIONTYPE),
                    "productionLines.workstationTypeComponent.workstationType.workstationExist",
                    entity.getBelongsToField(WorkstationTypeComponentFields.WORKSTATIONTYPE).getStringField("name"));
        }
    }
}
