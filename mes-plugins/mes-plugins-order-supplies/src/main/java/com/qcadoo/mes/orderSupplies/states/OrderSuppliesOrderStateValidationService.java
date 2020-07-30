/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.orderSupplies.states;

import com.qcadoo.mes.materialRequirements.constants.OrderFieldsMR;
import com.qcadoo.mes.orders.constants.InputProductsRequiredForType;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.Entity;

import org.springframework.stereotype.Service;

@Service
public class OrderSuppliesOrderStateValidationService {

    public void validationOnAccepted(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();

        String inputProductsRequiredForType = order.getStringField(OrderFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE);
        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (InputProductsRequiredForType.START_OPERATIONAL_TASK.getStringValue().equals(inputProductsRequiredForType)
                && !TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {
            stateChangeContext.addValidationError("orders.order.typeOfProductionRecording.error.typeIsntForEach");
        }
    }

}
