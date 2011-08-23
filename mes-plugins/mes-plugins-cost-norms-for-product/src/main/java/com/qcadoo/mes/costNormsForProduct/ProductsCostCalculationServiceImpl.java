package com.qcadoo.mes.costNormsForProduct;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.model.api.Entity;

@Service
public class ProductsCostCalculationServiceImpl implements ProductsCostCalculationService {

    public Map<String, BigDecimal> calculateProductsCost(final Entity technology, final ProductsCostCalculationConstants mode,
            final BigDecimal quantity) {
        checkArgument(technology != null, "technology is null!");

        return Collections.emptyMap();
    }

}
