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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.hooks.TOCDetailsHooks;
import com.qcadoo.mes.technologies.hooks.TechnologyOperationComponentHooks;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class TOCDetailsListeners {

    private static final String L_FORM = "form";

    private static final String L_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE = "operationProductInComponentsSource";

    private static final String L_LINE_SEPARATOR = "line.separator";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_IS_EMPTY = "technologies.technologyOperationComponent.import.operationProductInComponentsSource.isEmpty";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_INCORRECT_LINE = "technologies.technologyOperationComponent.import.operationProductInComponentsSource.incorrectLine";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_INCORRECT_QUANTITY = "technologies.technologyOperationComponent.import.operationProductInComponentsSource.incorrectQuantity";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_INCORRCT_NUMBER = "technologies.technologyOperationComponent.import.operationProductInComponentsSource.incorrectNumber";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_PRODUCT_HAS_ALREADY_BEEN_ADDED = "technologies.technologyOperationComponent.import.operationProductInComponentsSource.productHasAlreadyBeenAdded";

    private static final String L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_INFO = "technologies.technologyOperationComponent.import.info";

    public static final int BASE_IMPORT_POSITIONS_LENGTH = 2;

    public static final int EXTEND_IMPORT_POSITIONS_LENGTH = 3;

    private enum ErrorMessageType {

        INCORRECT_LINE(
                L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_INCORRECT_LINE), INCORRECT_NUMBER(
                        L_TECHNOLOGIES_TECHNOLOGY_OPERATION_COMPONENT_IMPORT_OPERATION_PRODUCT_IN_COMPONENTS_SOURCE_INCORRCT_NUMBER);

        private final String errorMessageType;

        private ErrorMessageType(final String errorMessageType) {
            this.errorMessageType = errorMessageType;
        }

        public String getStringValue() {
            return errorMessageType;
        }

    }

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TOCDetailsHooks tOCDetailsHooks;

    @Autowired
    private TechnologyOperationComponentHooks technologyOperationComponentHooks;

    @Autowired
    private TechnologyService technologyService;

    public void copyWorkstationsSettingsFromOperation(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(L_FORM);
        Entity toc = formComponent.getEntity();

        Entity operation = toc.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

        if (operation != null) {
            GridComponent workstationsGrid = (GridComponent) view
                    .getComponentByReference(TechnologyOperationComponentFields.WORKSTATIONS);

            technologyOperationComponentHooks.copyWorkstationsSettingsFromOperation(toc);
            technologyService.copyCommentAndAttachmentFromLowerInstance(toc, TechnologyOperationComponentFields.OPERATION);

            toc.setField(TechnologyOperationComponentFields.WORKSTATIONS, null);
            workstationsGrid.setEntities(operation.getManyToManyField(OperationFields.WORKSTATIONS));
            formComponent.setEntity(toc);
        }
    }

    public void setProductionLineLookup(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        tOCDetailsHooks.setProductionLineLookup(view);
    }

    public void setWorkstationsLookup(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        tOCDetailsHooks.setWorkstationsLookup(view);
    }

    public void setWorkstationsTabFields(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        tOCDetailsHooks.setWorkstationsTabFields(view);

        FieldComponent assignedToOperation = (FieldComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION);
        String assignedToOperationValue = (String) assignedToOperation.getFieldValue();

        if (AssignedToOperation.WORKSTATIONS.getStringValue().equals(assignedToOperationValue)) {
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.WORKSTATION_TYPE);
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.DIVISION);
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.PRODUCTION_LINE);
        } else if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperationValue)) {
            tOCDetailsHooks.clearWorkstationsField(view);
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.DIVISION);
            tOCDetailsHooks.clearLookupField(view, TechnologyOperationComponentFields.PRODUCTION_LINE);
        }
    }

    public void addUpTheNumberOfWorkstations(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        int size = form.getPersistedEntityWithIncludedFormValues()
                .getHasManyField(TechnologyOperationComponentFields.WORKSTATIONS).size();

        FieldComponent quantityOfWorkstations = (FieldComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS);

        quantityOfWorkstations.setFieldValue(size);
        quantityOfWorkstations.requestComponentUpdateState();
    }

}
