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
        checkArgument(technology.getTreeField("operationComponents") != null, "operation components is null");

        BigDecimal result = BigDecimal.ZERO;
        Map<String, BigDecimal> resultsMap = new HashMap<String, BigDecimal>();

        EntityTree operationComponents = technology.getTreeField("operationComponents");
        for (Entity operationComponent : operationComponents) {
            EntityList inputProducts = operationComponent.getHasManyField("operationProductInComponents");
            for (Entity inputProduct : inputProducts) {
                BigDecimal quantityOfInputProducts = (BigDecimal) inputProduct.getField("quantity");
                Entity product = inputProduct.getBelongsToField("product");
                BigDecimal cost = (BigDecimal) product.getField(mode.getStrValue());
                BigDecimal costForNumber = new BigDecimal(product.getField("costForNumber").toString());
                BigDecimal costPerUnit = cost.divide(costForNumber, 3);
                BigDecimal totalMaterialCosts = costPerUnit.multiply(quantityOfInputProducts);
                result = result.add(totalMaterialCosts);

            }
        }
        result = result.multiply(quantity);
        resultsMap.put("totalMaterialCosts", result);
        return resultsMap;
    }
}
