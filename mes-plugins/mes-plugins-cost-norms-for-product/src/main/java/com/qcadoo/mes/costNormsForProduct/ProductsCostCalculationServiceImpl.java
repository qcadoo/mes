package com.qcadoo.mes.costNormsForProduct;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ProductsCostCalculationServiceImpl implements ProductsCostCalculationService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    public BigDecimal calculateProductsCost(final Entity technology, final ProductsCostCalculationConstants mode,
            final BigDecimal quantity) {
        checkArgument(technology != null, "technology is null!");
        checkArgument(quantity != null, "quantity is  null");

        // Entity productInComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
        // TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).(technology.getId());

        if (mode.equals(ProductsCostCalculationConstants.AVERAGE)) {

            if (quantity.compareTo(BigDecimal.valueOf(3)) == 0) {
                return BigDecimal.valueOf(30);
            }

        }
        return quantity;

    }
}
