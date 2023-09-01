package com.qcadoo.mes.productFlowThruDivision.realProductionCost;

import java.math.BigDecimal;

import com.qcadoo.mes.costCalculation.constants.OrderAdditionalDirectCostFields;
import com.qcadoo.mes.costCalculation.constants.OrderFieldsCC;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;

public class RealProductionCost {

    private BigDecimal value = BigDecimal.ZERO;

    private Entity order;

    private RealProductionCostDataProvider dataProvider;

    private RealProductionCost() {

    }

    public static IMaterialsCosts service(RealProductionCostDataProvider dataProvider, final Entity order) {
        return new RealProductionCost.Builder(dataProvider, order);
    }

    public interface IMaterialsCosts {

        IMaterialsCostsMargin appendMaterialsCosts();
    }


    public interface IMaterialsCostsMargin {

        ILaborCosts appendMaterialsCostsMargin();
    }

    public interface ILaborCosts {

        ILaborCostsMargin appendLaborCosts();
    }

    public interface ILaborCostsMargin {

        IAdditionalDirectCosts appendLaborCostsMargin();
    }

    public interface IAdditionalDirectCosts {

        IAaveragePriceSubcontractor appendAdditionalDirectCosts();
    }

    public interface IAaveragePriceSubcontractor {

        ICalculate appendAveragePriceSubcontractor();
    }

    public interface ICalculate {

        BigDecimal calculate();
    }

    private static class Builder implements IMaterialsCosts, IMaterialsCostsMargin, ILaborCosts, ILaborCostsMargin,
            IAdditionalDirectCosts, IAaveragePriceSubcontractor, ICalculate {

        private RealProductionCost realProductionCost = new RealProductionCost();

        private Entity parameter;

        private BigDecimal laborCosts = BigDecimal.ZERO;

        private BigDecimal materialsCosts = BigDecimal.ZERO;

        public Builder(RealProductionCostDataProvider dataProvider, final Entity order) {
            realProductionCost.dataProvider = dataProvider;
            realProductionCost.order = order;
            parameter = dataProvider.getParameter();
        }

        @Override
        public BigDecimal calculate() {
            BigDecimal doneQty = realProductionCost.order.getDecimalField(OrderFields.DONE_QUANTITY);
            if (BigDecimal.ZERO.compareTo(BigDecimalUtils.convertNullToZero(doneQty)) == 0) {
                realProductionCost.value = BigDecimal.ZERO;
            } else {
                realProductionCost.value = realProductionCost.value.divide(doneQty,
                        realProductionCost.dataProvider.getMathContext());
            }
            realProductionCost.value = realProductionCost.value.add(realProductionCost.value.multiply(BigDecimalUtils
                    .convertNullToZero(parameter.getDecimalField(ParameterFieldsPC.REGISTRATION_PRICE_OVERHEAD_PB)).divide(
                            new BigDecimal(100L), realProductionCost.dataProvider.getMathContext())));
            return realProductionCost.dataProvider.setScale(realProductionCost.value);
        }

        @Override
        public IMaterialsCostsMargin appendMaterialsCosts() {
            materialsCosts = realProductionCost.dataProvider.getMaterialsCosts(realProductionCost.order);
            realProductionCost.value = realProductionCost.value.add(materialsCosts);
            return this;
        }

        @Override
        public ILaborCostsMargin appendLaborCosts() {
            laborCosts = realProductionCost.dataProvider.getLaborCost(realProductionCost.order);
            realProductionCost.value = realProductionCost.value.add(laborCosts);
            return this;
        }

        @Override
        public IAdditionalDirectCosts appendLaborCostsMargin() {
            BigDecimal margin = parameter.getDecimalField(ParameterFieldsPC.PRODUCTION_COST_MARGIN_PB);
            BigDecimal laborCostsMargin = realProductionCost.dataProvider.getLaborCostMargin(laborCosts, margin);
            realProductionCost.value = realProductionCost.value.add(laborCostsMargin);
            return this;
        }

        @Override
        public ILaborCosts appendMaterialsCostsMargin() {
            BigDecimal margin = parameter.getDecimalField(ParameterFieldsPC.MATERIAL_COST_MARGIN_PB);
            BigDecimal materialCostsMargin = realProductionCost.dataProvider.getMaterialCostMargin(materialsCosts, margin);
            realProductionCost.value = realProductionCost.value.add(materialCostsMargin);
            return this;
        }

        @Override
        public IAaveragePriceSubcontractor appendAdditionalDirectCosts() {
            realProductionCost.value = realProductionCost.value.add(realProductionCost.order
                    .getHasManyField(OrderFieldsCC.ORDER_ADDITIONAL_DIRECT_COSTS).stream()
                    .filter(e -> e.getDecimalField(OrderAdditionalDirectCostFields.ACTUAL_COST) != null)
                    .map(e -> e.getDecimalField(OrderAdditionalDirectCostFields.ACTUAL_COST)).reduce(BigDecimal.ZERO, BigDecimal::add));

            return this;
        }

        @Override
        public ICalculate appendAveragePriceSubcontractor() {
            BigDecimal priceSubcontractor = realProductionCost.dataProvider.getPriceSubcontractor(realProductionCost.order);
            realProductionCost.value = realProductionCost.value.add(priceSubcontractor);
            return this;
        }
    }
}
