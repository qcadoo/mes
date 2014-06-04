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
package com.qcadoo.mes.techSubcontrForOperTasks.aop;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.techSubcontracting.constants.TechnologyInstanceOperCompFieldsTS;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OperationalTaskDetailsListenersOTFOOverrideUtil {

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    public void setOperationalTaskNameDescriptionAndProductionLineForSubcontracted(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.ORDER);
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(L_TECHNOLOGY_OPERATION_COMPONENT);
        FieldComponent nameField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.NAME);
        FieldComponent descriptionField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.DESCRIPTION);
        LookupComponent productionLineLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFields.PRODUCTION_LINE);

        Entity order = orderLookup.getEntity();
        Entity technologyOperationComponent = technologyOperationComponentLookup.getEntity();

        if ((order == null) || (technologyOperationComponent == null) || isSubcontracting(technologyOperationComponent)) {
            nameField.setFieldValue(null);
            descriptionField.setFieldValue(null);
            productionLineLookup.setFieldValue(null);

            nameField.requestComponentUpdateState();
            descriptionField.requestComponentUpdateState();
            productionLineLookup.requestComponentUpdateState();
        }
    }

    private boolean isSubcontracting(final Entity technologyOperationComponent) {
        return ((technologyOperationComponent != null) && technologyOperationComponent
                .getBooleanField(TechnologyInstanceOperCompFieldsTS.IS_SUBCONTRACTING));
    }

}
