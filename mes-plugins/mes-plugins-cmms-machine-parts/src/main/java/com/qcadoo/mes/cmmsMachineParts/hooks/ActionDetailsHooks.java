package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionAppliesTo;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ActionDetailsHooks {

    private static final String L_FORM = "form";

    public void onBeforeRender(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity action = form.getPersistedEntityWithIncludedFormValues();
        ActionAppliesTo appliesTo = ActionAppliesTo.from(action);
        toggleGridsEnable(view, appliesTo, false);
    }

    public void toggleGridsEnable(final ViewDefinitionState view, final ActionAppliesTo appliesTo, final boolean shouldClear) {
        GridComponent workstationsGrid = (GridComponent) view.getComponentByReference(ActionFields.WORKSTATIONS);
        GridComponent subassembliesGrid = (GridComponent) view.getComponentByReference(ActionFields.SUBASSEMBLIES);
        GridComponent workstationTypesGrid = (GridComponent) view.getComponentByReference(ActionFields.WORKSTATION_TYPES);

        if (appliesTo.compareTo(ActionAppliesTo.WORKSTATION_OR_SUBASSEMBLY) == 0) {
            workstationsGrid.setEnabled(true);
            subassembliesGrid.setEnabled(true);
            workstationTypesGrid.setEnabled(false);
            if (shouldClear) {
                workstationTypesGrid.setEntities(Lists.newArrayList());
            }
        } else if (appliesTo.compareTo(ActionAppliesTo.WORKSTATION_TYPE) == 0) {
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
