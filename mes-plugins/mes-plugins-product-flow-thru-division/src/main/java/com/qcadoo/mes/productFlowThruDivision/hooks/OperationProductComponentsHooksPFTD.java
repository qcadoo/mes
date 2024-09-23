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
package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.productFlowThruDivision.constants.OperationProductInComponentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionFlowComponent;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class OperationProductComponentsHooksPFTD {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    public void fillProductionFlow(final DataDefinition technologyDD, final Entity operationProductComponent) {
        if (operationProductComponent.getField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW) == null) {
            operationProductComponent.setField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW,
                    ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue());
        }
    }

    public boolean checkRequiredFields(final DataDefinition dataDefinition, final Entity operationProductComponent) {
        String productionFlow = operationProductComponent.getStringField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW);
        if (operationProductComponent.getId() == null
                && !ProductionFlowComponent.WAREHOUSE.getStringValue().equals(productionFlow)
                && !ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue().equals(productionFlow)) {
            operationProductComponent.addError(dataDefinition.getField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            return false;
        }
        Entity productsFlowLocation = operationProductComponent
                .getBelongsToField(OperationProductInComponentFieldsPFTD.PRODUCTS_FLOW_LOCATION);
        if (ProductionFlowComponent.WAREHOUSE.getStringValue().equals(productionFlow) && productsFlowLocation == null) {
            operationProductComponent.addError(
                    dataDefinition.getField(OperationProductInComponentFieldsPFTD.PRODUCTS_FLOW_LOCATION),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            return false;
        }
        return true;
    }
}
