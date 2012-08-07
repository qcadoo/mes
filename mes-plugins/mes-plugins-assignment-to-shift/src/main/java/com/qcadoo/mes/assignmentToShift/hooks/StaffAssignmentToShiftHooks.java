package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.OccupationType.OTHER_CASE;
import static com.qcadoo.mes.assignmentToShift.constants.OccupationType.WORK_ON_LINE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE_ENUM;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE_VALUE_FOR_GRID;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.PRODUCTION_LINE;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.NUMBER;
import static com.qcadoo.model.constants.DictionaryItemFields.TECHNICAL_CODE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class StaffAssignmentToShiftHooks {

    @Autowired
    private StaffAssignmentToShiftDetailsHooks assignmentToShiftDetailsHooks;

    public void setOccupationTypeForGridValue(final DataDefinition staffAssignmentToShiftDD, final Entity staffAssignmentToShift) {
        String occupationType = staffAssignmentToShift.getStringField(OCCUPATION_TYPE);

        Entity dictionaryItem = assignmentToShiftDetailsHooks.findDictionaryItemByName(occupationType);

        String technicalCode = dictionaryItem.getStringField(TECHNICAL_CODE);
        if (technicalCode != null && technicalCode.equals(WORK_ON_LINE.getStringValue())) {
            if (staffAssignmentToShift.getBelongsToField(PRODUCTION_LINE) == null) {
                staffAssignmentToShift.addError(staffAssignmentToShiftDD.getField(PRODUCTION_LINE),
                        "assignmentToShift.staffAssignmentToShift.productionLine.isEmpty");

                return;
            }
            staffAssignmentToShift.setField(OCCUPATION_TYPE_VALUE_FOR_GRID, occupationType + ": "
                    + staffAssignmentToShift.getBelongsToField(PRODUCTION_LINE).getStringField(NUMBER));
        } else if (technicalCode != null && technicalCode.equals(OTHER_CASE.getStringValue())) {
            staffAssignmentToShift.setField(OCCUPATION_TYPE_VALUE_FOR_GRID,
                    occupationType + ": " + staffAssignmentToShift.getStringField(OCCUPATION_TYPE_NAME));
        } else {
            staffAssignmentToShift.setField(OCCUPATION_TYPE_VALUE_FOR_GRID, occupationType);
        }
    }

    public void setOccupationTypeEnum(final DataDefinition staffAssignmentToShiftDD, final Entity staffAssignmentToShift) {
        String occupationType = staffAssignmentToShift.getStringField(OCCUPATION_TYPE);

        Entity dictionaryItem = assignmentToShiftDetailsHooks.findDictionaryItemByName(occupationType);

        String technicalCode = dictionaryItem.getStringField(TECHNICAL_CODE);

        staffAssignmentToShift.setField(OCCUPATION_TYPE_ENUM, technicalCode);
    }

}
