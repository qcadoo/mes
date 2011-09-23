package com.qcadoo.mes.productionCounting.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductionRecordComparator;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductionCountingViewService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    public void fillProductWhenOrderChanged(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference("order");
        if (orderLookup.getFieldValue() == null) {
            viewDefinitionState.getComponentByReference("product").setFieldValue(null);
            viewDefinitionState.getComponentByReference("productionRecords").setVisible(false);
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());
        if (order == null) {
            viewDefinitionState.getComponentByReference("product").setFieldValue(null);
            viewDefinitionState.getComponentByReference("productionRecords").setVisible(false);
            return;
        }
        if (order.getStringField("typeOfProductionRecording") == null
                || order.getStringField("typeOfProductionRecording").equals("01none")) {
            viewDefinitionState.getComponentByReference("product").setFieldValue(null);
            viewDefinitionState.getComponentByReference("productionRecords").setVisible(false);
            ((FieldComponent) viewDefinitionState.getComponentByReference("order")).addMessage(translationService.translate(
                    "productionCounting.productionBalance.report.error.orderWithoutRecordingType",
                    viewDefinitionState.getLocale()), ComponentState.MessageType.FAILURE);
            return;
        }

        setProductFieldValue(viewDefinitionState, order);
        setProductionRecordsGridContent(viewDefinitionState, order);
    }

    public void fillProductionRecordsGrid(final ViewDefinitionState viewDefinitionState) {
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference("order");
        if (orderLookup.getFieldValue() == null) {
            viewDefinitionState.getComponentByReference("productionRecords").setVisible(false);
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());
        if (order == null) {
            viewDefinitionState.getComponentByReference("productionRecords").setVisible(false);
            return;
        }
        if (order.getStringField("typeOfProductionRecording") == null
                || order.getStringField("typeOfProductionRecording").equals("01none")) {
            viewDefinitionState.getComponentByReference("productionRecords").setVisible(false);
            return;
        }

        setProductionRecordsGridContent(viewDefinitionState, order);
    }

    private void setProductFieldValue(final ViewDefinitionState viewDefinitionState, final Entity order) {
        FieldComponent productField = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        productField.setFieldValue(order.getBelongsToField("product").getId());
    }

    private void setProductionRecordsGridContent(final ViewDefinitionState viewDefinitionState, final Entity order) {
        GridComponent productionRecords = (GridComponent) viewDefinitionState.getComponentByReference("productionRecords");
        List<Entity> productionRecordsList = new ArrayList<Entity>(order.getHasManyField("productionRecords"));
        Collections.sort(productionRecordsList, new EntityProductionRecordComparator());
        productionRecords.setEntities(productionRecordsList);
        productionRecords.setVisible(true);
    }

}
