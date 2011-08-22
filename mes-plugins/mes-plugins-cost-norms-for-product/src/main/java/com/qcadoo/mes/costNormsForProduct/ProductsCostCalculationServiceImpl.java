package com.qcadoo.mes.costNormsForProduct;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.model.api.Entity;

@Service
public class ProductsCostCalculationServiceImpl implements ProductsCostCalculationService {

    public BigDecimal calculateProductsCost(final Entity technology, final ProductsCostCalculationConstants mode,
            final BigDecimal quantity) {
        checkArgument(technology != null, "technology is null!");

        if (mode.equals(ProductsCostCalculationConstants.AVERAGE)) {
            return BigDecimal.valueOf(45);
        } else if (mode.equals(ProductsCostCalculationConstants.NOMINAL)) {
            return BigDecimal.valueOf(30);
        } else if (mode.equals(ProductsCostCalculationConstants.LAST_PURCHASE)) {
            return BigDecimal.valueOf(60);
        } else {
            return null;
        }

    }

}
