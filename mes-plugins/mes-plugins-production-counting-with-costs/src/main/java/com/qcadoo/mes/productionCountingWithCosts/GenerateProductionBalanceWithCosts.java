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
import com.qcadoo.mes.costNormsForProduct.ProductsCostCalculationService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceReportDataService;
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceService;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductionRecordOperationComparator;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionCountingWithCostsConstants;
import com.qcadoo.mes.productionCountingWithCosts.pdf.ProductionBalanceWithCostsPdfService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;

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
    private ProductsCostCalculationService productsCostCalculationService;

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

        sumarizeCostValues(productionBalance, order);
    }

    private void fillMaterialValues(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        BigDecimal plannedComponentsCosts = BigDecimal.ZERO;
        BigDecimal componentsCosts = BigDecimal.ZERO;

        BigDecimal givenQty = (BigDecimal) productionBalance.getField(QUANTITY);

        Map<Entity, BigDecimal> productQuantities = productsCostCalculationService.getProductWithCostForPlannedQuantities(
                productionBalance.getBelongsToField("technology"), givenQty,
                productionBalance.getStringField(ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE));

        for (Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
            Entity product = productQuantity.getKey();
            BigDecimal productCost = productQuantity.getValue();

            Entity balanceOperationProductInComponent = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT).find()
                    .add(SearchRestrictions.belongsTo(ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, productionBalance))
                    .add(SearchRestrictions.belongsTo("product", product)).setMaxResults(1).uniqueResult();

            if (balanceOperationProductInComponent != null) {
                BigDecimal registeredQuantity = (BigDecimal) balanceOperationProductInComponent.getField("usedQuantity");

                BigDecimal productRegisteredCost = BigDecimal.ZERO;

                if (registeredQuantity != null) {
                    productRegisteredCost = productsCostCalculationService.calculateProductCostForGivenQuantity(product,
                            registeredQuantity,
                            productionBalance.getStringField(ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE));
                }

                plannedComponentsCosts = plannedComponentsCosts.add(productCost, numberService.getMathContext());

                componentsCosts = componentsCosts.add(productRegisteredCost, numberService.getMathContext());
            }
        }

        BigDecimal componentsCostsBalance = componentsCosts.subtract(plannedComponentsCosts, numberService.getMathContext());

        productionBalance.setField("plannedComponentsCosts", numberService.setScale(plannedComponentsCosts));
        productionBalance.setField("componentsCosts", numberService.setScale(componentsCosts));

        productionBalance.setField("componentsCostsBalance", numberService.setScale(componentsCostsBalance));

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillOrderOperationProductInComponents(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> orderOperationProductInComponents = Lists.newArrayList();

        BigDecimal givenQty = (BigDecimal) productionBalance.getField(QUANTITY);

        Map<Entity, BigDecimal> productQuantities = productsCostCalculationService.getProductWithCostForPlannedQuantities(
                productionBalance.getBelongsToField("technology"), givenQty,
                productionBalance.getStringField(ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE));

        for (Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
            Entity product = productQuantity.getKey();
            BigDecimal productCost = productQuantity.getValue();

            Entity balanceOperationProductInComponent = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT).find()
                    .add(SearchRestrictions.belongsTo(ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, productionBalance))
                    .add(SearchRestrictions.belongsTo("product", product)).setMaxResults(1).uniqueResult();

            if (balanceOperationProductInComponent != null) {
                Entity orderOperationProductInComponent = dataDefinitionService.get(
                        ProductionCountingWithCostsConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingWithCostsConstants.MODEL_ORDER_OPERATION_PRODUCT_IN_COMPONENT).create();

                BigDecimal registeredQuantity = (BigDecimal) balanceOperationProductInComponent.getField("usedQuantity");

                BigDecimal productRegisteredCost = BigDecimal.ZERO;

                if (registeredQuantity != null) {
                    productRegisteredCost = productsCostCalculationService.calculateProductCostForGivenQuantity(product,
                            registeredQuantity,
                            productionBalance.getStringField(ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE));
                }

                BigDecimal balance = productRegisteredCost.subtract(productCost, numberService.getMathContext());

                orderOperationProductInComponent
                        .setField(ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, productionBalance);
                orderOperationProductInComponent.setField("product", product);
                orderOperationProductInComponent.setField("plannedCost", numberService.setScale(productCost));
                orderOperationProductInComponent.setField("registeredCost", numberService.setScale(productRegisteredCost));
                orderOperationProductInComponent.setField("balance", numberService.setScale(balance));

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

                    BigDecimal machineHourlyCost = (BigDecimal) calculationOperationComponent.getField("machineHourlyCost");

                    Integer plannedMachineTime = (Integer) operatonTimeComponent.getField("plannedMachineTime");
                    BigDecimal plannedMachineTimeHours = BigDecimal.valueOf(plannedMachineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal plannedMachineCosts = (BigDecimal) machineHourlyCost.multiply(plannedMachineTimeHours);

                    Integer machineTime = (Integer) operatonTimeComponent.getField("machineTime");
                    BigDecimal machineTimeHours = BigDecimal.valueOf(machineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal machineCosts = (BigDecimal) machineHourlyCost.multiply(machineTimeHours);

                    BigDecimal machineCostsBalance = machineCosts.subtract(plannedMachineCosts, numberService.getMathContext());

                    BigDecimal laborHourlyCost = (BigDecimal) calculationOperationComponent.getField("laborHourlyCost");

                    Integer plannedLaborTime = (Integer) operatonTimeComponent.getField("plannedLaborTime");
                    BigDecimal plannedLaborTimeHours = BigDecimal.valueOf(plannedLaborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal plannedLaborCosts = (BigDecimal) laborHourlyCost.multiply(plannedLaborTimeHours);

                    Integer laborTime = (Integer) operatonTimeComponent.getField("laborTime");
                    BigDecimal laborTimeHours = BigDecimal.valueOf(laborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal laborCosts = (BigDecimal) laborHourlyCost.multiply(laborTimeHours);

                    BigDecimal laborCostsBalance = laborCosts.subtract(plannedLaborCosts, numberService.getMathContext());

                    operationCostComponent.setField("productionBalance", productionBalance);
                    operationCostComponent.setField("orderOperationComponent",
                            operatonTimeComponent.getBelongsToField("orderOperationComponent"));

                    operationCostComponent.setField("plannedMachineCosts", numberService.setScale(plannedMachineCosts));
                    operationCostComponent.setField("machineCosts", numberService.setScale(machineCosts));
                    operationCostComponent.setField("machineCostsBalance", numberService.setScale(machineCostsBalance));

                    operationCostComponent.setField("plannedLaborCosts", numberService.setScale(plannedLaborCosts));
                    operationCostComponent.setField("laborCosts", numberService.setScale(laborCosts));
                    operationCostComponent.setField("laborCostsBalance", numberService.setScale(laborCostsBalance));

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

                    BigDecimal machineHourlyCost = (BigDecimal) calculationOperationComponent.getField("machineHourlyCost");

                    Integer plannedMachineTime = (Integer) operatonTimeComponent.getField("plannedMachineTime");
                    BigDecimal plannedMachineTimeHours = BigDecimal.valueOf(plannedMachineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    plannedMachineCosts = plannedMachineCosts.add(
                            (BigDecimal) machineHourlyCost.multiply(plannedMachineTimeHours), numberService.getMathContext());

                    Integer machineTime = (Integer) operatonTimeComponent.getField("machineTime");
                    BigDecimal machineTimeHours = BigDecimal.valueOf(machineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    machineCosts = machineCosts.add((BigDecimal) machineHourlyCost.multiply(machineTimeHours),
                            numberService.getMathContext());

                    BigDecimal laborHourlyCost = (BigDecimal) calculationOperationComponent.getField("laborHourlyCost");

                    Integer plannedLaborTime = (Integer) operatonTimeComponent.getField("plannedLaborTime");
                    BigDecimal plannedLaborTimeHours = BigDecimal.valueOf(plannedLaborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    plannedLaborCosts = plannedLaborCosts.add((BigDecimal) laborHourlyCost.multiply(plannedLaborTimeHours),
                            numberService.getMathContext());

                    Integer laborTime = (Integer) operatonTimeComponent.getField("laborTime");
                    BigDecimal laborTimeHours = BigDecimal.valueOf(laborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    laborCosts = laborCosts.add((BigDecimal) laborHourlyCost.multiply(laborTimeHours),
                            numberService.getMathContext());
                }
            }
        }

        BigDecimal machineCostsBalance = machineCosts.subtract(plannedMachineCosts, numberService.getMathContext());
        BigDecimal laborCostsBalance = laborCosts.subtract(plannedLaborCosts, numberService.getMathContext());

        productionBalance.setField("plannedMachineCosts", numberService.setScale(plannedMachineCosts));
        productionBalance.setField("machineCosts", numberService.setScale(machineCosts));
        productionBalance.setField("machineCostsBalance", numberService.setScale(machineCostsBalance));

        productionBalance.setField("plannedLaborCosts", numberService.setScale(plannedLaborCosts));
        productionBalance.setField("laborCosts", numberService.setScale(laborCosts));
        productionBalance.setField("laborCostsBalance", numberService.setScale(laborCostsBalance));

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void sumarizeCostValues(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }
        BigDecimal registeredTotalTechnicalProductionCosts = BigDecimal.ZERO;

        registeredTotalTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.add(
                (BigDecimal) productionBalance.getField("componentsCosts"), numberService.getMathContext());
        registeredTotalTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.add(
                (BigDecimal) productionBalance.getField("machineCosts"), numberService.getMathContext());
        registeredTotalTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.add(
                (BigDecimal) productionBalance.getField("laborCosts"), numberService.getMathContext());

        BigDecimal registeredTotalTechnicalProductionCostPerUnit =

        registeredTotalTechnicalProductionCosts.divide((BigDecimal) productionBalance.getField("quantity"),
                numberService.getMathContext());

        BigDecimal balanceTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.subtract(
                (BigDecimal) productionBalance.getField("totalTechnicalProductionCosts"), numberService.getMathContext());
        BigDecimal balanceTechnicalProductionCostPerUnit = registeredTotalTechnicalProductionCostPerUnit.subtract(
                (BigDecimal) productionBalance.getField("totalTechnicalProductionCostPerUnit"), numberService.getMathContext());

        productionBalance.setField("registeredTotalTechnicalProductionCosts",
                numberService.setScale(registeredTotalTechnicalProductionCosts));
        productionBalance.setField("registeredTotalTechnicalProductionCostPerUnit",
                numberService.setScale(registeredTotalTechnicalProductionCostPerUnit));
        productionBalance.setField("balanceTechnicalProductionCosts", numberService.setScale(balanceTechnicalProductionCosts));
        productionBalance.setField("balanceTechnicalProductionCostPerUnit",
                numberService.setScale(balanceTechnicalProductionCostPerUnit));

        productionBalance.getDataDefinition().save(productionBalance);
    }
}
