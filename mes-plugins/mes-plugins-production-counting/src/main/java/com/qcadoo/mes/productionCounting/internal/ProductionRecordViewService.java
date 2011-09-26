package com.qcadoo.mes.productionCounting.internal;

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PARAMETER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.productionCounting.internal.ProductionRecordService.getBooleanValue;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_PRODUCTION_RECORD;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_FOREACH;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_NONE;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class ProductionRecordViewService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private TranslationService translationService;

    private final static String CLOSED_ORDER = "04done";

    private final static Logger LOG = LoggerFactory.getLogger(ProductionRecordViewService.class);

    public void initializeRecordDetailsView(final ViewDefinitionState view) {
        FormComponent recordForm = (FormComponent) view.getComponentByReference("form");
        if (recordForm.getEntityId() == null) {
            return;
        }

        Entity order = getOrderFromLookup(view);

        view.getComponentByReference("order").setEnabled(false);
        view.getComponentByReference("orderOperationComponent").setEnabled(false);
        view.getComponentByReference("shift").setEnabled(false);

        view.getComponentByReference("borderLayoutConsumed")
                .setVisible(getBooleanValue(order.getField("registerProductionTime")));
        view.getComponentByReference("recordOperationProductOutComponent").setVisible(
                getBooleanValue(order.getField("registerQuantityOutProduct")));
        view.getComponentByReference("recordOperationProductInComponent").setVisible(
                getBooleanValue(order.getField("registerQuantityInProduct")));
    }

    public void setParametersDefaultValue(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity parameter = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_PARAMETER).get(form.getEntityId());

        for (String componentReference : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                "registerProductionTime")) {
            FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            if (parameter == null || parameter.getField(componentReference) == null) {
                component.setFieldValue(true);
                component.requestComponentUpdateState();
            }
        }
    }

    public void enabledOrDisabledOperationField(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("order is null");
            }
            return;
        }
        setComponentVisible((String) order.getField("typeOfProductionRecording"), view);
    }

    private void setComponentVisible(final String recordingType, final ViewDefinitionState view) {
        view.getComponentByReference("orderOperationComponent").setVisible(PARAM_RECORDING_TYPE_FOREACH.equals(recordingType));
        ((FieldComponent) view.getComponentByReference("orderOperationComponent")).requestComponentUpdateState();
        view.getComponentByReference("machineTime").setVisible(true);
        view.getComponentByReference("laborTime").setVisible(true);

        if (PARAM_RECORDING_TYPE_FOREACH.equals(recordingType)) {
            view.getComponentByReference("borderLayoutConsumed").setEnabled(true);
            view.getComponentByReference("borderLayoutConsumed").setFieldValue(
                    translationService.translate("consumedTimeForEach", view.getLocale()));
        }
        if (PARAM_RECORDING_TYPE_FOREACH.equals(recordingType)) {
            view.getComponentByReference("borderLayoutConsumed").setFieldValue(
                    translationService.translate("consumedTimeCumulated", view.getLocale()));
        }
        if (!PARAM_RECORDING_TYPE_CUMULATED.equals(recordingType) && !PARAM_RECORDING_TYPE_FOREACH.equals(recordingType)) {
            view.getComponentByReference("borderLayoutConsumed").setFieldValue(
                    translationService.translate("operationNoneLabel", view.getLocale()));
        }

    }

    public void registeringProductionTime(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            return;
        }
        Boolean registerProductionTime = getBooleanValue(order.getField("registerProductionTime"));
        if (registerProductionTime) {
            view.getComponentByReference("machineTime").setVisible(true);
            view.getComponentByReference("laborTime").setVisible(true);
        }
    }

    public void closeOrder(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity order = getOrderFromLookup(view);
        Boolean autoCloseOrder = getBooleanValue(order.getField("autoCloseOrder"));

        if (autoCloseOrder && view.getComponentByReference("isFinal").getFieldValue() == "1") {
            order.setField("state", CLOSED_ORDER);
            dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).save(order);
            form.addMessage(translationService.translate("productionCounting.order.orderClosed", view.getLocale()),
                    MessageType.INFO, false);
        }
    }

    public void fillFieldFromProduct(final ViewDefinitionState view) {
        Entity recordProduct = ((FormComponent) view.getComponentByReference("form")).getEntity();
        recordProduct = recordProduct.getDataDefinition().get(recordProduct.getId());
        Entity product = recordProduct.getBelongsToField("product");

        view.getComponentByReference("number").setFieldValue(product.getField("number"));
        view.getComponentByReference("name").setFieldValue(product.getField("name"));

        String typeOfMaterial = "basic.product.typeOfMaterial.value." + product.getStringField("typeOfMaterial");
        view.getComponentByReference("type").setFieldValue(translationService.translate(typeOfMaterial, view.getLocale()));
        view.getComponentByReference("usedQuantityUNIT").setFieldValue(product.getStringField("unit"));
        view.getComponentByReference("plannedQuantityUNIT").setFieldValue(product.getStringField("unit"));
        for (String reference : Arrays.asList("number", "name", "type", "usedQuantityUNIT", "plannedQuantityUNIT")) {
            ((FieldComponent) view.getComponentByReference(reference)).requestComponentUpdateState();
        }

    }

    private Entity getOrderFromLookup(final ViewDefinitionState view) {
        ComponentState lookup = view.getComponentByReference("order");
        if (!(lookup.getFieldValue() instanceof Long)) {
            return null;
        }
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER).get((Long) lookup.getFieldValue());
    }

    public void copyTimeValue(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        if (componentState.getName().contains("cumulated")) {
            view.getComponentByReference("laborTimeForEach").setFieldValue(view.getComponentByReference("laborTimeCumulated"));
            view.getComponentByReference("machineTimeForEach")
                    .setFieldValue(view.getComponentByReference("machineTimeCumulated"));
        } else {
            view.getComponentByReference("laborTimeCumulated").setFieldValue(view.getComponentByReference("laborTimeForEach"));
            view.getComponentByReference("machineTimeCumulated")
                    .setFieldValue(view.getComponentByReference("machineTimeForEach"));
        }
    }

    // VIEW HOOK for OrderDetails
    public void setOrderDefaultValue(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecording = (FieldComponent) view.getComponentByReference("typeOfProductionRecording");

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    form.getEntityId());
            if (order == null || "".equals(order.getField("typeOfProductionRecording"))) {
                typeOfProductionRecording.setFieldValue(PARAM_RECORDING_TYPE_NONE);
            }
            for (String componentReference : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                    "registerProductionTime")) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(componentReference);
                if (order == null || order.getField(componentReference) == null) {
                    component.setFieldValue(true);
                    component.requestComponentUpdateState();
                }
            }
        } else {
            typeOfProductionRecording.setFieldValue(PARAM_RECORDING_TYPE_NONE);
            for (String componentReference : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                    "registerProductionTime")) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(componentReference);
                if (component.getFieldValue() == null) {
                    component.setFieldValue(true);
                    component.requestComponentUpdateState();
                }
            }
        }
    }

    public void checkOrderState(final ViewDefinitionState viewDefinitionState) {
        FieldComponent orderState = (FieldComponent) viewDefinitionState.getComponentByReference("state");
        if ("03inProgress".equals(orderState.getFieldValue()) || "04done".equals(orderState.getFieldValue())) {
            for (String componentName : Arrays.asList("typeOfProductionRecording", "registerQuantityInProduct",
                    "registerQuantityOutProduct", "registerProductionTime", "allowedPartial", "blockClosing", "autoCloseOrder")) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(false);
                component.requestComponentUpdateState();
            }
        }
    }

    public void checkFinalProductionCountingForOrder(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntityId());
        if (order == null) {
            return;
        }
        Boolean blockClosing = (Boolean) order.getField("blockClosing");
        FieldComponent orderState = (FieldComponent) view.getComponentByReference("state");

        List<Entity> productionRecordings = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo("order", order)).add(SearchRestrictions.eq("isFinal", true)).list()
                .getEntities();
        if (blockClosing && productionRecordings.size() == 0 && "03inProgress".equals(orderState.getFieldValue())) {
            WindowComponent window = (WindowComponent) view.getComponentByReference("window");
            RibbonActionItem start = window.getRibbon().getGroupByName("status").getItemByName("acceptOrder");
            start.setEnabled(false);
            start.setMessage("productionRecording");
            start.requestUpdate(true);
            window.requestRibbonRender();
        }

    }

}
