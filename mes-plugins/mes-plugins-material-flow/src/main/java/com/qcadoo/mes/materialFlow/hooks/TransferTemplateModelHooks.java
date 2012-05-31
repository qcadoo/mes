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

import static com.qcadoo.mes.materialFlow.constants.TransferTemplateFields.STOCK_AREAS_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferTemplateFields.STOCK_AREAS_TO;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TransferTemplateModelHooks {

    public boolean checkIfOneOfStockAreasIsNotNull(final DataDefinition transferTemplateDD, final Entity transferTemplate) {
        Entity stockAreaFrom = transferTemplate.getBelongsToField(STOCK_AREAS_FROM);
        Entity stockAreaTo = transferTemplate.getBelongsToField(STOCK_AREAS_TO);

        if ((stockAreaFrom == null) && (stockAreaTo == null)) {
            transferTemplate.addError(transferTemplateDD.getField(STOCK_AREAS_FROM),
                    "materialFlow.validate.global.error.fillAtLeastOneStockAreas");
            transferTemplate.addError(transferTemplateDD.getField(STOCK_AREAS_TO),
                    "materialFlow.validate.global.error.fillAtLeastOneStockAreas");

            return false;
        }
        return true;
    }
}
