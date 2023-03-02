package com.qcadoo.mes.orders.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.SplitOrderParentConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrdersPlanningListListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void splitOrdersParts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> selectedEntitiesIds = ordersGrid.getSelectedEntitiesIds();

        Entity helper = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SPLIT_ORDER_HELPER).create();
        helper = helper.getDataDefinition().save(helper);
        for (Long id : selectedEntitiesIds) {
            Entity parent = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SPLIT_ORDER_PARENT).create();
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            parent.setField(SplitOrderParentConstants.SPLIT_ORDER_HELPER, helper);
            parent.setField(SplitOrderParentConstants.ORDER, order);
            parent.setField(SplitOrderParentConstants.NUMBER, order.getStringField(OrderFields.NUMBER));
            parent.setField(SplitOrderParentConstants.NAME, order.getStringField(OrderFields.NAME));
            parent.setField(SplitOrderParentConstants.DATE_FROM, order.getDateField(OrderFields.DATE_FROM));
            parent.setField(SplitOrderParentConstants.DATE_TO, order.getDateField(OrderFields.DATE_TO));
            parent.setField(SplitOrderParentConstants.PLANNED_QUANTITY, order.getDecimalField(OrderFields.PLANNED_QUANTITY));
            parent.setField(SplitOrderParentConstants.UNIT, order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.UNIT));
            parent = parent.getDataDefinition().save(parent);
            parent.getId();
        }


        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", helper.getId());

        String url = "../page/orders/divideOrdersDetails.html";
        view.openModal(url, parameters);
    }

    public void splitOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> selectedEntitiesIds = ordersGrid.getSelectedEntitiesIds();

        Entity helper = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SPLIT_ORDER_HELPER).create();
        helper = helper.getDataDefinition().save(helper);
        for (Long id : selectedEntitiesIds) {
            Entity parent = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SPLIT_ORDER_PARENT).create();
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
            parent.setField(SplitOrderParentConstants.SPLIT_ORDER_HELPER, helper);
            parent.setField(SplitOrderParentConstants.ORDER, order);
            parent.setField(SplitOrderParentConstants.NUMBER, order.getStringField(OrderFields.NUMBER));
            parent.setField(SplitOrderParentConstants.NAME, order.getStringField(OrderFields.NAME));
            parent.setField(SplitOrderParentConstants.DATE_FROM, order.getDateField(OrderFields.DATE_FROM));
            parent.setField(SplitOrderParentConstants.DATE_TO, order.getDateField(OrderFields.DATE_TO));
            parent.setField(SplitOrderParentConstants.PLANNED_QUANTITY, order.getDecimalField(OrderFields.PLANNED_QUANTITY));
            parent.setField(SplitOrderParentConstants.UNIT, order.getBelongsToField(OrderFields.PRODUCT).getStringField(ProductFields.UNIT));
            parent = parent.getDataDefinition().save(parent);
            parent.getId();
        }


        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", helper.getId());

        String url = "../page/orders/splitOrdersDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void changeDates(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> selectedEntitiesIds = ordersGrid.getSelectedEntitiesIds();

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.orderIds", selectedEntitiesIds.stream().map(Object::toString).collect(Collectors.joining(",")));

        String url = "../page/orders/changeDatesDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void setCategory(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> selectedEntitiesIds = ordersGrid.getSelectedEntitiesIds();

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.selectedEntities",
                selectedEntitiesIds.stream().map(Object::toString).collect(Collectors.joining(",")));

        String url = "../page/orders/setCategory.html";
        view.openModal(url, parameters);
    }

    public void openOrdersImportPage(final ViewDefinitionState view, final ComponentState state,
                                     final String[] args) {
        StringBuilder url = new StringBuilder("../page/orders/ordersImport.html");

        view.openModal(url.toString());
    }

}
