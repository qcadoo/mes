package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.OCCUPATION_TYPE_NAME;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.PRODUCTION_LINE;
import static com.qcadoo.model.constants.DictionaryItemFields.NAME;
import static com.qcadoo.model.constants.DictionaryItemFields.TECHNICAL_CODE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.OccupationTypeEnumStringValue;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class StaffAssignmentToShiftDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void enabledFieldWhenTypeIsSpecific(final ViewDefinitionState view) {
        FieldComponent occupationType = (FieldComponent) view.getComponentByReference("occupationType");
        Entity dictionaryItem = findDictionaryItemByName(occupationType.getFieldValue().toString());
        String technicalCode = dictionaryItem.getStringField(TECHNICAL_CODE);
        if (technicalCode != null && technicalCode.equals(OccupationTypeEnumStringValue.WORK_ON_LINE)) {
            setFieldsEnabled(view, true, false);
        } else if (technicalCode != null && technicalCode.equals(OccupationTypeEnumStringValue.OTHER_CASE)) {
            setFieldsEnabled(view, false, true);
        } else {
            setFieldsEnabled(view, false, false);
        }
    }

    private void setFieldsEnabled(final ViewDefinitionState view, final boolean enabledOrRequiredProductionLine,
            final boolean enabledOrRequiredOccupationTypeName) {
        FieldComponent productionLine = (FieldComponent) view.getComponentByReference(PRODUCTION_LINE);
        FieldComponent occupationTypeName = (FieldComponent) view.getComponentByReference(OCCUPATION_TYPE_NAME);
        productionLine.setEnabled(enabledOrRequiredProductionLine);
        occupationTypeName.setEnabled(enabledOrRequiredOccupationTypeName);
        productionLine.setRequired(enabledOrRequiredProductionLine);
        occupationTypeName.setRequired(enabledOrRequiredOccupationTypeName);
        productionLine.requestComponentUpdateState();
        occupationTypeName.requestComponentUpdateState();
    }

    protected Entity findDictionaryItemByName(final String name) {
        return dataDefinitionService.get("qcadooModel", "dictionaryItem").find().add(SearchRestrictions.eq(NAME, name))
                .uniqueResult();
    }

}
