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
package com.qcadoo.mes.technologies.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationDetailsHooks {

    private static final String L_WORKSTATIONS = "workstations";

    private static final String L_ADD_UP_THE_NUMBER_OF_WORKSTATIONS = "addUpTheNumberOfWorkstations";

    private static final String L_WORKSTATION_LOOKUP = "workstationLookup";

    private static final String L_PRODUCT_IN_OUT_COMPONENTS_TAB = "productInOutComponents";

    private static final List<String> L_WORKSTATIONS_TAB_FIELDS = Arrays.asList(OperationFields.ASSIGNED_TO_OPERATION,
            OperationFields.QUANTITY_OF_WORKSTATIONS);

    private static final List<String> L_WORKSTATIONS_TAB_LOOKUPS = Arrays.asList(
            OperationFields.DIVISION, OperationFields.WORKSTATION_TYPE);

    public static final String L_CREATE_OPERATION_OUTPUT = "createOperationOutput";

    public static final String L_PRODUCT = "product";

    @Autowired
    private SecurityService securityService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        disableWorkstationsTabFieldsIfOperationIsNotSaved(view);
        setWorkstationsCriteriaModifiers(view);
        hideProductInOutComponents(view);
        disableCreateOperationOutput(view);
        ComponentState skillsTab = view.getComponentByReference("skillsTab");
        skillsTab.setVisible(securityService.hasCurrentUserRole("ROLE_SKILLS"));
    }

    private void disableCreateOperationOutput(final ViewDefinitionState view) {
        CheckBoxComponent createOperationOutput = (CheckBoxComponent) view.getComponentByReference(L_CREATE_OPERATION_OUTPUT);
        LookupComponent productLookupComponent = (LookupComponent) view.getComponentByReference(L_PRODUCT);
        createOperationOutput.setEnabled(productLookupComponent.isEmpty() || !Objects.nonNull(productLookupComponent.getEntity()));
    }

    private void hideProductInOutComponents(final ViewDefinitionState view) {
        ComponentState tabComponent = view.getComponentByReference(L_PRODUCT_IN_OUT_COMPONENTS_TAB);

        if (tabComponent != null) {
            tabComponent.setVisible(false);
        }
    }

    private void setWorkstationsCriteriaModifiers(final ViewDefinitionState view) {
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(OperationFields.DIVISION);
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(L_WORKSTATION_LOOKUP);
        GridComponent workstations = (GridComponent) view.getComponentByReference(OperationFields.WORKSTATIONS);

        Entity division = divisionLookup.getEntity();
        FilterValueHolder filter = workstationLookup.getFilterValue();

        if (division != null) {
            filter.put(OperationFields.DIVISION, division.getId());
            workstations.setEditable(true);
        } else {
            filter.remove(OperationFields.DIVISION);
            workstations.setEditable(false);
        }

        workstationLookup.setFilterValue(filter);
    }

    public void clearWorkstationsField(final ViewDefinitionState view) {
        GridComponent workstations = (GridComponent) view.getComponentByReference(OperationFields.WORKSTATIONS);
        FormComponent operationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity operation = operationForm.getEntity();
        List<Entity> entities = Lists.newArrayList();

        workstations.setEntities(entities);
        workstations.setFieldValue(null);

        operation.setField(OperationFields.WORKSTATIONS, null);

        Entity savedOperation = operation.getDataDefinition().save(operation);
        operationForm.setEntity(savedOperation);
    }

    public void clearLookupField(final ViewDefinitionState view, String fieldName) {
        LookupComponent lookupComponent = (LookupComponent) view.getComponentByReference(fieldName);

        lookupComponent.setFieldValue(null);
        lookupComponent.requestComponentUpdateState();
    }

    private void disableWorkstationsTabFieldsIfOperationIsNotSaved(ViewDefinitionState view) {
        FormComponent operationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent workstations = (GridComponent) view.getComponentByReference(OperationFields.WORKSTATIONS);

        if (operationForm.getEntityId() == null) {
            changedEnabledFields(view, L_WORKSTATIONS_TAB_FIELDS, false);
            changeEnabledLookups(view, L_WORKSTATIONS_TAB_LOOKUPS, Lists.newArrayList(""));
            workstations.setEnabled(false);
        } else {
            changedEnabledFields(view, L_WORKSTATIONS_TAB_FIELDS, true);
            changeEnabledLookups(view, L_WORKSTATIONS_TAB_LOOKUPS, L_WORKSTATIONS_TAB_LOOKUPS);
            workstations.setEnabled(true);
            setWorkstationsTabFields(view);
        }
    }

    private void changedEnabledFields(final ViewDefinitionState view, final List<String> references, final boolean enabled) {
        for (String reference : references) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);
            fieldComponent.setEnabled(enabled);
        }
    }

    public void setWorkstationsTabFields(final ViewDefinitionState view) {
        FieldComponent assignedToOperation = (FieldComponent) view.getComponentByReference(OperationFields.ASSIGNED_TO_OPERATION);
        String assignedToOperationValue = (String) assignedToOperation.getFieldValue();
        GridComponent workstations = (GridComponent) view.getComponentByReference(OperationFields.WORKSTATIONS);

        if (AssignedToOperation.WORKSTATIONS.getStringValue().equals(assignedToOperationValue)) {
            changeEnabledLookups(view, L_WORKSTATIONS_TAB_LOOKUPS,
                    Lists.newArrayList(OperationFields.DIVISION));
            workstations.setEnabled(true);
            enableRibbonItem(view, !workstations.getEntities().isEmpty());
        } else if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperationValue)) {
            changeEnabledLookups(view, L_WORKSTATIONS_TAB_LOOKUPS, Lists.newArrayList(OperationFields.WORKSTATION_TYPE));
            workstations.setEnabled(false);
            enableRibbonItem(view, false);
        }
    }

    private void changeEnabledLookups(final ViewDefinitionState view, final List<String> fields,
                                      final List<String> enabledFields) {
        for (String field : fields) {
            LookupComponent lookupComponent = (LookupComponent) view.getComponentByReference(field);
            lookupComponent.setEnabled(enabledFields.contains(field));
        }
    }

    private void enableRibbonItem(final ViewDefinitionState view, final boolean enable) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonActionItem addUpTheNumberOfWorkstations = window.getRibbon().getGroupByName(L_WORKSTATIONS)
                .getItemByName(L_ADD_UP_THE_NUMBER_OF_WORKSTATIONS);

        addUpTheNumberOfWorkstations.setEnabled(enable);
        addUpTheNumberOfWorkstations.requestUpdate(true);
    }

}
