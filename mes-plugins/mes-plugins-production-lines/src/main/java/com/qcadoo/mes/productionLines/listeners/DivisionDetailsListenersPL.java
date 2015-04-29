package com.qcadoo.mes.productionLines.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class DivisionDetailsListenersPL {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onAddExistingEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (args.length < 1) {
            return;
        }
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity division = form.getPersistedEntityWithIncludedFormValues();
        List<Long> addedWorkstationIds = parseIds(args[0]);
        for (Long addedWorkstationId : addedWorkstationIds) {
            Entity workstation = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION)
                    .get(addedWorkstationId);
            workstation.setField(WorkstationFields.DIVISION, division);
            workstation.getDataDefinition().save(workstation);
        }

    }

    private List<Long> parseIds(final String ids) {
        List<Long> result = Lists.newArrayList();
        String[] splittedIds = ids.replace("[", "").replace("]", "").replace("\"", "").split(",");
        for (int i = 0; i < splittedIds.length; i++) {
            result.add(Long.parseLong(splittedIds[i]));
        }
        return result;
    }

    public void onRemoveSelectedEntity(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent workstationsGrid = (GridComponent) view.getComponentByReference("workstations");
        List<Entity> workstationsToDelete = workstationsGrid.getSelectedEntities();
        for (Entity workstation : workstationsToDelete) {
            workstation.setField(WorkstationFieldsPL.PRODUCTION_LINE, null);
            workstation.setField(WorkstationFields.DIVISION, null);
            workstation.getDataDefinition().save(workstation);

        }
    }

    public void onRemoveSelectedProductionLines(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent productionLinesGrid = (GridComponent) view.getComponentByReference("productionLines");
        List<Entity> productionLinesToDelete = productionLinesGrid.getSelectedEntities();
        for (Entity productionLine : productionLinesToDelete) {
            List<Entity> workstations = productionLine.getHasManyField(ProductionLineFields.WORKSTATIONS);
            for (Entity workstation : workstations) {
                workstation.setField(WorkstationFieldsPL.PRODUCTION_LINE, null);
                workstation.setField(WorkstationFields.DIVISION, null);
                workstation.getDataDefinition().save(workstation);
            }
        }
    }
}
