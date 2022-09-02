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
package com.qcadoo.mes.technologies.listeners;

import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.hooks.TOCDetailsHooks;
import com.qcadoo.mes.technologies.hooks.TechnologyOperationComponentHooks;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TOCDetailsListeners {

    @Autowired
    private NumberService numberService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TOCDetailsHooks tOCDetailsHooks;

    @Autowired
    private TechnologyOperationComponentHooks technologyOperationComponentHooks;

    public void copyWorkstationsSettingsFromOperation(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent technologyOperationComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent workstationsGrid = (GridComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.WORKSTATIONS);

        Entity technologyOperationComponent = technologyOperationComponentForm.getEntity();

        Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

        if (Objects.nonNull(operation)) {
            technologyOperationComponentHooks.copyWorkstationsSettingsFromOperation(technologyOperationComponent);
            technologyService.copyCommentAndAttachmentFromLowerInstance(technologyOperationComponent, TechnologyOperationComponentFields.OPERATION);

            technologyOperationComponent.setField(TechnologyOperationComponentFields.WORKSTATIONS, null);
            workstationsGrid.setEntities(operation.getManyToManyField(OperationFields.WORKSTATIONS));
            technologyOperationComponentForm.setEntity(technologyOperationComponent);
        }
    }

    public void setWorkstationsTabFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        tOCDetailsHooks.setWorkstationsTabFields(view);

        FieldComponent assignedToOperation = (FieldComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION);
        String assignedToOperationValue = (String) assignedToOperation.getFieldValue();

        if (AssignedToOperation.WORKSTATIONS.getStringValue().equals(assignedToOperationValue)) {
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.WORKSTATION_TYPE);
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.DIVISION);
        } else if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperationValue)) {
            tOCDetailsHooks.clearWorkstationsField(view);
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.DIVISION);
        }
    }

    public void addUpTheNumberOfWorkstations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent technologyOperationComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        int size = technologyOperationComponentForm.getPersistedEntityWithIncludedFormValues()
                .getHasManyField(TechnologyOperationComponentFields.WORKSTATIONS).size();

        FieldComponent quantityOfWorkstations = (FieldComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS);

        quantityOfWorkstations.setFieldValue(size);
        quantityOfWorkstations.requestComponentUpdateState();
    }

    public void fillProductDataFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent operationLookup = (LookupComponent) view.getComponentByReference(TechnologyOperationComponentFields.OPERATION);

        CheckBoxComponent showInProductData = (CheckBoxComponent) view.getComponentByReference(TechnologyOperationComponentFields.SHOW_IN_PRODUCT_DATA);
        FieldComponent productDataNumber = (FieldComponent) view.getComponentByReference(TechnologyOperationComponentFields.PRODUCT_DATA_NUMBER);

        Entity operation = operationLookup.getEntity();

        if (Objects.isNull(operation)) {
            showInProductData.setChecked(false);
            productDataNumber.setFieldValue(null);
        } else {
            showInProductData.setChecked(operation.getBooleanField(OperationFields.SHOW_IN_PRODUCT_DATA));
            productDataNumber.setFieldValue(numberService.formatWithMinimumFractionDigits(operation
                    .getField(OperationFields.PRODUCT_DATA_NUMBER), 0));
        }

        showInProductData.requestComponentUpdateState();
        productDataNumber.requestComponentUpdateState();
    }

}
