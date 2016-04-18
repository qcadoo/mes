package com.qcadoo.mes.productionCounting.listeners;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.SetTechnologyInComponentsService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductionCountingQuantityDetailsListenersPC {

    private static final String L_GRID = "productionCountingQuantitySetComponents";

    @Autowired
    SetTechnologyInComponentsService setTechnologyInComponentsService;

    public void onRemoveSelectedProductionCountingQuantitySetComponents(final ViewDefinitionState view,
            final ComponentState state, final String[] args) {
        GridComponent grid = ((GridComponent) view.getComponentByReference(L_GRID));
        List<Entity> selectedEntities = grid.getSelectedEntities();
        List<Entity> entities = grid.getEntities();
        List<Long> ids = new ArrayList<>();

        if (selectedEntities.size() == entities.size()) {
            state.addMessage("productionCounting.productionCountingQuantitySetComponent.error.cantDelete", MessageType.INFO);
        } else {
            selectedEntities
                    .forEach(productionCountingQuantitySetComponent -> {
                        productionCountingQuantitySetComponent.getDataDefinition().delete(
                                productionCountingQuantitySetComponent.getId());
                    });
            grid.reloadEntities();
            if (selectedEntities.size() == 1) {
                state.addMessage("qcadooView.message.deleteMessage", MessageType.SUCCESS);

            } else if (selectedEntities.size() > 1) {
                state.addMessage("qcadooView.message.deleteMessages", MessageType.SUCCESS, String.valueOf(ids.size()));
            }
        }

    }
}
