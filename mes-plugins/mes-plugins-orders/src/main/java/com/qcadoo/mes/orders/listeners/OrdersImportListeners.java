package com.qcadoo.mes.orders.listeners;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.imports.order.OrderCellBinderRegistry;
import com.qcadoo.mes.orders.imports.order.OrderXlsxImportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OrdersImportListeners {

    @Autowired
    private OrderXlsxImportService orderXlsxImportService;

    @Autowired
    private OrderCellBinderRegistry orderCellBinderRegistry;

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        orderXlsxImportService.downloadImportSchema(view, OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER,
                XlsxImportService.L_XLSX);
    }

    public void processImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws IOException {
        orderXlsxImportService.processImportFile(view, orderCellBinderRegistry.getCellBinderRegistry(), true,
                OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER, OrdersImportListeners::createRestrictionForOrder);
    }

    private static SearchCriterion createRestrictionForOrder(final Entity order) {
        return SearchRestrictions.eq(OrderFields.NUMBER, order.getStringField(OrderFields.NUMBER));
    }

    public void redirectToLogs(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        orderXlsxImportService.redirectToLogs(view, OrdersConstants.MODEL_ORDER);
    }

    public void onInputChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        orderXlsxImportService.changeButtonsState(view, false);
    }

}
