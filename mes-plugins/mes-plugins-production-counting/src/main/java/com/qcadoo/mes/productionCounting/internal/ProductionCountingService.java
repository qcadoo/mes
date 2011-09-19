package com.qcadoo.mes.productionCounting.internal;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;

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
public class ProductionCountingService {

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
        // fillInProductsGridWhenOrderStateForEach(viewDefinitionState);
        // fillOutProductsGridWhenOrderStateForEach(viewDefinitionState);

    }

    private void fillInProductsGridWhenOrderStateForEach(final ViewDefinitionState viewDefinitionState) {
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

        EntityList operations = (EntityList) technology.getTreeField("operationComponents");

        SearchDisjunction disjunction = SearchRestrictions.disjunction();
        for (Entity operationComponent : technology.getTreeField("operationComponents")) {
            disjunction.add(SearchRestrictions.belongsTo("operationComponent", operationComponent));
        }

        SearchResult searchResult = dd.find().add(disjunction).createAlias("operation", "operation")
                .addOrder(SearchOrders.asc("product.name")).list();

        grid.setEntities(searchResult.getEntities());
    }
}
