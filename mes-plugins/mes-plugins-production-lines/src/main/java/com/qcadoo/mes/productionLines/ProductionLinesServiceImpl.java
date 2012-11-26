/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.productionLines;

import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.QUANTITY_FOR_OTHER_WORKSTATION_TYPES;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.WORKSTATION_TYPE_COMPONENTS;
import static com.qcadoo.mes.productionLines.constants.WorkstationTypeComponentFields.QUANTITY;
import static com.qcadoo.mes.technologies.constants.OperationFields.WORKSTATION_TYPE;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.OPERATION;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public class ProductionLinesServiceImpl implements ProductionLinesService {

    public Integer getWorkstationTypesCount(final Entity operationComponent, final Entity productionLine) {
        List<Entity> workstationTypeComponents = productionLine.getHasManyField(WORKSTATION_TYPE_COMPONENTS);

        Entity desiredWorkstation = operationComponent.getBelongsToField(OPERATION).getBelongsToField(WORKSTATION_TYPE);

        if (desiredWorkstation != null) {
            for (Entity workstationTypeComponent : workstationTypeComponents) {
                Entity workstation = workstationTypeComponent.getBelongsToField(WORKSTATION_TYPE);

                // FIXME mici, proxy entity equals thing
                if (desiredWorkstation.getId().equals(workstation.getId())) {
                    return (Integer) workstationTypeComponent.getField(QUANTITY);
                }
            }
        }

        return (Integer) productionLine.getField(QUANTITY_FOR_OTHER_WORKSTATION_TYPES);
    }

}
