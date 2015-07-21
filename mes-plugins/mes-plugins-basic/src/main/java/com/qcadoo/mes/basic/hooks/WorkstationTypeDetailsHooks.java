package com.qcadoo.mes.basic.hooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class WorkstationTypeDetailsHooks {

    @Autowired
    DataDefinitionService dataDefinitionService;

    public void disableSubassemblyCheckbox(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity workstationType = form.getEntity();
        FieldComponent subassemblyCheckbox = (FieldComponent) view.getComponentByReference(WorkstationTypeFields.SUBASSEMBLY);
        if (workstationType.getBooleanField(WorkstationTypeFields.SUBASSEMBLY)) {
            boolean hasSubassemblies = hasSubassemblies(workstationType);
            subassemblyCheckbox.setEnabled(!hasSubassemblies);
        } else {
            boolean hasWorkstations = hasWorkstations(workstationType);
            subassemblyCheckbox.setEnabled(!hasWorkstations);
        }
    }

    private boolean hasSubassemblies(final Entity workstationType) {
        List<Entity> subassemblies = dataDefinitionService
                .get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBASSEMBLY).find()
                .add(SearchRestrictions.belongsTo(SubassemblyFields.WORKSTATION_TYPE, workstationType)).list().getEntities();
        return !subassemblies.isEmpty();
    }

    private boolean hasWorkstations(final Entity workstationType) {
        List<Entity> workstations = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION)
                .find().add(SearchRestrictions.belongsTo(WorkstationFields.WORKSTATION_TYPE, workstationType)).list()
                .getEntities();
        return !workstations.isEmpty();
    }
}
