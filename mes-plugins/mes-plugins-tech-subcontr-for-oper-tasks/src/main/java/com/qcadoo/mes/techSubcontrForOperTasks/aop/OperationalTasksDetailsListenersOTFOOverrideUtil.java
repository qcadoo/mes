/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFRFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OperationalTasksDetailsListenersOTFOOverrideUtil {

    public void checkIfOperationIsSubcontracted(final ViewDefinitionState viewDefinitionState) {
        LookupComponent technologyLookup = (LookupComponent) viewDefinitionState
                .getComponentByReference(OperationalTasksOTFRFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);
        technologyLookup.setFieldValue(null);
        technologyLookup.requestComponentUpdateState();

        fillProductionLineField(viewDefinitionState);
    }

    public void setOperationalNameAndDescriptionForSubcontractedOperation(final ViewDefinitionState viewDefinitionState) {
        Entity techInstOperComp = ((LookupComponent) viewDefinitionState
                .getComponentByReference(OperationalTasksOTFRFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)).getEntity();
        FieldComponent description = (FieldComponent) viewDefinitionState
                .getComponentByReference(OperationalTasksFields.DESCRIPTION);
        FieldComponent name = (FieldComponent) viewDefinitionState.getComponentByReference(OperationalTasksFields.NAME);
        if (techInstOperComp == null) {
            description.setFieldValue(null);
            name.setFieldValue(null);
        } else {
            fillProductionLineField(viewDefinitionState);
            description.setFieldValue(techInstOperComp.getStringField(TechnologyInstanceOperCompFields.COMMENT));
            name.setFieldValue(techInstOperComp.getBelongsToField(TechnologyInstanceOperCompFields.OPERATION).getStringField(
                    OperationFields.NAME));
        }
        description.requestComponentUpdateState();
        name.requestComponentUpdateState();
    }

    private void fillProductionLineField(final ViewDefinitionState viewDefinitionState) {
        Entity order = ((LookupComponent) viewDefinitionState.getComponentByReference(OperationalTasksOTFRFields.ORDER))
                .getEntity();
        FieldComponent productionLine = (FieldComponent) viewDefinitionState
                .getComponentByReference(OperationalTasksFields.PRODUCTION_LINE);
        if (order == null || isSubcontracting(viewDefinitionState)) {
            productionLine.setFieldValue(null);
            ((FieldComponent) viewDefinitionState.getComponentByReference(OperationalTasksFields.DESCRIPTION))
                    .setFieldValue(null);
            ((FieldComponent) viewDefinitionState.getComponentByReference(OperationalTasksFields.NAME)).setFieldValue(null);
        } else {
            productionLine.setFieldValue(order.getBelongsToField(OrderFields.PRODUCTION_LINE).getId());
        }
        productionLine.requestComponentUpdateState();
    }

    private boolean isSubcontracting(final ViewDefinitionState viewDefinitionState) {
        Entity techInstOperComp = ((LookupComponent) viewDefinitionState
                .getComponentByReference(OperationalTasksOTFRFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)).getEntity();
        return techInstOperComp != null && techInstOperComp.getBooleanField("isSubcontracting");
    }
}
