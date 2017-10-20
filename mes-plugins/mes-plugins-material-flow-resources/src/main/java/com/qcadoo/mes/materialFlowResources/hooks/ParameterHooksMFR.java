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
package com.qcadoo.mes.materialFlowResources.hooks;

import static com.qcadoo.mes.materialFlowResources.constants.ChangeDateWhenTransferToWarehouseType.NEVER;
import static com.qcadoo.mes.materialFlowResources.constants.ParameterFieldsMFR.CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterHooksMFR {

    public void setChangeDateWhenTransferToWarehouseTypeDefaultValue(final DataDefinition dataDefinition, final Entity documentPositionParameters) {
        String changeDateWhenTransferToWarehouseType = documentPositionParameters.getStringField(CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE);

        if (changeDateWhenTransferToWarehouseType == null) {
            documentPositionParameters.setField(CHANGE_DATE_WHEN_TRANSFER_TO_WAREHOUSE_TYPE, NEVER.getStringValue());
        }
    }

}
