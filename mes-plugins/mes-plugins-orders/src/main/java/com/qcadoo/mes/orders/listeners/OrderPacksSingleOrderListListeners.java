package com.qcadoo.mes.orders.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.OrderPackService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.OrderPackServiceMarker;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderPacksSingleOrderListListeners {

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrderPackService orderPackService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateExecutorService stateExecutorService;

    public void showPackageProcesses(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long packId = form.getEntityId();

        if (Objects.isNull(packId)) {
            return;
        }

        Entity pack = form.getEntity().getDataDefinition().get(packId);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("orderPackNumber", applyInOperator(pack.getStringField(OrderPackFields.NUMBER)));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        String url = "/page/orders/orderTechnologicalProcessesList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void showPackageProcessesFromList(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> packsIds = grid.getSelectedEntitiesIds();

        Entity pack = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_PACK).get(
                packsIds.stream().findFirst().get());

        Map<String, String> filters = Maps.newHashMap();
        filters.put("orderPackNumber", applyInOperator(pack.getStringField(OrderPackFields.NUMBER)));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        String url = "/page/orders/orderTechnologicalProcessesList.html";
        view.redirectTo(url, false, true, parameters);
    }

    private String applyInOperator(final String value) {
        return "[" + value + "]";
    }

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(OrderPackServiceMarker.class, view, args);
    }

    public void printLabels(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> packsIds = grid.getSelectedEntitiesIds();

        if (packsIds.isEmpty()) {
            view.addMessage("orders.packs.notSelected", ComponentState.MessageType.INFO);
        } else {
            view.redirectTo(
                    "/orders/packsLabels.pdf?"
                            + packsIds.stream().map(id -> "ids=" + id.toString()).collect(Collectors.joining("&")), true, false);
        }

    }

    public void generateOrderPacks(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(OrdersConstants.MODEL_ORDER);
        Entity order = form.getPersistedEntityWithIncludedFormValues();
        Entity parameter = parameterService.getParameter();
        if (!order.getHasManyField(OrderFields.ORDER_PACKS).isEmpty()) {
            view.addMessage("orders.orderPacksGeneration.error.orderHasPacks", ComponentState.MessageType.INFO);
            return;
        } else if (!parameter.getBooleanField(ParameterFieldsO.GENERATE_PACKS_FOR_ORDERS)) {
            view.addMessage("orders.orderPacksGeneration.error.parameterDisabled", ComponentState.MessageType.INFO);
            return;
        }
        orderPackService.generateOrderPacks(order);
        view.addMessage("orders.orderPacksGeneration.success", ComponentState.MessageType.SUCCESS);
    }
}
