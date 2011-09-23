package com.qcadoo.mes.productionCounting.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.print.ProductionBalancePdfService;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductInOutComparator;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductionBalanceViewService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionBalancePdfService productionBalancePdfService;

    @Autowired
    private ProductionBalanceReportDataService productionBalanceReportDataService;

    public void fillFieldsWhenOrderChanged(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference("order");
        if (orderLookup.getFieldValue() == null) {
            setGridsVisibility(viewDefinitionState, false);
            clearFieldValues(viewDefinitionState);
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());
        if (order == null) {
            setGridsVisibility(viewDefinitionState, false);
            clearFieldValues(viewDefinitionState);
            return;
        }

        setFieldValues(viewDefinitionState, order);
        setGridsContent(viewDefinitionState, order);
    }

    public void fillGrids(final ViewDefinitionState viewDefinitionState) {
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference("order");
        if (orderLookup.getFieldValue() == null) {
            setGridsVisibility(viewDefinitionState, false);
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());
        if (order == null) {
            setGridsVisibility(viewDefinitionState, false);
            return;
        }

        setGridsContent(viewDefinitionState, order);
    }

    private void setFieldValues(final ViewDefinitionState viewDefinitionState, final Entity order) {
        FieldComponent productField = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        productField.setFieldValue(order.getBelongsToField("product").getId());

        FieldComponent recordsNumberField = (FieldComponent) viewDefinitionState.getComponentByReference("recordsNumber");
        Integer recordsNumberValue = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD)
                .find("where order.id=" + order.getId().toString()).list().getEntities().size();
        recordsNumberField.setFieldValue(recordsNumberValue);
    }

    private void setGridsContent(final ViewDefinitionState viewDefinitionState, final Entity order) {
        setInputProductsGridContent(viewDefinitionState, order);
        setOutputProductsGridContent(viewDefinitionState, order);
        setProductionTimeTabContent(viewDefinitionState, order);
    }

    private void clearFieldValues(final ViewDefinitionState viewDefinitionState) {
        viewDefinitionState.getComponentByReference("product").setFieldValue(null);
        viewDefinitionState.getComponentByReference("recordsNumber").setFieldValue(null);
    }

    private void setGridsVisibility(final ViewDefinitionState viewDefinitionState, final Boolean isVisible) {
        viewDefinitionState.getComponentByReference("inputProductsGrid").setVisible(isVisible);
        viewDefinitionState.getComponentByReference("outputProductsGrid").setVisible(isVisible);
        viewDefinitionState.getComponentByReference("operationsTimeGrid").setVisible(isVisible);
        viewDefinitionState.getComponentByReference("productionTimeGridLayout").setVisible(isVisible);
    }

    private void setInputProductsGridContent(final ViewDefinitionState viewDefinitionState, final Entity order) {
        GridComponent inputProducts = (GridComponent) viewDefinitionState.getComponentByReference("inputProductsGrid");
        List<Entity> inputProductsList = new ArrayList<Entity>();
        for (Entity productionRecord : order.getHasManyField("productionRecords")) {
            inputProductsList.addAll(productionRecord.getHasManyField("recordOperationProductInComponents"));
        }
        Collections.sort(inputProductsList, new EntityProductInOutComparator());
        inputProducts.setEntities(productionBalanceReportDataService.groupProductInOutComponentsByProduct(inputProductsList));
        inputProducts.setVisible(true);
    }

    private void setOutputProductsGridContent(final ViewDefinitionState viewDefinitionState, final Entity order) {
        GridComponent outputProducts = (GridComponent) viewDefinitionState.getComponentByReference("outputProductsGrid");
        List<Entity> outputProductsList = new ArrayList<Entity>();
        for (Entity productionRecord : order.getHasManyField("productionRecords")) {
            outputProductsList.addAll(productionRecord.getHasManyField("recordOperationProductOutComponents"));
        }
        Collections.sort(outputProductsList, new EntityProductInOutComparator());
        outputProducts.setEntities(productionBalanceReportDataService.groupProductInOutComponentsByProduct(outputProductsList));
        outputProducts.setVisible(true);
    }

    private void setProductionTimeTabContent(final ViewDefinitionState viewDefinitionState, final Entity order) {
        if (order.getStringField("typeOfProductionRecording").equals("03forEach")) {
            viewDefinitionState.getComponentByReference("operationsTimeGrid").setVisible(true);
            viewDefinitionState.getComponentByReference("productionTimeGridLayout").setVisible(false);
            setProductionTimeGridContent(viewDefinitionState, order);
        } else {
            viewDefinitionState.getComponentByReference("operationsTimeGrid").setVisible(false);
            viewDefinitionState.getComponentByReference("productionTimeGridLayout").setVisible(true);
            setTimeValues(viewDefinitionState, order);
        }
    }

    private void setTimeValues(final ViewDefinitionState viewDefinitionState, final Entity order) {
        BigDecimal plannedTime;
        BigDecimal machinePlannedTime = BigDecimal.ZERO;
        BigDecimal laborPlannedTime = BigDecimal.ZERO;

        List<Entity> orderOperationComponents = order.getTreeField("orderOperationComponents");
        for (Entity orderOperationComponent : orderOperationComponents) {
            plannedTime = ((BigDecimal) orderOperationComponent.getField("productionInOneCycle")).multiply(
                    new BigDecimal((Integer) orderOperationComponent.getField("tj"))).add(
                    new BigDecimal((Integer) orderOperationComponent.getField("tpz")));
            machinePlannedTime = machinePlannedTime.add(plannedTime.multiply((BigDecimal) orderOperationComponent
                    .getField("machineUtilization")));
            laborPlannedTime = laborPlannedTime.add(plannedTime.multiply((BigDecimal) orderOperationComponent
                    .getField("laborUtilization")));
        }

        BigDecimal machineRegisteredTime = BigDecimal.ZERO;
        BigDecimal laborRegisteredTime = BigDecimal.ZERO;
        for (Entity productionRecord : order.getHasManyField("productionRecords")) {
            machineRegisteredTime = machineRegisteredTime.add(new BigDecimal((Integer) productionRecord.getField("machineTime")));
            laborRegisteredTime = laborRegisteredTime.add(new BigDecimal((Integer) productionRecord.getField("laborTime")));
        }

        BigDecimal machineTimeBalance = machineRegisteredTime.subtract(machinePlannedTime);
        BigDecimal laborTimeBalance = laborRegisteredTime.subtract(laborPlannedTime);

        FieldComponent machinePlannedTimeField = (FieldComponent) viewDefinitionState
                .getComponentByReference("machinePlannedTime");
        machinePlannedTimeField.setFieldValue(productionBalancePdfService.convertTimeToString(machinePlannedTime));

        FieldComponent machineRegisteredTimeField = (FieldComponent) viewDefinitionState
                .getComponentByReference("machineRegisteredTime");
        machineRegisteredTimeField.setFieldValue(productionBalancePdfService.convertTimeToString(machineRegisteredTime));

        FieldComponent machineTimeBalanceField = (FieldComponent) viewDefinitionState
                .getComponentByReference("machineTimeBalance");
        machineTimeBalanceField.setFieldValue(productionBalancePdfService.convertTimeToString(machineTimeBalance));

        FieldComponent laborPlannedTimeField = (FieldComponent) viewDefinitionState.getComponentByReference("laborPlannedTime");
        laborPlannedTimeField.setFieldValue(productionBalancePdfService.convertTimeToString(laborPlannedTime));

        FieldComponent laborRegisteredTimeField = (FieldComponent) viewDefinitionState
                .getComponentByReference("laborRegisteredTime");
        laborRegisteredTimeField.setFieldValue(productionBalancePdfService.convertTimeToString(laborRegisteredTime));

        FieldComponent laborTimeBalanceField = (FieldComponent) viewDefinitionState.getComponentByReference("laborTimeBalance");
        laborTimeBalanceField.setFieldValue(productionBalancePdfService.convertTimeToString(laborTimeBalance));
    }

    private void setProductionTimeGridContent(final ViewDefinitionState viewDefinitionState, final Entity order) {
        GridComponent productionsTime = (GridComponent) viewDefinitionState.getComponentByReference("operationsTimeGrid");
        List<Entity> productionRecordsList = new ArrayList<Entity>(order.getHasManyField("productionRecords"));
        Collections.sort(productionRecordsList, new EntityProductInOutComparator());
        productionsTime.setEntities(productionBalanceReportDataService.groupProductionRecordsByOperation(productionRecordsList));
        productionsTime.setVisible(true);
    }

}
