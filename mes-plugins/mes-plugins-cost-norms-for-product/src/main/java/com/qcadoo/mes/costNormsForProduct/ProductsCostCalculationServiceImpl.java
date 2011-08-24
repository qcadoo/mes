package com.qcadoo.mes.costNormsForProduct;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;

@Service
public class ProductsCostCalculationServiceImpl implements ProductsCostCalculationService {

    public Map<String, BigDecimal> calculateProductsCost(final Entity technology, final ProductsCostCalculationConstants mode,
            final BigDecimal quantity) {

        checkArgument(technology != null, "technology is null!");
        checkArgument(quantity != null, "quantity is  null");

        BigDecimal result = new BigDecimal(0);
        Map<String, BigDecimal> results = new HashMap<String, BigDecimal>();

        if (technology != null) {
            EntityTree operationComponents = technology.getTreeField("operationComponents");
            if (!operationComponents.isEmpty()) {
                for (Entity operationComponent : operationComponents) {
                    EntityList inputProducts = operationComponent.getHasManyField("operationProductInComponents");
                    for (Entity inputProduct : inputProducts) {
                        BigDecimal quantityInputProduct = (BigDecimal) inputProduct.getField("quantity");
                        Entity product = inputProduct.getBelongsToField("product");
                        BigDecimal includeCostOfMaterial = (BigDecimal) product.getField(mode.getStrValue());
                        BigDecimal costForNumber = (BigDecimal) product.getField("costForNumber");

                        BigDecimal costPerPrice = includeCostOfMaterial.divide(costForNumber);
                        BigDecimal costQuantityFromPorduct = costPerPrice.multiply(quantityInputProduct);
                        result = result.add(costQuantityFromPorduct);

                    }
                }
                result = result.multiply(quantity);
                results.put("materialCost", result);
            }
        }
        return results;
    }
}
