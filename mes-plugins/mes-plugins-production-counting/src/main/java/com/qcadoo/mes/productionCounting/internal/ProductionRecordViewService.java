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

@Service
public class ProductionRecordViewService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

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

        view.getComponentByReference("recordOperationProductOutComponent").setVisible(
                getBooleanValue(order.getField("registerQuantityOutProduct")));
        view.getComponentByReference("recordOperationProductInComponent").setVisible(
                getBooleanValue(order.getField("registerQuantityInProduct")));
        view.getComponentByReference("borderLayoutConsumedTimeCumulated").setVisible(
                getBooleanValue(order.getField("registerProductionTime")));
        view.getComponentByReference("borderLayoutConsumedTimeForEach").setVisible(
                getBooleanValue(order.getField("registerProductionTime")));
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
        view.getComponentByReference("orderOperationComponent").setEnabled(PARAM_RECORDING_TYPE_FOREACH.equals(recordingType));

        view.getComponentByReference("borderLayoutConsumedTimeForEach").setVisible(
                PARAM_RECORDING_TYPE_FOREACH.equals(recordingType));
        view.getComponentByReference("borderLayoutConsumedTimeCumulated").setVisible(
                PARAM_RECORDING_TYPE_CUMULATED.equals(recordingType));
        view.getComponentByReference("operationNoneLabel").setVisible(
                !PARAM_RECORDING_TYPE_CUMULATED.equals(recordingType) && !PARAM_RECORDING_TYPE_FOREACH.equals(recordingType));

        ((FieldComponent) view.getComponentByReference("orderOperationComponent")).requestComponentUpdateState();
    }

    public void registeringProductionTime(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            return;
        }
        Boolean registerProductionTime = getBooleanValue(order.getField("registerProductionTime"));
        if (registerProductionTime) {
            view.getComponentByReference("borderLayoutConsumedTimeForEach").setVisible(false);
            view.getComponentByReference("borderLayoutConsumedTimeCumulated").setVisible(false);
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

    public void checkFinalProductionRecording(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (!("04done".equals(order.getField("state")))) {
            return;
        }
        List<Entity> productionRecordings = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo("order", order)).add(SearchRestrictions.eq("isFinal", true)).list()
                .getEntities();
        if (productionRecordings.size() == 0) {
            componentState.addMessage(
                    translationService.translate("orders.order.error.productionCounting.final", view.getLocale()),
                    MessageType.FAILURE);
            return;
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
        ((FieldComponent) view.getComponentByReference("number")).requestComponentUpdateState();
        ((FieldComponent) view.getComponentByReference("name")).requestComponentUpdateState();
        ((FieldComponent) view.getComponentByReference("type")).requestComponentUpdateState();
    }

    private Entity getOrderFromLookup(final ViewDefinitionState view) {
        ComponentState lookup = view.getComponentByReference("order");
        if (!(lookup.getFieldValue() instanceof Long)) {
            return null;
        }
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER).get((Long) lookup.getFieldValue());
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

}
