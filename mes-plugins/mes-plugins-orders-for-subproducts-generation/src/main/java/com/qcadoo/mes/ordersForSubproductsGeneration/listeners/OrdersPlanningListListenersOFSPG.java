package com.qcadoo.mes.ordersForSubproductsGeneration.listeners;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrdersPlanningListListenersOFSPG {

    public void generateListOfProductionOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> ids = grid.getSelectedEntitiesIds();

        if (ids.isEmpty()) {
            view.addMessage("ordersForSubproductsGeneration.ordersPlanningList.notSelected", ComponentState.MessageType.INFO);
        } else {
            if (!state.isHasError()) {
                view.redirectTo("/ordersForSubproductsGeneration/listOfProductionOrdersReport." + args[0] + "?ids="
                        + ids.stream().map(String::valueOf).collect(Collectors.joining(",")), true, false);
            }
        }
    }
}
