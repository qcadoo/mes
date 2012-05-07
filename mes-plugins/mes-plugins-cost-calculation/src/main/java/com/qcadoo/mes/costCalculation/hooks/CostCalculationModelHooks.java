package com.qcadoo.mes.costCalculation.hooks;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CostCalculationModelHooks {

    public void clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("fileName", null);
        entity.setField("generated", false);
        entity.setField("date", null);

    }

    public void clearGeneratedCosts(final DataDefinition dataDefinition, final Entity entity) {
        for (String reference : Arrays.asList("totalMaterialCosts", "totalMachineHourlyCosts", "totalLaborHourlyCosts",
                "totalPieceworkCosts", "totalTechnicalProductionCostsLabel", "totalTechnicalProductionCosts",
                "productionCostMarginValue", "materialCostMarginValue", "additionalOverheadValue", "totalOverhead", "totalCosts",
                "totalCostPerUnit")) {
            entity.setField(reference, BigDecimal.ZERO);
        }
    }
}
