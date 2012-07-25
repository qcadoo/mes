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
package com.qcadoo.mes.materialFlowResources.hooks;

import static com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants.MODEL_TRANSFER;
import static com.qcadoo.mes.materialFlowResources.constants.TransferFieldsMFR.BATCH;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TransferModelHooksMFR {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void generateBatch(final DataDefinition transferDD, final Entity transfer) {
        if (transfer.getField(BATCH) == null) {
            transfer.setField(BATCH, materialFlowResourcesService.generateBatchForTransfer(MODEL_TRANSFER));
        }
    }

    public void manageResources(final DataDefinition transferDD, final Entity transfer) {
        materialFlowResourcesService.manageResources(transfer);
    }

}
