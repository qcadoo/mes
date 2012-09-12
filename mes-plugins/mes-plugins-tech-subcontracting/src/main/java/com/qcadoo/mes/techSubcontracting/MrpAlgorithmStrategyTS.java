package com.qcadoo.mes.techSubcontracting;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.MrpAlgorithmStrategy;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;

@Service("mrpAlgorithmStrategyTS")
public class MrpAlgorithmStrategyTS implements MrpAlgorithmStrategy {

    public boolean isApplicableFor(MrpAlgorithm algorithm) {
        return MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS.equals(algorithm);
    }

    public Map<Entity, BigDecimal> perform(Map<Entity, BigDecimal> productComponentQuantities, Set<Entity> nonComponents,
            MrpAlgorithm algorithm, String type) {
        Map<Entity, BigDecimal> productQuantities = new HashMap<Entity, BigDecimal>();

        for (Entry<Entity, BigDecimal> productComponentQuantity : productComponentQuantities.entrySet()) {
            if (type.equals(productComponentQuantity.getKey().getDataDefinition().getName())) {
                if (nonComponents.contains(productComponentQuantity.getKey())) {
                    continue;
                }
                addProductQuantitiesToList(productComponentQuantity, productQuantities);
            } else {
                Entity operation = productComponentQuantity.getKey().getBelongsToField("operationComponent");
                if (operation.getBooleanField("isSubcontracting")) {
                    addProductQuantitiesToList(productComponentQuantity, productQuantities);
                }
            }
        }
        return productQuantities;
    }

    private void addProductQuantitiesToList(final Entry<Entity, BigDecimal> productComponentQuantity,
            final Map<Entity, BigDecimal> productQuantities) {
        Entity product = productComponentQuantity.getKey().getBelongsToField("product");
        BigDecimal newQty = productComponentQuantity.getValue();

        BigDecimal oldQty = productQuantities.get(product);
        if (oldQty != null) {
            newQty = newQty.add(oldQty);

        }
        productQuantities.put(product, newQty);
    }

}
