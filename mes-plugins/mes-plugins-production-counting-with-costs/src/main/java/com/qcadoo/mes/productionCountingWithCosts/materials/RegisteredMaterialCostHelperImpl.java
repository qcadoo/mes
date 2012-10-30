package com.qcadoo.mes.productionCountingWithCosts.materials;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.mes.productionCountingWithCosts.util.DecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public final class RegisteredMaterialCostHelperImpl implements RegisteredMaterialCostHelper {

    @Autowired
    private NumberService numberService;

    @Override
    public void countRegisteredMaterialMarginValue(final Entity productionBalance) {
        final BigDecimal materialCostMargin = DecimalUtils.nullToZero(productionBalance
                .getDecimalField(ProductionBalanceFieldsPCWC.MATERIAL_COST_MARGIN));
        final BigDecimal registeredMaterialCost = getRegisteredMaterialCost(productionBalance);
        final BigDecimal materialCostMarginFactor = DecimalUtils.toPercent(materialCostMargin, numberService.getMathContext());
        final BigDecimal registeredMaterialMarginValue = registeredMaterialCost.multiply(materialCostMarginFactor,
                numberService.getMathContext());

        productionBalance.setField(ProductionBalanceFieldsPCWC.MATERIAL_COST_MARGIN_VALUE,
                numberService.setScale(registeredMaterialMarginValue));
    }

    private BigDecimal getRegisteredMaterialCost(final Entity productionBalance) {
        return DecimalUtils.nullToZero(productionBalance.getDecimalField(ProductionBalanceFieldsPCWC.COMPONENTS_COSTS));
    }

}
