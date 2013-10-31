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

import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.DESCRIPTION;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.NAME;
import static com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields.PRODUCTION_LINE;
import static com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksFieldsOTFOF.ORDER;
import static com.qcadoo.mes.techSubcontracting.constants.TechnologyInstanceOperCompFieldsTS.IS_SUBCONTRACTING;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.COMMENT;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.OPERATION;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OperationalTasksDetailsListenersOTFOOverrideUtil {

    public void checkIfOperationIsSubcontracted(final ViewDefinitionState viewDefinitionState) {
        LookupComponent technologyLookup = (LookupComponent) viewDefinitionState
                .getComponentByReference("technologyOperationComponent");
        technologyLookup.setFieldValue(null);
        technologyLookup.requestComponentUpdateState();

        fillProductionLineField(viewDefinitionState);
    }

    public void setOperationalNameAndDescriptionForSubcontractedOperation(final ViewDefinitionState viewDefinitionState) {
        Entity techInstOperComp = ((LookupComponent) viewDefinitionState.getComponentByReference("technologyOperationComponent"))
                .getEntity();
        FieldComponent description = (FieldComponent) viewDefinitionState.getComponentByReference(DESCRIPTION);
        FieldComponent name = (FieldComponent) viewDefinitionState.getComponentByReference(NAME);
        if (techInstOperComp == null) {
            description.setFieldValue(null);
            name.setFieldValue(null);
        } else {
            fillProductionLineField(viewDefinitionState);
            description.setFieldValue(techInstOperComp.getStringField(COMMENT));
            name.setFieldValue(techInstOperComp.getBelongsToField(OPERATION).getStringField(NAME));
        }
        description.requestComponentUpdateState();
        name.requestComponentUpdateState();
    }

    private void fillProductionLineField(final ViewDefinitionState viewDefinitionState) {
        Entity order = ((LookupComponent) viewDefinitionState.getComponentByReference(ORDER)).getEntity();
        FieldComponent productionLine = (FieldComponent) viewDefinitionState.getComponentByReference(PRODUCTION_LINE);
        if (order == null || isSubcontracting(viewDefinitionState)) {
            productionLine.setFieldValue(null);
            ((FieldComponent) viewDefinitionState.getComponentByReference(DESCRIPTION)).setFieldValue(null);
            ((FieldComponent) viewDefinitionState.getComponentByReference(NAME)).setFieldValue(null);
        } else {
            productionLine.setFieldValue(order.getBelongsToField(PRODUCTION_LINE).getId());
        }
        productionLine.requestComponentUpdateState();
    }

    private boolean isSubcontracting(final ViewDefinitionState viewDefinitionState) {
        Entity techInstOperComp = ((LookupComponent) viewDefinitionState.getComponentByReference("technologyOperationComponent"))
                .getEntity();
        return techInstOperComp != null && techInstOperComp.getBooleanField(IS_SUBCONTRACTING);
    }

}
