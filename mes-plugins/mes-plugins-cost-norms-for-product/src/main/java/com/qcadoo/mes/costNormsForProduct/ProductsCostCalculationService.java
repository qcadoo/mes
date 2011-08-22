package com.qcadoo.mes.costNormsForProduct;

import java.math.BigDecimal;

import com.qcadoo.model.api.Entity;


public interface ProductsCostCalculationService {
    public BigDecimal calculateProductsCost(final String mode, final Entity technology);
}
