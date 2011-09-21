package com.qcadoo.mes.productionCounting.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
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
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchDisjunction;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
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

    public void generateData(final DataDefinition dd, final Entity entity) {
        entity.setField("number", numberGeneratorService.generateNumber(ProductionCountingConstants.PLUGIN_IDENTIFIER, entity
                .getDataDefinition().getName()));
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
        Entity order = entity.getBelongsToField("order");
        boolean isFinal = (Boolean) entity.getField("isFinal");
        dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq("order", order));
    }

    public void setParametersDefaultValue(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity parameter = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).get(
                form.getEntityId());

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
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
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

    public void setProductBelongsToOperation(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        // TODO ALBR
    }

    public void enabledOrDisabledOperationField(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {

        ComponentState orderLookup = (ComponentState) viewDefinitionState.getComponentByReference("order");
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());
        if (order == null) {
            Log.debug("order is null!!");
            return;
        }

        String typeOfProductionRecording = (String) order.getField("typeOfProductionRecording");
        if ("02cumulated".equals(typeOfProductionRecording)) {
            setComponentVisible(false, true, false, viewDefinitionState);
            fillInProductsGridWhenOrderStateCumulated(viewDefinitionState);
            fillOutProductsGridWhenOrderStateCumulated(viewDefinitionState);
        } else if ("03forEach".equals(typeOfProductionRecording)) {
            setComponentVisible(true, false, false, viewDefinitionState);
            // fillInProductsGridWhenOrderStateForEach(viewDefinitionState);
            // fillOutProductsGridWhenOrderStateForEach(viewDefinitionState);
        } else {
            setComponentVisible(false, false, true, viewDefinitionState);
        }
    }

    private void setComponentVisible(final boolean forEach, final boolean cumulated, final boolean none,
            final ViewDefinitionState viewDefinitionState) {
        ((FieldComponent) viewDefinitionState.getComponentByReference("orderOperationComponent")).setVisible(none ? forEach
                : cumulated);
        ((FieldComponent) viewDefinitionState.getComponentByReference("orderOperationComponent")).requestComponentUpdateState();
        ((ComponentState) viewDefinitionState.getComponentByReference("borderLayoutConsumedTimeForEach")).setVisible(forEach);
        ((ComponentState) viewDefinitionState.getComponentByReference("borderLayoutConsumedTimeCumulated")).setVisible(cumulated);
        ((ComponentState) viewDefinitionState.getComponentByReference("operationNoneLabel")).setVisible(none);
    }

    private void fillInProductsGridWhenOrderStateCumulated(final ViewDefinitionState viewDefinitionState) {
        checkArgument(viewDefinitionState != null, "viewDefinitionState is null");
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("recordOperationProductInComponent");
        ComponentState orderLookup = (ComponentState) viewDefinitionState.getComponentByReference("order");
        Long orderId = (Long) orderLookup.getFieldValue();
        if (orderId == null || grid == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
        Entity technology = order.getBelongsToField("technology");

        DataDefinition dd = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);

        SearchDisjunction disjunction = SearchRestrictions.disjunction();
        for (Entity operationComponent : technology.getTreeField("operationComponents")) {
            disjunction.add(SearchRestrictions.belongsTo("operationComponent", operationComponent));
        }

        SearchResult searchResult = dd.find().add(disjunction).createAlias("product", "product")
                .addOrder(SearchOrders.asc("product.name")).list();

        grid.setEntities(searchResult.getEntities());

    }

    private void fillOutProductsGridWhenOrderStateCumulated(final ViewDefinitionState viewDefinitionState) {
        checkArgument(viewDefinitionState != null, "viewDefinitionState is null");
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("recordOperationProductOutComponent");
        ComponentState orderLookup = (ComponentState) viewDefinitionState.getComponentByReference("order");
        Long orderId = (Long) orderLookup.getFieldValue();
        if (orderId == null || grid == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
        Entity technology = order.getBelongsToField("technology");

        DataDefinition dd = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);

        SearchDisjunction disjunction = SearchRestrictions.disjunction();
        for (Entity operationComponent : technology.getTreeField("operationComponents")) {
            disjunction.add(SearchRestrictions.belongsTo("operationComponent", operationComponent));
        }

        SearchResult searchResult = dd.find().add(disjunction).createAlias("product", "product")
                .addOrder(SearchOrders.asc("product.name")).list();

        grid.setEntities(searchResult.getEntities());
    }

    private void setComponentVisibleForEach(final ViewDefinitionState viewDefinitionState) {
        ((FieldComponent) viewDefinitionState.getComponentByReference("orderOperationComponent")).setVisible(true);
        ((ComponentState) viewDefinitionState.getComponentByReference("borderLayoutConsumedTimeForEach")).setVisible(true);
        ((ComponentState) viewDefinitionState.getComponentByReference("borderLayoutConsumedTimeCumulated")).setVisible(false);
        fillInProductsGridWhenOrderStateForEach(viewDefinitionState);
        // fillOutProductsGridWhenOrderStateForEach(viewDefinitionState);

    }

    private void fillInProductsGridWhenOrderStateForEach(final ViewDefinitionState viewDefinitionState) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("recordOperationProductInComponent");
        ComponentState orderLookup = (ComponentState) viewDefinitionState.getComponentByReference("order");
        Long orderId = (Long) orderLookup.getFieldValue();
        if (orderId == null || grid == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
        Entity technology = order.getBelongsToField("technology");

        DataDefinition dd = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);

        SearchDisjunction disjunction = SearchRestrictions.disjunction();
        for (Entity operationComponent : technology.getTreeField("operationComponents")) {
            disjunction.add(SearchRestrictions.belongsTo("operationComponent", operationComponent));
        }

        SearchResult searchResult = dd.find().add(disjunction).createAlias("operation", "operation")
                .addOrder(SearchOrders.asc("product.name")).list();

        grid.setEntities(searchResult.getEntities());
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
        checkArgument(modelName != null, "missing model name argument");
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
