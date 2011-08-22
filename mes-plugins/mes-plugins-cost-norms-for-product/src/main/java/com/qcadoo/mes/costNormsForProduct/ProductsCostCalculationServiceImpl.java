package com.qcadoo.mes.costNormsForProduct;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.model.api.Entity;

public class ProductsCostCalculationServiceImpl implements ProductsCostCalculationService {

    public BigDecimal calculateProductsCost(final ProductsCostCalculationConstants mode, final Entity technology) {

        checkArgument(technology != null, "technology is null!");

        return BigDecimal.valueOf(45);
    }

}
