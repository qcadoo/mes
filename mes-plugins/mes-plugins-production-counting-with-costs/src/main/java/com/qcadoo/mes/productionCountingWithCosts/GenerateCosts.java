package com.qcadoo.mes.productionCountingWithCosts;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.CostCalculationService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class GenerateCosts implements Observer {

    @Autowired
    private NumberService numberService;

    @Autowired
    private CostCalculationService costCalculationService;

    @Override
    public void update(Observable arg0, Object arg1) {
        Entity balance = (Entity) arg1;

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
    }
}
