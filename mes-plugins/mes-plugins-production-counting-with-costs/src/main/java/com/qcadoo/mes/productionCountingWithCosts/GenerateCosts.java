package com.qcadoo.mes.productionCountingWithCosts;

import java.util.Observable;
import java.util.Observer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForOperation.OperationsCostCalculationService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class GenerateCosts implements Observer {

    @Autowired
    private OperationsCostCalculationService operationsCostCalculationService;

    @Override
    public void update(Observable arg0, Object arg1) {
        Entity balance = (Entity) arg1;

        Entity order = balance.getBelongsToField("order");
        Entity technology = order.getBelongsToField("technology");
        EntityTree technologyTree = technology.getTreeField("operationComponents");

        operationsCostCalculationService.createTechnologyInstanceForCalculation(technologyTree, balance);
        balance.getDataDefinition().save(balance);
    }
}
