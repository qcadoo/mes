package com.qcadoo.mes.productionCountingWithCosts.hooks;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionBalanceHooksPCWC {

    public void clearFieldsOfSummaryCosts(final DataDefinition dataDefinition, final Entity entity) {
        for (String reference : Arrays.asList("registeredTotalTechnicalProductionCosts",
                "registeredTotalTechnicalProductionCostPerUnit", "totalTechnicalProductionCosts",
                "totalTechnicalProductionCostPerUnit", "balanceTechnicalProductionCosts",
                "balanceTechnicalProductionCostPerUnit", "productionCostMarginValue", "materialCostMarginValue",
                "additionalOverheadValue", "totalOverhead", "totalCosts", "totalCostPerUnit")) {
            entity.setField(reference, null);
        }
    }
}
