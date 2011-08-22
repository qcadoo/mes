package com.qcadoo.mes.costNormsForProduct;

import static com.google.common.base.Preconditions.checkArgument;
import java.math.BigDecimal;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.model.api.Entity;

public class ProductsCostCalculationServiceImpl implements ProductsCostCalculationService {

    public BigDecimal calculateProductsCost(final String mode, final Entity technology) {
        checkArgument(technology != null, "technology is null!");
        checkArgument(
                ProductsCostCalculationConstants.AVERAGE.equals(mode) && ProductsCostCalculationConstants.LAST_PURCHASE.equals(mode)
                        && ProductsCostCalculationConstants.NOMINAL.equals(mode), "Incorrect calculation mode value!");
        return null;
    }

}
