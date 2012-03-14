package com.qcadoo.mes.productionCountingWithCosts;

import static com.google.common.collect.Lists.newLinkedList;
import static com.qcadoo.mes.costCalculation.constants.CostCalculationFields.CALCULATION_OPERATION_COMPONENTS;
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
import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceService;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionCountingWithCostsConstants;
import com.qcadoo.mes.productionCountingWithCosts.pdf.ProductionBalanceWithCostsPdfService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.TreeNumberingService;
import com.qcadoo.report.api.SortUtil;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class GenerateProductionBalanceWithCosts implements Observer {

    private static final String L_DURATION = "duration";

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
    private TimeConverterService timeConverterService;

    @Autowired
    private TreeNumberingService treeNumberingService;

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

        String calculateMaterialCostsMode = productionBalance.getStringField("calculateMaterialCostsMode");

        String costMode = "";
        if (CalculateMaterialCostsMode.NOMINAL.getStringValue().equals(calculateMaterialCostsMode)) {
            costMode = CalculateMaterialCostsMode.NOMINAL.getStringValue();
        } else if (CalculateMaterialCostsMode.AVERAGE.getStringValue().equals(calculateMaterialCostsMode)) {
            costMode = CalculateMaterialCostsMode.AVERAGE.getStringValue();
        } else if (CalculateMaterialCostsMode.LAST_PURCHASE.getStringValue().equals(calculateMaterialCostsMode)) {
            costMode = CalculateMaterialCostsMode.LAST_PURCHASE.getStringValue();
        } else if (CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue().equals(calculateMaterialCostsMode)) {
            costMode = CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue();
        }

        BigDecimal componentsCostsBalance = BigDecimal.ZERO;

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
            BigDecimal quantity = BigDecimal.TEN;

            Entity orderOperationProductInComponent = dataDefinitionService.get(
                    ProductionCountingWithCostsConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingWithCostsConstants.MODEL_ORDER_OPERATION_PRODUCT_IN_COMPONENT).create();

            BigDecimal nominalCost = (BigDecimal) product.getField("nominalCost");
            BigDecimal costForNumber = (BigDecimal) product.getField("costForNumber");

            BigDecimal totalCostPerUnit = nominalCost.divide(costForNumber, numberService.getMathContext());

            BigDecimal plannedCost = quantity.multiply(totalCostPerUnit, numberService.getMathContext());
            BigDecimal registeredCost = BigDecimal.TEN;
            BigDecimal balance = registeredCost.subtract(plannedCost, numberService.getMathContext());

            orderOperationProductInComponent.setField(ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, productionBalance);
            orderOperationProductInComponent.setField("product", product);
            orderOperationProductInComponent.setField("plannedCost", plannedCost);
            orderOperationProductInComponent.setField("registeredCost", registeredCost);
            orderOperationProductInComponent.setField("balance", balance);

            orderOperationProductInComponents.add(orderOperationProductInComponent);
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

        List<Entity> calculationOperationComponents = newLinkedList(productionBalance
                .getTreeField(CALCULATION_OPERATION_COMPONENTS));

        Collections.sort(calculationOperationComponents, treeNumberingService.getTreeNodesNumberComparator());

        if (calculationOperationComponents != null && !calculationOperationComponents.isEmpty()) {
            for (Entity calculationOperationComponent : calculationOperationComponents) {
                Entity operationCostComponent = dataDefinitionService.get(ProductionCountingWithCostsConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingWithCostsConstants.MODEL_OPERATION_COST_COMPONENT).create();

                Integer durationMs = (Integer) calculationOperationComponent.getField(L_DURATION);
                BigDecimal milisecondsInHour = BigDecimal.valueOf(3600);
                BigDecimal durationHours = BigDecimal.valueOf(durationMs).divide(milisecondsInHour,
                        numberService.getMathContext());
                BigDecimal machineUtilization = (BigDecimal) calculationOperationComponent.getField("machineUtilization");
                BigDecimal machineHourlyCost = (BigDecimal) calculationOperationComponent.getField("machineHourlyCost");

                BigDecimal plannedMachineCosts = (BigDecimal) machineUtilization.multiply(machineHourlyCost).multiply(
                        durationHours);
                BigDecimal machineCosts = BigDecimal.ZERO;
                BigDecimal machineCostsBalance = machineCosts.subtract(plannedMachineCosts, numberService.getMathContext());

                BigDecimal laborUtilization = (BigDecimal) calculationOperationComponent.getField("laborUtilization");
                BigDecimal laborHourlyCost = (BigDecimal) calculationOperationComponent.getField("laborHourlyCost");

                BigDecimal plannedLaborCosts = (BigDecimal) laborUtilization.multiply(laborHourlyCost).multiply(durationHours);
                BigDecimal laborCosts = BigDecimal.ZERO;
                BigDecimal laborCostsBalance = laborCosts.subtract(plannedLaborCosts, numberService.getMathContext());

                operationCostComponent.setField("orderOperationComponent",
                        calculationOperationComponent.getField("technologyOperationComponent"));

                operationCostComponent.setField("plannedMachineCosts", plannedMachineCosts);
                operationCostComponent.setField("machineCosts", machineCosts);
                operationCostComponent.setField("machineCostsBalance", machineCostsBalance);

                operationCostComponent.setField("plannedLaborCosts", plannedLaborCosts);
                operationCostComponent.setField("laborCosts", laborCosts);
                operationCostComponent.setField("laborCostsBalance", laborCostsBalance);

                operationCostComponents.add(operationCostComponent);
            }
        }

        productionBalance.setField(ProductionBalanceFieldsPCWC.OPERATION_COST_COMPONENTS, operationCostComponents);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillCostValues(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }
    }
}
