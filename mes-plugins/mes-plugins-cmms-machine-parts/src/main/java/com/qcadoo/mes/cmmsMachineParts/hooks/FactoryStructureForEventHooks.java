package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.factoryStructure.FactoryStructureGenerationService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class FactoryStructureForEventHooks {

    @Autowired
    private FactoryStructureGenerationService factoryStructureGenerationService;

    // TODO move fields' names to constants
    public void generateFactoryStructure(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity maintenanceEvent = form.getEntity();
        EntityTree factoryStructure = factoryStructureGenerationService.generateFactoryStructureForEntity(maintenanceEvent,
                "maintenanceEvent");
        maintenanceEvent.setField("factoryStructure", factoryStructure);

        form.setEntity(maintenanceEvent);
    }
}
