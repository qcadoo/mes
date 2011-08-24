package com.qcadoo.mes.costNormsForProduct;

import java.math.BigDecimal;
import java.util.Map;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.model.api.Entity;


public interface ProductsCostCalculationService {
    public Map<String, BigDecimal> calculateProductsCost(final Entity technology, final ProductsCostCalculationConstants mode, final BigDecimal quantity);
}
