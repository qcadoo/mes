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
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.constants.QcadooViewConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TOCDetailsHooks {

    private static final String L_NAVIGATION = "navigation";

    private static final String L_WORKSTATIONS = "workstations";

    private static final String L_ADD_UP_THE_NUMBER_OF_WORKSTATIONS = "addUpTheNumberOfWorkstations";

    private static final String L_WORKSTATION_LOOKUP = "workstationLookup";

    private static final List<String> L_WORKSTATIONS_TAB_FIELDS = Arrays.asList(
            TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION,
            TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS);

    private static final List<String> L_WORKSTATIONS_TAB_LOOKUPS = Arrays.asList(
            TechnologyOperationComponentFields.DIVISION,
            TechnologyOperationComponentFields.WORKSTATION_TYPE);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        disableWorkstationsTabFieldsIfOperationIsNotSaved(view);
        setWorkstationsCriteriaModifiers(view);
        disableViewForState(view);
    }

    private void disableViewForState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity toc = form.getEntity();

        if (toc.getId() == null) {
            return;
        }

        toc = getTechnologyOperationComponentDD().get(toc.getId());

        Entity technology = toc.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);

        if (!TechnologyState.DRAFT.getStringValue().equals(technology.getStringField(TechnologyFields.STATE))) {
            WindowComponent windowComponent = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
            Ribbon ribbon = windowComponent.getRibbon();

            ribbon.getGroups().stream().filter(group -> !group.getName().equals(L_NAVIGATION))
                    .forEach(group -> group.getItems().forEach(item -> {
                        if(!item.getName().equals("save") && !item.getName().equals("saveBack")) {
                            item.setEnabled(false);
                            item.requestUpdate(true);
                        }
                    }));
            form.setFormEnabled(false);
            FieldComponent attachment = (FieldComponent) view.getComponentByReference(TechnologyOperationComponentFields.ATTACHMENT);
            attachment.setEnabled(true);
            attachment.requestComponentUpdateState();
            GridComponent workstationsGrid = (GridComponent) view.getComponentByReference("workstations");
            workstationsGrid.setEnabled(false);
        }
    }

    private void setWorkstationsCriteriaModifiers(final ViewDefinitionState view) {
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(TechnologyOperationComponentFields.DIVISION);
        LookupComponent workstationLookup = (LookupComponent) view.getComponentByReference(L_WORKSTATION_LOOKUP);
        GridComponent workstations = (GridComponent) view.getComponentByReference(TechnologyOperationComponentFields.WORKSTATIONS);

        Entity division = divisionLookup.getEntity();

        FilterValueHolder filter = workstationLookup.getFilterValue();

        if (division != null) {
            filter.put(TechnologyOperationComponentFields.DIVISION, division.getId());
            workstations.setEditable(true);
        } else {
            filter.remove(TechnologyOperationComponentFields.DIVISION);
            workstations.setEditable(false);
        }

        workstationLookup.setFilterValue(filter);
    }

    private void disableWorkstationsTabFieldsIfOperationIsNotSaved(ViewDefinitionState view) {
        FormComponent operationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent workstations = (GridComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.WORKSTATIONS);

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

    private void changeEnabledLookups(final ViewDefinitionState view, final List<String> fields,
                                      final List<String> enabledFields) {
        for (String field : fields) {
            LookupComponent lookupComponent = (LookupComponent) view.getComponentByReference(field);
            lookupComponent.setEnabled(enabledFields.contains(field));
        }
    }

    public void setWorkstationsTabFields(final ViewDefinitionState view) {
        FieldComponent assignedToOperation = (FieldComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION);
        String assignedToOperationValue = (String) assignedToOperation.getFieldValue();
        GridComponent workstations = (GridComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.WORKSTATIONS);

        if (AssignedToOperation.WORKSTATIONS.getStringValue().equals(assignedToOperationValue)) {
            changeEnabledLookups(view, L_WORKSTATIONS_TAB_LOOKUPS,
                    Lists.newArrayList(OperationFields.DIVISION));
            workstations.setEnabled(true);
            enableRibbonItem(view, !workstations.getEntities().isEmpty());
        } else if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperationValue)) {
            changeEnabledLookups(view, L_WORKSTATIONS_TAB_LOOKUPS,
                    Lists.newArrayList(TechnologyOperationComponentFields.WORKSTATION_TYPE));
            workstations.setEnabled(false);
            enableRibbonItem(view, false);
        }
    }

    public void clearWorkstationsField(final ViewDefinitionState view) {
        GridComponent workstations = (GridComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.WORKSTATIONS);
        FormComponent operationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity operation = operationForm.getEntity();

        List<Entity> entities = Lists.newArrayList();

        workstations.setEntities(entities);
        workstations.setFieldValue(null);

        operation.setField(OperationFields.WORKSTATIONS, null);

        Entity savedOperation = operation.getDataDefinition().save(operation);
        operationForm.setEntity(savedOperation);
    }

    public void clearLookupField(final ViewDefinitionState view, final String fieldName) {
        LookupComponent lookupComponent = (LookupComponent) view.getComponentByReference(fieldName);

        lookupComponent.setFieldValue(null);
        lookupComponent.requestComponentUpdateState();
    }

    private void enableRibbonItem(final ViewDefinitionState view, final boolean enable) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonActionItem addUpTheNumberOfWorkstations = window.getRibbon().getGroupByName(L_WORKSTATIONS)
                .getItemByName(L_ADD_UP_THE_NUMBER_OF_WORKSTATIONS);

        addUpTheNumberOfWorkstations.setEnabled(enable);
        addUpTheNumberOfWorkstations.requestUpdate(true);
    }

    private DataDefinition getTechnologyOperationComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

}
