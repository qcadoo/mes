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
package com.qcadoo.mes.productFlowThruDivision.validators;

import com.qcadoo.mes.productFlowThruDivision.constants.ProductionFlowComponent;
import com.qcadoo.mes.productFlowThruDivision.constants.TechnologyFieldsPFTD;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class TechnologyValidatorsPFTD {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    public boolean checkRequiredFields(final DataDefinition dataDefinition, final Entity technology) {
        String productionFlow = technology.getStringField(TechnologyFieldsPFTD.PRODUCTION_FLOW);
        if (technology.getId() == null && !ProductionFlowComponent.WAREHOUSE.getStringValue().equals(productionFlow)
                && !ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue().equals(productionFlow)) {
            technology.addError(dataDefinition.getField(TechnologyFieldsPFTD.PRODUCTION_FLOW),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);

            return false;
        }
        Entity productsFlowLocation = technology.getBelongsToField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION);
        if (ProductionFlowComponent.WAREHOUSE.getStringValue().equals(productionFlow) && productsFlowLocation == null) {
            technology.addError(dataDefinition.getField(TechnologyFieldsPFTD.PRODUCTS_FLOW_LOCATION),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            return false;
        }
        return checkIfWarehousesAreDifferent(technology);
    }

    private boolean checkIfWarehousesAreDifferent(final Entity technology) {
        Entity componentsLocation = technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_LOCATION);
        Entity componentsOutLocation = technology.getBelongsToField(TechnologyFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);

        if (componentsLocation != null && componentsOutLocation != null
                && componentsLocation.getId().equals(componentsOutLocation.getId())) {
            technology.addGlobalError("technologies.technology.error.componentsLocationsAreSame", false);
            return false;
        }
        return true;
    }

}
