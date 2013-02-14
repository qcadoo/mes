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
package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.OccupationType.OTHER_CASE;
import static com.qcadoo.mes.assignmentToShift.constants.OccupationType.WORK_ON_LINE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.PRODUCTION_LINE;
import static com.qcadoo.model.constants.DictionaryItemFields.NAME;
import static com.qcadoo.model.constants.DictionaryItemFields.TECHNICAL_CODE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class StaffAssignmentToShiftDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setFieldsEnabledWhenTypeIsSpecific(final ViewDefinitionState view) {
        FieldComponent occupationType = (FieldComponent) view.getComponentByReference(OCCUPATION_TYPE);

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

    protected Entity findDictionaryItemByTechnicalCode(final String technicalCode) {
        return dataDefinitionService.get("qcadooModel", "dictionaryItem").find()
                .add(SearchRestrictions.eq(TECHNICAL_CODE, technicalCode)).uniqueResult();
    }

}
