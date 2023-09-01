package com.qcadoo.mes.productFlowThruDivision.realProductionCost;

import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RealProductionCostService {

    @Autowired
    private RealProductionCostDataProvider realProductionCostDataProvider;

    @Autowired
    private NumberService numberService;

    public BigDecimal calculateRealProductionCost(final Entity order) {
        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (TypeOfProductionRecording.BASIC.getStringValue().equals(typeOfProductionRecording)) {
            return BigDecimal.ZERO;
        }
        return RealProductionCost.service(realProductionCostDataProvider, order).appendMaterialsCosts()
                .appendMaterialsCostsMargin().appendLaborCosts().appendLaborCostsMargin().appendAdditionalDirectCosts()
                .appendAveragePriceSubcontractor().calculate();
    }

    public void calculateRealProductionCost(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference("order");
        Entity order = form.getEntity();

        if (order.getId() == null) {
            return;
        }

        Entity orderDB = order.getDataDefinition().get(order.getId());

        BigDecimal realProductionCost = calculateRealProductionCost(orderDB);
        view.addMessage("productFlowThruDivision.realProductionCost.calculationResult", ComponentState.MessageType.SUCCESS,
                false, numberService.formatWithMinimumFractionDigits(numberService.setScaleWithDefaultMathContext(realProductionCost, 2), 2));

    }
}
