/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.techSubcontracting.constants.TechnologyInstanceOperCompFieldsTS;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OperationalTaskDetailsListenersOTFOOverrideUtil {

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    public void checkIfOperationIsSubcontracted(final ViewDefinitionState view) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(L_TECHNOLOGY_OPERATION_COMPONENT);

        technologyOperationComponentLookup.setFieldValue(null);
        technologyOperationComponentLookup.requestComponentUpdateState();

        fillProductionLineField(view);
    }

    public void setOperationalNameAndDescriptionForSubcontractedOperation(final ViewDefinitionState view) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(L_TECHNOLOGY_OPERATION_COMPONENT);
        FieldComponent nameField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.NAME);
        FieldComponent descriptionField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.DESCRIPTION);

        Entity technologyOperationComponent = technologyOperationComponentLookup.getEntity();

        if (technologyOperationComponent == null) {
            nameField.setFieldValue(null);
            descriptionField.setFieldValue(null);
        } else {
            Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

            nameField.setFieldValue(operation.getStringField(OperationFields.NAME));
            descriptionField.setFieldValue(technologyOperationComponent.getStringField(OperationFields.COMMENT));

            fillProductionLineField(view);
        }

        nameField.requestComponentUpdateState();
        descriptionField.requestComponentUpdateState();
    }

    private void fillProductionLineField(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(OperationalTaskFieldsOTFO.ORDER);
        FieldComponent nameField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.NAME);
        FieldComponent descriptionField = (FieldComponent) view.getComponentByReference(OperationalTaskFields.DESCRIPTION);
        LookupComponent productionLineLookup = (LookupComponent) view
                .getComponentByReference(OperationalTaskFields.PRODUCTION_LINE);

        Entity order = orderLookup.getEntity();

        if ((order == null) || isSubcontracting(view)) {
            nameField.setFieldValue(null);
            descriptionField.setFieldValue(null);
            productionLineLookup.setFieldValue(null);
        } else {
            Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

            productionLineLookup.setFieldValue(productionLine.getId());
        }

        nameField.requestComponentUpdateState();
        descriptionField.requestComponentUpdateState();
        productionLineLookup.requestComponentUpdateState();
    }

    private boolean isSubcontracting(final ViewDefinitionState viewDefinitionState) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) viewDefinitionState
                .getComponentByReference(L_TECHNOLOGY_OPERATION_COMPONENT);

        Entity technologyOperationComponent = technologyOperationComponentLookup.getEntity();

        return ((technologyOperationComponent != null) && technologyOperationComponent
                .getBooleanField(TechnologyInstanceOperCompFieldsTS.IS_SUBCONTRACTING));
    }

}
