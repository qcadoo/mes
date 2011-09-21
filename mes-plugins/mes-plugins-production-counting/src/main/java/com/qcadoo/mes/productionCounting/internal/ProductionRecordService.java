package com.qcadoo.mes.productionCounting.internal;

import static com.google.common.collect.Lists.newArrayList;
import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PARAMETER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PLUGIN_IDENTIFIER;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialRequirements.api.MaterialRequirementReportDataService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductionRecordService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    MaterialRequirementReportDataService materialRequirementReportDataService;

    @Autowired
    NumberGeneratorService numberGeneratorService;

    @Autowired
    SecurityService securityService;

    private final static String CUMULATE = "02cumulated";

    private final static String FOR_EACH_OPERATION = "03forEach";

    public void generateData(final DataDefinition dd, final Entity entity) {
        entity.setField("number", numberGeneratorService.generateNumber(PLUGIN_IDENTIFIER, entity.getDataDefinition().getName()));
        entity.setField("creationTime", new Date());
        entity.setField("worker", securityService.getCurrentUserName());
    }

    public void checkTypeOfProductionRecording(final DataDefinition dd, final Entity entity) {
        Entity order = entity.getBelongsToField("order");
        String typeOfProductionRecording = order.getStringField("typeOfProductionRecording");
        if (typeOfProductionRecording == null || "01none".equals(typeOfProductionRecording)) {
            entity.addError(dd.getField("order"), "productionCounting.validate.global.error.productionRecord.orderError");
        }
    }

    public void checkFinal(final DataDefinition dd, final Entity entity) {

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

    public void setOrderDefaultValue(final ViewDefinitionState viewDefinitionState) {
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference("typeOfProductionRecording");

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        if (form.getEntityId() != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER).get(
                    (Long) form.getEntityId());
            if (order == null || "".equals(order.getField("typeOfProductionRecording"))) {
                typeOfProductionRecording.setFieldValue("01none");
            }
            for (String componentReference : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                    "registerProductionTime")) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
                if (order == null || order.getField(componentReference) == null) {
                    component.setFieldValue(true);
                    component.requestComponentUpdateState();
                }
            }
        } else {
            typeOfProductionRecording.setFieldValue("01none");
            for (String componentReference : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                    "registerProductionTime")) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
                if (component.getFieldValue() == null) {
                    component.setFieldValue(true);
                    component.requestComponentUpdateState();
                }
            }
        }
    }

    public void checkStateOrder(final ViewDefinitionState viewDefinitionState) {
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

    public void enabledOrDisabledOperationField(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        Long orderId = (Long) viewDefinitionState.getComponentByReference("order").getFieldValue();
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER).get(orderId);
        if (order == null) {
            Log.debug("order is null!!");
            return;
        }
        String typeOfProductionRecording = (String) order.getField("typeOfProductionRecording");
        setComponentVisible((String) order.getField("typeOfProductionRecording"), viewDefinitionState);

        if ("02cumulated".equals(typeOfProductionRecording)) {
            fillProductsGrid(viewDefinitionState, MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT);
            fillProductsGrid(viewDefinitionState, MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT);
        }
    }

    private void fillProductsGrid(final ViewDefinitionState view, final String modelName) {
        GridComponent grid = (GridComponent) view.getComponentByReference(modelName);
        Entity productionRecord = ((FormComponent) view.getComponentByReference("form")).getEntity();
        if (productionRecord.getId() == null) {
            return;
        }
        grid.setEntities(productionRecord.getHasManyField(modelName + 's'));
    }

    private void setComponentVisible(final String recordingType, final ViewDefinitionState view) {
        view.getComponentByReference("orderOperationComponent").setVisible(
                FOR_EACH_OPERATION.equals(recordingType) || CUMULATE.equals(recordingType));
        view.getComponentByReference("borderLayoutConsumedTimeForEach").setVisible(FOR_EACH_OPERATION.equals(recordingType));
        view.getComponentByReference("borderLayoutConsumedTimeCumulated").setVisible(CUMULATE.equals(recordingType));
        view.getComponentByReference("operationNoneLabel").setVisible(
                !CUMULATE.equals(recordingType) && !FOR_EACH_OPERATION.equals(recordingType));

        ((FieldComponent) view.getComponentByReference("orderOperationComponent")).requestComponentUpdateState();
    }

    public void copyProductInAndOut(final DataDefinition dd, final Entity productionRecord) {
        Entity order = productionRecord.getBelongsToField("order");
        String typeOfProductionRecording = order.getStringField("typeOfProductionRecording");
        List<Entity> operationComponents = null;

        for (String fieldName : newArrayList("recordOperationProductInComponents", "recordOperationProductOutComponents")) {
            if (productionRecord.getHasManyField(fieldName) != null) {
                return;
            }
        }

        if ("02cumulated".equals(typeOfProductionRecording)) {
            operationComponents = order.getTreeField("orderOperationComponents");
        } else if ("03forEach".equals(typeOfProductionRecording)) {
            operationComponents = newArrayList(productionRecord.getBelongsToField("orderOperationComponent"));
        }

        copyOperationProductComponents(operationComponents, productionRecord, MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT);
        copyOperationProductComponents(operationComponents, productionRecord, MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT);
    }

    // TODO products list should be distinct?
    private void copyOperationProductComponents(final List<Entity> orderOperations, final Entity productionRecord,
            final String modelName) {
        if (orderOperations == null) {
            return;
        }

        DataDefinition recordProductDD = dataDefinitionService.get(PLUGIN_IDENTIFIER, modelName);
        List<Entity> products = newArrayList();
        String technologyProductFieldName = "operationProductOutComponents";
        String recordProductFieldName = "recordOperationProductOutComponents";

        if (MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT.equals(modelName)) {
            technologyProductFieldName = "operationProductInComponents";
            recordProductFieldName = "recordOperationProductInComponents";
        }

        for (Entity orderOperation : orderOperations) {
            List<Entity> technologyProducts = orderOperation.getBelongsToField("technologyOperationComponent").getHasManyField(
                    technologyProductFieldName);
            if (technologyProducts == null) {
                continue;
            }

            for (Entity technologyProduct : technologyProducts) {
                Entity recordProduct = recordProductDD.create();
                recordProduct.setField("product", technologyProduct.getField("product"));
                recordProduct.setField("plannedQuantity", technologyProduct.getField("quantity"));
                products.add(recordProduct);
            }
        }
        productionRecord.setField(recordProductFieldName, products);
    }
}
