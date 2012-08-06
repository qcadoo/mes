package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.model.constants.DictionaryItemFields.TECHNICAL_CODE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.OccupationTypeEnumStringValue;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class StaffAssignmentToShiftHooks {

    @Autowired
    private StaffAssignmentToShiftDetailsHooks assignmentToShiftDetailsHooks;

    public void setOccupationTypeForGridValue(final DataDefinition staffAssignmentToShiftDD, final Entity staffAssignmentToShift) {
        String occupationType = staffAssignmentToShift.getStringField("occupationType");
        Entity dictionaryItem = assignmentToShiftDetailsHooks.findDictionaryItemByName(occupationType);
        String technicalCode = dictionaryItem.getStringField(TECHNICAL_CODE);
        if (technicalCode != null && technicalCode.equals(OccupationTypeEnumStringValue.WORK_ON_LINE.getStringValue())) {
            if (staffAssignmentToShift.getBelongsToField(StaffAssignmentToShiftFields.PRODUCTION_LINE) == null) {
                staffAssignmentToShift.addError(staffAssignmentToShiftDD.getField(StaffAssignmentToShiftFields.PRODUCTION_LINE),
                        "assignmentToShift.staffAssignmentToShift.productionLine.isEmpty");
                return;
            }
            staffAssignmentToShift.setField("occupationTypeValueForGrid",
                    occupationType + ": "
                            + staffAssignmentToShift.getBelongsToField(StaffAssignmentToShiftFields.PRODUCTION_LINE).getStringField("number"));
        } else if (technicalCode != null && technicalCode.equals(OccupationTypeEnumStringValue.OTHER_CASE.getStringValue())) {
            staffAssignmentToShift.setField("occupationTypeValueForGrid",
                    occupationType + ": " + staffAssignmentToShift.getStringField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME));
        } else {
            staffAssignmentToShift.setField("occupationTypeValueForGrid", occupationType);
        }
    }

    public void setOccupationTypeEnum(final DataDefinition staffAssignmentToShiftDD, final Entity staffAssignmentToShift) {
        String occupationType = staffAssignmentToShift.getStringField("occupationType");
        Entity dictionaryItem = assignmentToShiftDetailsHooks.findDictionaryItemByName(occupationType);
        String technicalCode = dictionaryItem.getStringField(TECHNICAL_CODE);
        staffAssignmentToShift.setField("occupationTypeEnum", technicalCode);
    }
}
