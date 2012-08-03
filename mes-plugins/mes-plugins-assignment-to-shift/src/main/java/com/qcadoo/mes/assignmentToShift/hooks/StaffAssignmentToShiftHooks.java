package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.model.constants.DictionaryItemFields.TECHNICAL_CODE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.assignmentToShift.constants.OccupationTypeEnumStringValue;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class StaffAssignmentToShiftHooks {

    @Autowired
    private StaffAssignmentToShiftDetailsHooks assignmentToShiftDetailsHooks;

    @Autowired
    private TranslationService translationService;

    public void setOccupationTypeForGridValue(final DataDefinition dataDefinition, final Entity entity) {
        String occupationType = entity.getStringField("occupationType");
        Entity dictionaryItem = assignmentToShiftDetailsHooks.findDictionaryItemByName(occupationType);
        String technicalCode = dictionaryItem.getStringField(TECHNICAL_CODE);
        if (technicalCode != null && technicalCode.equals(OccupationTypeEnumStringValue.WORK_ON_LINE.getStringValue())) {
            if (entity.getBelongsToField(StaffAssignmentToShiftFields.PRODUCTION_LINE) == null) {
                entity.addError(dataDefinition.getField(StaffAssignmentToShiftFields.PRODUCTION_LINE),
                        "assignmentToShift.staffAssignmentToShift.productionLine.isEmpty");
                return;
            }
            entity.setField("occupationTypeValueForGrid",
                    occupationType + ": "
                            + entity.getBelongsToField(StaffAssignmentToShiftFields.PRODUCTION_LINE).getStringField("number"));
        } else if (technicalCode != null && technicalCode.equals(OccupationTypeEnumStringValue.OTHER_CASE.getStringValue())) {
            entity.setField("occupationTypeValueForGrid",
                    occupationType + ": " + entity.getStringField(StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME));
        } else {
            entity.setField("occupationTypeValueForGrid", occupationType);
        }
    }

    public void setOccupationTypeEnum(final DataDefinition dataDefinition, final Entity entity) {
        String occupationType = entity.getStringField("occupationType");
        Entity dictionaryItem = assignmentToShiftDetailsHooks.findDictionaryItemByName(occupationType);
        String technicalCode = dictionaryItem.getStringField(TECHNICAL_CODE);
        entity.setField("occupationTypeEnum", technicalCode);
    }
}
