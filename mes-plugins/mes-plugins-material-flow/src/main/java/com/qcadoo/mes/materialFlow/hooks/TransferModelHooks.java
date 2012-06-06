/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.materialFlow.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlow.MaterialFlowResourceService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TransferModelHooks {

    @Autowired
    private MaterialFlowResourceService matersialFlowResourceService;

    public void copyProductionOrConsumptionDataFromBelongingTransformation(final DataDefinition dd, final Entity transfer) {
        Entity transformation = transfer.getBelongsToField("transformationsProduction");

        if (transformation == null) {
            transformation = transfer.getBelongsToField("transformationsConsumption");

            if (transformation == null) {
                // came here from plain transfer detail view
                return;
            } else {
                transfer.setField("type", "Consumption");
                transfer.setField("stockAreasFrom", transformation.getBelongsToField("stockAreasFrom"));
            }
        } else {
            transfer.setField("type", "Production");
            transfer.setField("stockAreasTo", transformation.getBelongsToField("stockAreasTo"));
        }

        transfer.setField("time", transformation.getField("time"));
        transfer.setField("staff", transformation.getBelongsToField("staff"));
    }

    public void manageResources(final DataDefinition transferDD, final Entity transfer) {
        matersialFlowResourceService.manageResources(transfer);
    }
}
