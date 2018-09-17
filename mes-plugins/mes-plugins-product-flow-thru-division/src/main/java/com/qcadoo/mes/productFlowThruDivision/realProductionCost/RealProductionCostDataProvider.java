package com.qcadoo.mes.productFlowThruDivision.realProductionCost;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
import com.qcadoo.mes.costNormsForMaterials.constants.OrderFieldsCNFM;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;

@Service
public class RealProductionCostDataProvider {

    public static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ParameterService parameterService;

    public BigDecimal getMaterialsCosts(final Entity order) {
        List<Entity> inComps = order.getHasManyField(OrderFieldsCNFM.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS);
        return inComps.stream().map(inComp -> inComp.getDecimalField(TechnologyInstOperProductInCompFields.COST_FOR_ORDER))
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getMaterialCostMargin(final BigDecimal materialCost, final BigDecimal margin) {
        return materialCost.multiply(BigDecimalUtils.convertNullToZero(margin).divide(new BigDecimal(100l)),
                numberService.getMathContext());
    }

    public BigDecimal getLaborCost(final Entity order) {

        Entity parameters = parameterService.getParameter();
        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
            return getLaborCostForEachOperationTypeRecording(order, parameters);
        } else {
            return getLaborCostForCumulatedTypeRecording(order, parameters);
        }

    }

    public BigDecimal getLaborCostMargin(final BigDecimal laborCost, final BigDecimal margin) {
        return laborCost.multiply(BigDecimalUtils.convertNullToZero(margin).divide(new BigDecimal(100l)),
                numberService.getMathContext());
    }

    private BigDecimal getLaborCostForCumulatedTypeRecording(final Entity order, final Entity parameters) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT sum(productionTracking.machineTime) AS machineTime, ");
        query.append("sum(productionTracking.laborTime) AS laborTime ");
        query.append("FROM #productionCounting_productionTracking productionTracking ");
        query.append("LEFT JOIN productionTracking.order _order ");
        query.append("WHERE _order.id = :orderId AND productionTracking.state = '02accepted'");

        Entity costEntity = dataDefinitionService.get("productionCounting", "productionTracking").find(query.toString())
                .setLong("orderId", order.getId()).setMaxResults(1).uniqueResult();

        BigDecimal laborCostPerHour = BigDecimalUtils.convertNullToZero(parameters
                .getDecimalField(ParameterFieldsPC.AVERAGE_LABOR_HOURLY_COST_PB));
        BigDecimal machineCostPerHour = BigDecimalUtils.convertNullToZero(parameters
                .getDecimalField(ParameterFieldsPC.AVERAGE_MACHINE_HOURLY_COST_PB));
        BigDecimal machineCost = BigDecimalUtils.convertNullToZero(costEntity.getLongField("machineTime")).divide(new BigDecimal(3600),
                numberService.getMathContext()).multiply(machineCostPerHour, numberService.getMathContext());

        BigDecimal laborCost = BigDecimalUtils.convertNullToZero(costEntity.getLongField("laborTime")).divide(new BigDecimal(3600),
                numberService.getMathContext()).multiply(laborCostPerHour, numberService.getMathContext());

        return laborCost.add(machineCost, numberService.getMathContext());
    }

    private BigDecimal getLaborCostForEachOperationTypeRecording(final Entity order, final Entity parameters) {
        BigDecimal laborCost = BigDecimal.ZERO;
        BigDecimal machineCost = BigDecimal.ZERO;

        boolean fromParameters = false;
        BigDecimal machineCostPerHour = BigDecimal.ZERO;
        BigDecimal laborCostPerHour = BigDecimal.ZERO;

        StringBuilder query = new StringBuilder();
        query.append("SELECT toc.id AS tocId, toc.machineHourlyCost AS machineHourlyCost, ");
        query.append("toc.laborHourlyCost AS laborHourlyCost, sum(productionTracking.machineTime) AS machineTime, ");
        query.append("sum(productionTracking.laborTime) AS laborTime ");
        query.append("FROM #productionCounting_productionTracking productionTracking ");
        query.append("LEFT JOIN productionTracking.order _order ");
        query.append("LEFT JOIN productionTracking.technologyOperationComponent toc ");
        query.append("WHERE _order.id = :orderId AND productionTracking.state = '02accepted' ");
        query.append("GROUP BY toc.id");

        List<Entity> costs = dataDefinitionService.get("productionCounting", "productionTracking").find(query.toString())
                .setLong("orderId", order.getId()).list().getEntities();

        String sourceOfOperationCosts = parameters.getStringField(ParameterFieldsPC.SOURCE_OF_OPERATION_COSTS_PB);
        if (SourceOfOperationCosts.PARAMETERS.getStringValue().equals(sourceOfOperationCosts)) {
            fromParameters = true;
            laborCostPerHour = BigDecimalUtils.convertNullToZero(parameters.getDecimalField(ParameterFieldsPC.AVERAGE_LABOR_HOURLY_COST_PB));
            machineCostPerHour = BigDecimalUtils.convertNullToZero(
                    parameters.getDecimalField(ParameterFieldsPC.AVERAGE_MACHINE_HOURLY_COST_PB));
        }

        for (Entity cost : costs) {
            if (!fromParameters) {
                laborCostPerHour = BigDecimalUtils.convertNullToZero(cost.getDecimalField("laborHourlyCost"));
                machineCostPerHour = BigDecimalUtils.convertNullToZero(cost.getDecimalField("machineHourlyCost"));
            }
            BigDecimal machineHourlyCostPerToc = new BigDecimal(nullLongToZero(cost.getLongField("machineTime"))).divide(new BigDecimal(3600),
                    numberService.getMathContext()).multiply(machineCostPerHour, numberService.getMathContext());
            machineCost = machineCost.add(machineHourlyCostPerToc, numberService.getMathContext());

            BigDecimal laborHourlyCostPerToc = new BigDecimal(nullLongToZero(cost.getLongField("laborTime"))).divide(new BigDecimal(3600),
                    numberService.getMathContext()).multiply(laborCostPerHour, numberService.getMathContext());
            laborCost = laborCost.add(laborHourlyCostPerToc, numberService.getMathContext());

        }

        return laborCost.add(machineCost, numberService.getMathContext());
    }

    public Entity getParameter() {
        return parameterService.getParameter();
    }

    public BigDecimal setScale(BigDecimal value) {
        return numberService.setScaleWithDefaultMathContext(value);
    }

    public MathContext getMathContext() {
        return numberService.getMathContext();
    }

    private Long nullLongToZero(Long obj){
        if(Objects.isNull(obj)){
            return 0L;
        }
        return obj;
    }
}
