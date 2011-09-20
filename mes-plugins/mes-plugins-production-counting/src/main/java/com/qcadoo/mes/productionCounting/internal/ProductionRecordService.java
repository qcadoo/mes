package com.qcadoo.mes.productionCounting.internal;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchDisjunction;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
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

    public void generateNumer(final DataDefinition dd, final Entity entity) {
        entity.setField("number", numberGeneratorService.generateNumber(ProductionCountingConstants.PLUGIN_IDENTIFIER, entity
                .getDataDefinition().getName()));
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

    public void setProductBelongsToOperation(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        // TODO ALBR
    }

    public void enabledOrDisabledOperationField(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {

        ComponentState orderLookup = (ComponentState) viewDefinitionState.getComponentByReference("order");
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());
        if (order != null) {
            String typeOfProductionRecording = (String) order.getField("typeOfProductionRecording");
            if ("02cumulated".equals(typeOfProductionRecording)) {
                setComponentVisibleCumulated(viewDefinitionState);
            } else if ("03forEach".equals(typeOfProductionRecording)) {
                setComponentVisibleForEach(viewDefinitionState);
            }
        } else {
            Log.debug("order is null!!");
        }
    }

    private void setComponentVisibleCumulated(final ViewDefinitionState viewDefinitionState) {
        ((FieldComponent) viewDefinitionState.getComponentByReference("orderOperationComponent")).setVisible(false);
        ((FieldComponent) viewDefinitionState.getComponentByReference("orderOperationComponent")).requestComponentUpdateState();
        ((ComponentState) viewDefinitionState.getComponentByReference("borderLayoutConsumedTimeForEach")).setVisible(false);
        ((ComponentState) viewDefinitionState.getComponentByReference("borderLayoutConsumedTimeCumulated")).setVisible(true);
        fillInProductsGridWhenOrderStateCumulated(viewDefinitionState);
        fillOutProductsGridWhenOrderStateCumulated(viewDefinitionState);

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

    public void copyProductInAndOut(final DataDefinition dd, final Entity entity) {
        Entity productionCounting = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_RECORD).save(entity);
        Entity order = productionCounting.getBelongsToField("order");
        if (order != null || "02cumulated".equals(order.getBelongsToField("typeOfProductionRecording"))) {
            DataDefinition operationComponentDD = dataDefinitionService.get("productionScheduling", "orderOperationComponent");
            List<Entity> orderOperationComponents = operationComponentDD.find().add(SearchRestrictions.belongsTo("order", order))
                    .list().getEntities();

            if (orderOperationComponents != null || orderOperationComponents.size() != 0) {

                copyProductInAndOutForOrder(orderOperationComponents, productionCounting);
            }
        } else if (order != null || "03forEach".equals(order.getBelongsToField("typeOfProductionRecording"))) {
            List<Entity> orderOperationComponents = new ArrayList<Entity>();
            orderOperationComponents.add(productionCounting.getBelongsToField("orderOperationComponent"));
            copyProductInAndOutForOrder(orderOperationComponents, productionCounting);
        }
    }

    private void copyProductInAndOutForOrder(final List<Entity> orderOperationComponents, final Entity productionCounting) {
        checkArgument(orderOperationComponents != null, "orderOperationComponents is null");

        productionCounting.setField("recordOperationProductInComponent",
                createRecordOperationProductInComponent(orderOperationComponents, productionCounting));
        productionCounting.setField("recordOperationProductOutComponent",
                createRecordOperationProductOutComponent(orderOperationComponents, productionCounting));
    }

    private List<Entity> createRecordOperationProductInComponent(final List<Entity> orderOperationComponents,
            final Entity productionCounting) {
        checkArgument(orderOperationComponents != null, "entityList is null!");
        checkArgument(productionCounting != null, "productionCounting is null!");
        DataDefinition productInDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT);
        List<Entity> listOfProductIn = new ArrayList<Entity>();

        for (Entity orderOperationComponent : orderOperationComponents) {
            Entity operationComponent = orderOperationComponent.getBelongsToField("technologyOperationComponent");
            EntityList operationProductInComponents = operationComponent.getHasManyField("operationProductInComponents");

            for (Entity productIn : operationProductInComponents) {
                Entity recordIn = productInDD.create();
                recordIn.setField("product", productIn.getId());
                recordIn.setField("plannedQuantity", productIn.getField("quantity"));
                recordIn.setField("productionRecord", productionCounting.getId());
                // productInDD.save(recordIn);
                listOfProductIn.add(recordIn);
            }
        }
        return listOfProductIn;
    }

    private List<Entity> createRecordOperationProductOutComponent(final List<Entity> orderOperationComponents,
            final Entity productionCounting) {
        checkArgument(orderOperationComponents != null, "entityList is null!");
        checkArgument(productionCounting != null, "productionCounting is null!");
        DataDefinition productOutDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT);
        List<Entity> listOfProductOut = new ArrayList<Entity>();

        for (Entity orderOperationComponent : orderOperationComponents) {
            Entity operationComponent = orderOperationComponent.getBelongsToField("technologyOperationComponent");
            EntityList operationProductOutComponents = operationComponent.getHasManyField("operationProductOutComponents");

            for (Entity productOut : operationProductOutComponents) {
                Entity recordOut = productOutDD.create();
                recordOut.setField("product", productOut.getId());
                recordOut.setField("plannedQuantity", productOut.getField("quantity"));
                recordOut.setField("productionRecord", productionCounting.getId());
                // productOutDD.save(recordOut);
                listOfProductOut.add(recordOut);
            }
        }
        return listOfProductOut;
    }

}
