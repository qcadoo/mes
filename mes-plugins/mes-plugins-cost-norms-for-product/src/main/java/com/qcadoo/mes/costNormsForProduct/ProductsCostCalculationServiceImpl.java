package com.qcadoo.mes.costNormsForProduct;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.*;
import static java.math.BigDecimal.ROUND_UP;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;

@Service
public class ProductsCostCalculationServiceImpl implements ProductsCostCalculationService {

    public void calculateProductsCost(final Entity costCalculation) {
        checkArgument(costCalculation != null);
        BigDecimal quantity = getBigDecimal(costCalculation.getField("quantity"));
        BigDecimal result = BigDecimal.ZERO;
        EntityTree technologyOperationComponents = costCalculation.getBelongsToField("technology").getTreeField("operationComponents");
        ProductsCostCalculationConstants mode = getProductModeFromField(costCalculation.getField("calculateMaterialCostsMode"));

        checkArgument(quantity != null && quantity != BigDecimal.ZERO, "quantity is  null");
        checkArgument(technologyOperationComponents != null, "operationComponents is null!");
        checkArgument(mode != null, "mode is null!");

        for (Entity operationComponent : technologyOperationComponents) {
            EntityList inputProducts = operationComponent.getHasManyField("operationProductInComponents");
            for (Entity inputProduct : inputProducts) {
                BigDecimal quantityOfInputProducts = getBigDecimal(inputProduct.getField("quantity"));
                Entity product = inputProduct.getBelongsToField("product");
                BigDecimal cost = getBigDecimal(product.getField(mode.getStrValue()));
                BigDecimal costForNumber = getBigDecimal(product.getField("costForNumber"));
                BigDecimal costPerUnit = cost.divide(costForNumber, 3);

                result = result.add(costPerUnit.multiply(quantityOfInputProducts));
            }
        }
        result = result.multiply(quantity);
        costCalculation.setField("totalMaterialCosts", result.setScale(3, ROUND_UP));
    }

    private ProductsCostCalculationConstants getProductModeFromField(final Object value) {
        String strValue = value.toString();
        if ("01nominal".equals(strValue)) {
            return NOMINAL;
        }
        if ("02average".equals(strValue)) {
            return AVERAGE;
        }
        if ("03lastPurchase".equals(strValue)) {
            return LASTPURCHASE;
        }
        return ProductsCostCalculationConstants.valueOf(strValue);
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }
}
