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
package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.MultiAssignmentToShiftFields.OCCUPATION_TYPE;
import static com.qcadoo.mes.assignmentToShift.constants.MultiAssignmentToShiftFields.OCCUPATION_TYPE_NAME;
import static com.qcadoo.mes.assignmentToShift.constants.MultiAssignmentToShiftFields.PRODUCTION_LINE;
import static com.qcadoo.mes.assignmentToShift.constants.OccupationType.OTHER_CASE;
import static com.qcadoo.mes.assignmentToShift.constants.OccupationType.WORK_ON_LINE;
import static com.qcadoo.model.constants.DictionaryItemFields.NAME;
import static com.qcadoo.model.constants.DictionaryItemFields.TECHNICAL_CODE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.MultiAssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.criteriaModifiers.StaffCriteriaModifier;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class MultiAssignmentToShiftDetailsHooks {

    public static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StaffCriteriaModifier staffCriteriaModifier;

    public void onBeforeRender(final ViewDefinitionState view) {
        setFieldsEnabledWhenTypeIsSpecific(view);
        enableAddButton(view);
        setStaffFilter(view);
    }

    public void onSave(final DataDefinition multiAssignmentToShiftDD, final Entity multiAssignmentToShift) {
        checkIfProductionLineFilled(multiAssignmentToShiftDD, multiAssignmentToShift);
    }

    private void checkIfProductionLineFilled(DataDefinition multiAssignmentToShiftDD, Entity multiAssignmentToShift) {
        if (multiAssignmentToShift.getId() != null) {
            String occupationType = multiAssignmentToShift.getStringField(MultiAssignmentToShiftFields.OCCUPATION_TYPE);

            Entity dictionaryItem = findDictionaryItemByName(occupationType);

            String technicalCode = dictionaryItem.getStringField(TECHNICAL_CODE);

            if (technicalCode != null && technicalCode.equals(WORK_ON_LINE.getStringValue())) {
                if (multiAssignmentToShift.getBelongsToField(MultiAssignmentToShiftFields.PRODUCTION_LINE) == null) {
                    multiAssignmentToShift.addError(
                            multiAssignmentToShiftDD.getField(MultiAssignmentToShiftFields.PRODUCTION_LINE),
                            "assignmentToShift.staffAssignmentToShift.productionLine.isEmpty");
                }
            }
        }
    }

    private void enableAddButton(ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity multiAssignment = form.getPersistedEntityWithIncludedFormValues();

        GridComponent workersComponent = (GridComponent) view.getComponentByReference("workers");
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup add = (RibbonGroup) window.getRibbon().getGroupByName("add");
        RibbonActionItem addMany = (RibbonActionItem) add.getItemByName("addManyWorkers");
        if (workersComponent.getEntities().isEmpty() || !multiAssignment.isValid()) {
            addMany.setEnabled(false);
            addMany.requestUpdate(true);
        } else {
            addMany.setEnabled(true);
            addMany.requestUpdate(true);
        }
    }

    private void setStaffFilter(ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity multiAssignment = form.getPersistedEntityWithIncludedFormValues();
        Entity assignment = multiAssignment.getBelongsToField("assignmentToShift");
        if (assignment != null) {
            Entity crew = assignment.getBelongsToField(AssignmentToShiftFields.CREW);

            LookupComponent staffLookup = (LookupComponent) view.getComponentByReference("staffLookup");

            staffCriteriaModifier.putCrewNumber(staffLookup, crew);
        }
    }

    public void setFieldsEnabledWhenTypeIsSpecific(final ViewDefinitionState view) {
        FieldComponent occupationType = (FieldComponent) view.getComponentByReference(OCCUPATION_TYPE);

        if (occupationType.getFieldValue() != null) {
            Entity dictionaryItem = findDictionaryItemByName(occupationType.getFieldValue().toString());
            if (dictionaryItem == null) {
                setFieldsEnabled(view, false, false);
            } else {
                String occupationTypeTechnicalCode = dictionaryItem.getStringField(TECHNICAL_CODE);

                if (WORK_ON_LINE.getStringValue().equals(occupationTypeTechnicalCode)) {
                    setFieldsEnabled(view, true, false);
                } else if (OTHER_CASE.getStringValue().equals(occupationTypeTechnicalCode)) {
                    setFieldsEnabled(view, false, true);
                } else {
                    setFieldsEnabled(view, false, false);
                }
            }
        }
    }

    private void setFieldsEnabled(final ViewDefinitionState view, final boolean visibleOrRequiredProductionLine,
            final boolean visibleOrRequiredOccupationTypeName) {
        FieldComponent productionLine = (FieldComponent) view.getComponentByReference(PRODUCTION_LINE);
        FieldComponent occupationTypeName = (FieldComponent) view.getComponentByReference(OCCUPATION_TYPE_NAME);

        productionLine.setVisible(visibleOrRequiredProductionLine);
        productionLine.setRequired(visibleOrRequiredProductionLine);
        productionLine.requestComponentUpdateState();
        occupationTypeName.setVisible(visibleOrRequiredOccupationTypeName);
        occupationTypeName.setRequired(visibleOrRequiredOccupationTypeName);
        occupationTypeName.requestComponentUpdateState();
    }

    public void setOccupationTypeToDefault(final ViewDefinitionState view) {
        FormComponent staffAssignmentToShiftForm = (FormComponent) view.getComponentByReference("form");
        FieldComponent occupationType = (FieldComponent) view.getComponentByReference(OCCUPATION_TYPE);

        if ((staffAssignmentToShiftForm.getEntityId() == null) && (occupationType.getFieldValue() == null)) {
            Entity dictionaryItem = findDictionaryItemByTechnicalCode(WORK_ON_LINE.getStringValue());

            if (dictionaryItem != null) {
                String occupationTypeName = dictionaryItem.getStringField(NAME);

                occupationType.setFieldValue(occupationTypeName);
                occupationType.requestComponentUpdateState();
            }
        }
    }

    protected Entity findDictionaryItemByName(final String name) {
        return dataDefinitionService.get("qcadooModel", "dictionaryItem").find().add(SearchRestrictions.eq(NAME, name))
                .uniqueResult();
    }

    public Entity findDictionaryItemByTechnicalCode(final String technicalCode) {
        return dataDefinitionService.get("qcadooModel", "dictionaryItem").find()
                .add(SearchRestrictions.eq(TECHNICAL_CODE, technicalCode)).uniqueResult();
    }

}
