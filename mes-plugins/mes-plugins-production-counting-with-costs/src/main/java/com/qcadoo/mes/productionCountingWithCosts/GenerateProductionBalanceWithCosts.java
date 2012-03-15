package com.qcadoo.mes.productionCountingWithCosts;

import static com.qcadoo.mes.costCalculation.constants.CostCalculationFields.QUANTITY;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.lowagie.text.DocumentException;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.costCalculation.CostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceReportDataService;
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceService;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductionRecordOperationComparator;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionCountingWithCostsConstants;
import com.qcadoo.mes.productionCountingWithCosts.pdf.ProductionBalanceWithCostsPdfService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.SortUtil;

@Service
public class GenerateProductionBalanceWithCosts implements Observer {

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    private static final String L_STATE = "state";

    @Autowired
    private NumberService numberService;

    @Autowired
    private CostCalculationService costCalculationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ProductionBalanceWithCostsPdfService productionBalanceWithCostsPdfService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionBalanceService productionBalanceService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ProductionBalanceReportDataService productionBalanceReportDataService;

    @Override
    public void update(Observable arg0, Object arg1) {
        Entity balance = (Entity) arg1;

        doTheCostsPart(balance);
        generateBalanceWithCostsReport(balance);
    }

    private void generateBalanceWithCostsReport(final Entity balance) {
        Locale locale = LocaleContextHolder.getLocale();

        String localePrefix = "productionCounting.productionBalanceWithCosts.report.fileName";

        Entity productionBalanceWithFileName = fileService.updateReportFileName(balance, "date", localePrefix);

        String localePrefixToMatch = localePrefix;

        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();

        try {
            productionBalanceWithCostsPdfService.generateDocument(productionBalanceWithFileName, company, locale,
                    localePrefixToMatch);

            balance.setField(ProductionBalanceFieldsPCWC.GENERATED_WITH_COSTS, Boolean.TRUE);
        } catch (IOException e) {
            throw new RuntimeException("Problem with saving productionBalanceWithCosts report");
        } catch (DocumentException e) {
            throw new RuntimeException("Problem with generating productionBalanceWithCosts report");
        }

        balance.getDataDefinition().save(balance);
    }

    void doTheCostsPart(final Entity balance) {
        Entity order = balance.getBelongsToField("order");
        Entity technology = order.getBelongsToField("technology");

        BigDecimal quantity = (BigDecimal) order.getField("plannedQuantity");
        balance.setField("quantity", quantity);
        balance.setField("technology", technology);

        costCalculationService.calculateTotalCost(balance);

        BigDecimal totalTechnicalProductionCosts = (BigDecimal) balance.getField("totalTechnicalProductionCosts");
        BigDecimal perUnit = totalTechnicalProductionCosts.divide(quantity, numberService.getMathContext());
        balance.setField("totalTechnicalProductionCostPerUnit", numberService.setScale(perUnit));

        balance.getDataDefinition().save(balance);

        fillFieldsAndGrids(balance);
    }

