package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.FaultTypeAppliesTo;
import com.qcadoo.mes.cmmsMachineParts.constants.FaultTypeFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class FaultTypeDetailsHooks {

    private static final String L_FORM = "form";

    public void onBeforeRender(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity faultType = form.getPersistedEntityWithIncludedFormValues();
        FaultTypeAppliesTo appliesTo = FaultTypeAppliesTo.from(faultType);
        toggleGridsEnable(view, appliesTo, false);
    }

    public void toggleGridsEnable(final ViewDefinitionState view, final FaultTypeAppliesTo appliesTo, final boolean shouldClear) {
        GridComponent workstationsGrid = (GridComponent) view.getComponentByReference(FaultTypeFields.WORKSTATIONS);
        GridComponent subassembliesGrid = (GridComponent) view.getComponentByReference(FaultTypeFields.SUBASSEMBLIES);
        GridComponent workstationTypesGrid = (GridComponent) view.getComponentByReference(FaultTypeFields.WORKSTATION_TYPES);

        if (appliesTo.compareTo(FaultTypeAppliesTo.WORKSTATION_OR_SUBASSEMBLY) == 0) {
            workstationsGrid.setEnabled(true);
            subassembliesGrid.setEnabled(true);
            workstationTypesGrid.setEnabled(false);
            if (shouldClear) {
                workstationTypesGrid.setEntities(Lists.newArrayList());
            }
        } else if (appliesTo.compareTo(FaultTypeAppliesTo.WORKSTATION_TYPE) == 0) {
            workstationsGrid.setEnabled(false);
            subassembliesGrid.setEnabled(false);
            workstationTypesGrid.setEnabled(true);
            if (shouldClear) {
                workstationsGrid.setEntities(Lists.newArrayList());
                subassembliesGrid.setEntities(Lists.newArrayList());
            }
        } else {
            workstationsGrid.setEnabled(false);
            subassembliesGrid.setEnabled(false);
            workstationTypesGrid.setEnabled(false);

            if (shouldClear) {
                workstationsGrid.setEntities(Lists.newArrayList());
                subassembliesGrid.setEntities(Lists.newArrayList());
                workstationTypesGrid.setEntities(Lists.newArrayList());
            }
        }
    }
}
