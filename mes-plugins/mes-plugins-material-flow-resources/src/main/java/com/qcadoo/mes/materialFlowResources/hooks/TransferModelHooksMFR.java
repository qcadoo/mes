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

import static com.qcadoo.mes.materialFlowResources.constants.TransferFieldsMFR.BATCH;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class TransferModelHooksMFR {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void generateBatch(final DataDefinition transferDD, final Entity transfer) {
        transfer.setField(BATCH, generateBatchForTransfer(MaterialFlowConstants.MODEL_TRANSFER));
    }

    public void manageResources(final DataDefinition transferDD, final Entity transfer) {
        materialFlowResourcesService.manageResources(transfer);
    }

    private String generateBatchForTransfer(final String model) {
        String batch = numberGeneratorService.generateNumber(MaterialFlowConstants.PLUGIN_IDENTIFIER, model);

        Long parsedNumber = Long.parseLong(batch);

        while (batchAlreadyExist(model, batch)) {
            parsedNumber++;

            batch = String.format("%03d", parsedNumber);
        }

        return batch;
    }

    public boolean batchAlreadyExist(final String model, final String batch) {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, model).find()
                .add(SearchRestrictions.eq(BATCH, batch)).setMaxResults(1).uniqueResult() != null;
    }

}