    private void fillFieldsAndGrids(final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(OrdersConstants.MODEL_ORDER);

        if ((order == null) || productionBalanceService.checkIfTypeOfProductionRecordingIsBasic(order)) {
            return;
        }

        fillMaterialValues(productionBalance, order);
        fillOrderOperationProductInComponents(productionBalance, order);

        if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING))) {
            fillCostValues(productionBalance, order);
            fillOperationCostComponents(productionBalance, order);
        } else if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING))) {
            fillCostValues(productionBalance, order);
        }
    }

    private void fillMaterialValues(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        BigDecimal plannedComponentsCosts = BigDecimal.ZERO;
        BigDecimal componentsCosts = BigDecimal.ZERO;

        Entity technology = productionBalance.getBelongsToField("technology");

        BigDecimal givenQty = (BigDecimal) productionBalance.getField(QUANTITY);

        Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getNeededProductQuantities(technology, givenQty,
                true);

        productQuantities = SortUtil.sortMapUsingComparator(productQuantities, new EntityNumberComparator());

        for (Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
            Entity product = productQuantity.getKey();
            BigDecimal quantity = BigDecimal.ZERO;

            Entity balanceOperationProductInComponent = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT).find()
                    .add(SearchRestrictions.belongsTo(ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, productionBalance))
                    .add(SearchRestrictions.belongsTo("product", product)).setMaxResults(1).uniqueResult();

            if (balanceOperationProductInComponent != null) {
                BigDecimal nominalCost = (BigDecimal) product.getField("nominalCost");
                BigDecimal costForNumber = (BigDecimal) product.getField("costForNumber");

                BigDecimal totalCostPerUnit = nominalCost.divide(costForNumber, numberService.getMathContext());

                BigDecimal registeredQuantity = (BigDecimal) balanceOperationProductInComponent.getField("usedQuantity");

                plannedComponentsCosts = plannedComponentsCosts.add(
                        quantity.multiply(totalCostPerUnit, numberService.getMathContext()), numberService.getMathContext());

                componentsCosts = componentsCosts.add(
                        registeredQuantity.multiply(totalCostPerUnit, numberService.getMathContext()),
                        numberService.getMathContext());
            }
        }

        BigDecimal componentsCostsBalance = componentsCosts.subtract(plannedComponentsCosts, numberService.getMathContext());

        productionBalance.setField("plannedComponentsCosts", plannedComponentsCosts);
        productionBalance.setField("componentsCosts", componentsCosts);

        productionBalance.setField("componentsCostsBalance", componentsCostsBalance);

        productionBalance.getDataDefinition().save(productionBalance);

    }

    private void fillOrderOperationProductInComponents(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> orderOperationProductInComponents = Lists.newArrayList();

        Entity technology = productionBalance.getBelongsToField("technology");

        BigDecimal givenQty = (BigDecimal) productionBalance.getField(QUANTITY);

        Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getNeededProductQuantities(technology, givenQty,
                true);

        productQuantities = SortUtil.sortMapUsingComparator(productQuantities, new EntityNumberComparator());

        for (Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
            Entity product = productQuantity.getKey();
            BigDecimal quantity = BigDecimal.ZERO;

            Entity balanceOperationProductInComponent = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT).find()
                    .add(SearchRestrictions.belongsTo(ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, productionBalance))
                    .add(SearchRestrictions.belongsTo("product", product)).setMaxResults(1).uniqueResult();

            if (balanceOperationProductInComponent != null) {
                Entity orderOperationProductInComponent = dataDefinitionService.get(
                        ProductionCountingWithCostsConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingWithCostsConstants.MODEL_ORDER_OPERATION_PRODUCT_IN_COMPONENT).create();

                BigDecimal nominalCost = (BigDecimal) product.getField("nominalCost");
                BigDecimal costForNumber = (BigDecimal) product.getField("costForNumber");

                BigDecimal totalCostPerUnit = nominalCost.divide(costForNumber, numberService.getMathContext());

                BigDecimal registeredQuantity = (BigDecimal) balanceOperationProductInComponent.getField("usedQuantity");

                BigDecimal plannedCost = quantity.multiply(totalCostPerUnit, numberService.getMathContext());

                BigDecimal registeredCost = registeredQuantity.multiply(totalCostPerUnit, numberService.getMathContext());

                BigDecimal balance = registeredCost.subtract(plannedCost, numberService.getMathContext());

                orderOperationProductInComponent
                        .setField(ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, productionBalance);
                orderOperationProductInComponent.setField("product", product);
                orderOperationProductInComponent.setField("plannedCost", plannedCost);
                orderOperationProductInComponent.setField("registeredCost", registeredCost);
                orderOperationProductInComponent.setField("balance", balance);

                orderOperationProductInComponents.add(orderOperationProductInComponent);
            }
        }

        productionBalance.setField(ProductionBalanceFieldsPCWC.ORDER_OPERATION_PRODUCT_IN_COMPONENTS,
                orderOperationProductInComponents);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillOperationCostComponents(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> operationCostComponents = Lists.newArrayList();

        List<Entity> productionRecordsList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(L_STATE, ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(OrdersConstants.MODEL_ORDER, order)).list().getEntities();

        if (!productionRecordsList.isEmpty()) {
            Collections.sort(productionRecordsList, new EntityProductionRecordOperationComparator());

            List<Entity> operationTimeComponents = productionBalanceReportDataService
                    .groupProductionRecordsByOperation(productionRecordsList);

            for (Entity operatonTimeComponent : operationTimeComponents) {
                Entity calculationOperationComponent = dataDefinitionService
                        .get(CostNormsForOperationConstants.PLUGIN_IDENTIFIER,
                                CostNormsForOperationConstants.MODEL_CALCULATION_OPERATION_COMPONENT)
                        .find()
                        .add(SearchRestrictions
                                .belongsTo(ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, productionBalance))
                        .add(SearchRestrictions.belongsTo("technologyOperationComponent", operatonTimeComponent
                                .getBelongsToField("orderOperationComponent").getBelongsToField("technologyOperationComponent")))
                        .setMaxResults(1).uniqueResult();

                if (calculationOperationComponent != null) {
                    Entity operationCostComponent = dataDefinitionService.get(
                            ProductionCountingWithCostsConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingWithCostsConstants.MODEL_OPERATION_COST_COMPONENT).create();

                    BigDecimal milisecondsInHour = BigDecimal.valueOf(3600);

                    BigDecimal machineUtilization = (BigDecimal) calculationOperationComponent.getField("machineUtilization");
                    BigDecimal machineHourlyCost = (BigDecimal) calculationOperationComponent.getField("machineHourlyCost");

                    Integer plannedMachineTime = (Integer) operatonTimeComponent.getField("plannedMachineTime");
                    BigDecimal plannedMachineTimeHours = BigDecimal.valueOf(plannedMachineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal plannedMachineCosts = (BigDecimal) machineUtilization.multiply(machineHourlyCost).multiply(
                            plannedMachineTimeHours);

                    Integer machineTime = (Integer) operatonTimeComponent.getField("machineTime");
                    BigDecimal machineTimeHours = BigDecimal.valueOf(machineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal machineCosts = (BigDecimal) machineUtilization.multiply(machineHourlyCost).multiply(
                            machineTimeHours);

                    BigDecimal machineCostsBalance = machineCosts.subtract(plannedMachineCosts, numberService.getMathContext());

                    BigDecimal laborUtilization = (BigDecimal) calculationOperationComponent.getField("laborUtilization");
                    BigDecimal laborHourlyCost = (BigDecimal) calculationOperationComponent.getField("laborHourlyCost");

                    Integer plannedLaborTime = (Integer) operatonTimeComponent.getField("plannedLaborTime");
                    BigDecimal plannedLaborTimeHours = BigDecimal.valueOf(plannedLaborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal plannedLaborCosts = (BigDecimal) laborUtilization.multiply(laborHourlyCost).multiply(
                            plannedLaborTimeHours);

                    Integer laborTime = (Integer) operatonTimeComponent.getField("laborTime");
                    BigDecimal laborTimeHours = BigDecimal.valueOf(laborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal laborCosts = (BigDecimal) laborUtilization.multiply(laborHourlyCost).multiply(laborTimeHours);

                    BigDecimal laborCostsBalance = laborCosts.subtract(plannedLaborCosts, numberService.getMathContext());

                    operationCostComponent.setField("productionBalance", productionBalance);
                    operationCostComponent.setField("orderOperationComponent",
                            operatonTimeComponent.getBelongsToField("orderOperationComponent"));

                    operationCostComponent.setField("plannedMachineCosts", plannedMachineCosts);
                    operationCostComponent.setField("machineCosts", machineCosts);
                    operationCostComponent.setField("machineCostsBalance", machineCostsBalance);

                    operationCostComponent.setField("plannedLaborCosts", plannedLaborCosts);
                    operationCostComponent.setField("laborCosts", laborCosts);
                    operationCostComponent.setField("laborCostsBalance", laborCostsBalance);

                    operationCostComponents.add(operationCostComponent);
                }
            }
        }

        productionBalance.setField(ProductionBalanceFieldsPCWC.OPERATION_COST_COMPONENTS, operationCostComponents);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillCostValues(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        BigDecimal plannedMachineCosts = BigDecimal.ZERO;
        BigDecimal machineCosts = BigDecimal.ZERO;

        BigDecimal laborCosts = BigDecimal.ZERO;
        BigDecimal plannedLaborCosts = BigDecimal.ZERO;

        List<Entity> productionRecordsList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(L_STATE, ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(OrdersConstants.MODEL_ORDER, order)).list().getEntities();

        if (!productionRecordsList.isEmpty()) {
            Collections.sort(productionRecordsList, new EntityProductionRecordOperationComparator());

            List<Entity> operationTimeComponents = productionBalanceReportDataService
                    .groupProductionRecordsByOperation(productionRecordsList);

            for (Entity operatonTimeComponent : operationTimeComponents) {
                Entity calculationOperationComponent = dataDefinitionService
                        .get(CostNormsForOperationConstants.PLUGIN_IDENTIFIER,
                                CostNormsForOperationConstants.MODEL_CALCULATION_OPERATION_COMPONENT)
                        .find()
                        .add(SearchRestrictions
                                .belongsTo(ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, productionBalance))
                        .add(SearchRestrictions.belongsTo("technologyOperationComponent", operatonTimeComponent
                                .getBelongsToField("orderOperationComponent").getBelongsToField("technologyOperationComponent")))
                        .setMaxResults(1).uniqueResult();

                if (calculationOperationComponent != null) {
                    BigDecimal milisecondsInHour = BigDecimal.valueOf(3600);

                    BigDecimal machineUtilization = (BigDecimal) calculationOperationComponent.getField("machineUtilization");
                    BigDecimal machineHourlyCost = (BigDecimal) calculationOperationComponent.getField("machineHourlyCost");

                    Integer plannedMachineTime = (Integer) operatonTimeComponent.getField("plannedMachineTime");
                    BigDecimal plannedMachineTimeHours = BigDecimal.valueOf(plannedMachineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    plannedMachineCosts = plannedMachineCosts.add((BigDecimal) machineUtilization.multiply(machineHourlyCost)
                            .multiply(plannedMachineTimeHours), numberService.getMathContext());

                    Integer machineTime = (Integer) operatonTimeComponent.getField("machineTime");
                    BigDecimal machineTimeHours = BigDecimal.valueOf(machineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    machineCosts = machineCosts.add(
                            (BigDecimal) machineUtilization.multiply(machineHourlyCost).multiply(machineTimeHours),
                            numberService.getMathContext());

                    BigDecimal laborUtilization = (BigDecimal) calculationOperationComponent.getField("laborUtilization");
                    BigDecimal laborHourlyCost = (BigDecimal) calculationOperationComponent.getField("laborHourlyCost");

                    Integer plannedLaborTime = (Integer) operatonTimeComponent.getField("plannedLaborTime");
                    BigDecimal plannedLaborTimeHours = BigDecimal.valueOf(plannedLaborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    plannedLaborCosts = plannedLaborCosts.add(
                            (BigDecimal) laborUtilization.multiply(laborHourlyCost).multiply(plannedLaborTimeHours),
                            numberService.getMathContext());

                    Integer laborTime = (Integer) operatonTimeComponent.getField("laborTime");
                    BigDecimal laborTimeHours = BigDecimal.valueOf(laborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    laborCosts = laborCosts.add((BigDecimal) laborUtilization.multiply(laborHourlyCost).multiply(laborTimeHours),
                            numberService.getMathContext());
                }
            }
        }

        BigDecimal machineCostsBalance = machineCosts.subtract(plannedMachineCosts, numberService.getMathContext());
        BigDecimal laborCostsBalance = laborCosts.subtract(plannedLaborCosts, numberService.getMathContext());

        productionBalance.setField("plannedMachineCosts", plannedMachineCosts);
        productionBalance.setField("machineCosts", machineCosts);
        productionBalance.setField("machineCostsBalance", machineCostsBalance);

        productionBalance.setField("plannedCostsTime", plannedLaborCosts);
        productionBalance.setField("laborCosts", laborCosts);
        productionBalance.setField("laborCostsBalance", laborCostsBalance);

        productionBalance.getDataDefinition().save(productionBalance);
    }
}
