package com.qcadoo.mes.productionLines.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.mes.productionLines.factoryStructure.FactoryStructureGenerationService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class SubassemblyDetailsListenersPL {

    @Autowired
    private FactoryStructureGenerationService factoryStructureGenerationService;

    public void generateFactoryStructure(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity subassembly = form.getEntity();
        EntityTree structure = factoryStructureGenerationService.generateFactoryStructureForSubassembly(subassembly);
        subassembly.setField(WorkstationFieldsPL.FACTORY_STRUCTURE, structure);
        form.setEntity(subassembly);

    }
}
