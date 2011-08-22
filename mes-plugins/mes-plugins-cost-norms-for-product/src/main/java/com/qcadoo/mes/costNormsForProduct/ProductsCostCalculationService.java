package com.qcadoo.mes.costNormsForProduct;

import java.math.BigDecimal;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.model.api.Entity;


public interface ProductsCostCalculationService {
    public BigDecimal calculateProductsCost(final Entity technology, final ProductsCostCalculationConstants mode, final BigDecimal quantity);
}
