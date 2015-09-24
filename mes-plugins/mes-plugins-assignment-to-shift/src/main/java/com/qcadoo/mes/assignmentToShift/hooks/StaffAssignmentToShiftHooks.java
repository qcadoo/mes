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

import static com.qcadoo.mes.assignmentToShift.constants.OccupationType.OTHER_CASE;
import static com.qcadoo.mes.assignmentToShift.constants.OccupationType.WORK_ON_LINE;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.NUMBER;
import static com.qcadoo.model.constants.DictionaryItemFields.TECHNICAL_CODE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.OccupationType;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class StaffAssignmentToShiftHooks {

    @Autowired
    private StaffAssignmentToShiftDetailsHooks assignmentToShiftDetailsHooks;

    public void onSave(final DataDefinition staffAssignmentToShiftDD, final Entity staffAssignmentToShift) {
        setOccupationTypeForGridValue(staffAssignmentToShiftDD, staffAssignmentToShift);
        setOccupationTypeEnum(staffAssignmentToShiftDD, staffAssignmentToShift);
        clearProductionLineForOtherTypes(staffAssignmentToShiftDD, staffAssignmentToShift);
    }

    public void setOccupationTypeForGridValue(final DataDefinition staffAssignmentToShiftDD, final Entity staffAssignmentToShift) {
        String occupationType = staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.OCCUPATION_TYPE);

        Entity dictionaryItem = assignmentToShiftDetailsHooks.findDictionaryItemByName(occupationType);

        String technicalCode = dictionaryItem.getStringField(TECHNICAL_CODE);

        if (technicalCode != null && technicalCode.equals(WORK_ON_LINE.getStringValue())) {
            if (staffAssignmentToShift.getBelongsToField(StaffAssignmentToShiftFields.PRODUCTION_LINE) == null) {
                staffAssignmentToShift.addError(staffAssignmentToShiftDD.getField(StaffAssignmentToShiftFields.PRODUCTION_LINE),
                        "assignmentToShift.staffAssignmentToShift.productionLine.isEmpty");

                return;
            }

            staffAssignmentToShift.setField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_VALUE_FOR_GRID,
                    occupationType
                            + ": "
                            + staffAssignmentToShift.getBelongsToField(StaffAssignmentToShiftFields.PRODUCTION_LINE)
                                    .getStringField(NUMBER));
        } else if (technicalCode != null && technicalCode.equals(OTHER_CASE.getStringValue())) {
            String occupationTypeName = staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME);

            if (StringUtils.isEmpty(occupationTypeName)) {
                staffAssignmentToShift.setField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_VALUE_FOR_GRID, occupationType);
            } else {
                staffAssignmentToShift.setField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_VALUE_FOR_GRID, occupationType
                        + ": " + occupationTypeName);
            }
        } else {
            staffAssignmentToShift.setField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_VALUE_FOR_GRID, occupationType);
        }
    }

    public void setOccupationTypeEnum(final DataDefinition staffAssignmentToShiftDD, final Entity staffAssignmentToShift) {
        String occupationType = staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.OCCUPATION_TYPE);

        Entity dictionaryItem = assignmentToShiftDetailsHooks.findDictionaryItemByName(occupationType);

        String technicalCode = dictionaryItem.getStringField(TECHNICAL_CODE);

        staffAssignmentToShift.setField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_ENUM, technicalCode);
    }

    private void clearProductionLineForOtherTypes(final DataDefinition staffAssignmentToShiftDD,
            final Entity staffAssignmentToShift) {
        String occupationTypeEnum = staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_ENUM);
        if (occupationTypeEnum == null || !occupationTypeEnum.equals(OccupationType.WORK_ON_LINE.getStringValue())) {
            staffAssignmentToShift.setField(StaffAssignmentToShiftFields.PRODUCTION_LINE, null);
        }
    }

}
