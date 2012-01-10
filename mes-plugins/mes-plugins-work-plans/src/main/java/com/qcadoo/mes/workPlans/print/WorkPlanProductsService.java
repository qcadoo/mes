package com.qcadoo.mes.workPlans.print;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class WorkPlanProductsService {

    enum ProductType {
        IN, OUT;
    }

    public Map<Entity, Map<Entity, BigDecimal>> getProductQuantities(List<Entity> orders, ProductType type) {
        Map<Entity, Map<Entity, BigDecimal>> inProductsPerOperationComponent = new HashMap<Entity, Map<Entity, BigDecimal>>();
        for (Entity order : orders) {
            BigDecimal plannedQty = (BigDecimal) order.getField("plannedQuantity");

            Entity technology = order.getBelongsToField("technology");

            if (technology == null) {
                continue;
            }

            EntityTree operationComponents = technology.getTreeField("operationComponents");

            for (Entity operationComponent : operationComponents) {
                Map<Entity, BigDecimal> productInComponents = new TreeMap<Entity, BigDecimal>(getProductComparator());

                List<Entity> operationProdComps = Collections.emptyList();
                if (type == ProductType.IN) {
                    operationProdComps = operationComponent.getHasManyField("operationProductInComponents");
                } else if (type == ProductType.OUT) {
                    operationProdComps = operationComponent.getHasManyField("operationProductOutComponents");
                }

                for (Entity operationProdComp : operationProdComps) {
                    BigDecimal neededQty = (BigDecimal) operationProdComp.getField("quantity");

                    if ("02perTechnology".equals(technology.getStringField("componentQuantityAlgorithm"))) {
                        neededQty = neededQty.multiply(plannedQty);
                    }

                    productInComponents.put(operationProdComp, neededQty);
                }
                inProductsPerOperationComponent.put(operationComponent, productInComponents);
            }
        }

        return inProductsPerOperationComponent;
    }

    private Comparator<Entity> getProductComparator() {
        return new Comparator<Entity>() {

            @Override
            public int compare(Entity o1, Entity o2) {
                Entity product1 = o1.getBelongsToField("product");
                Entity product2 = o2.getBelongsToField("product");
                return product1.getStringField("name").compareTo(product2.getStringField("name"));
            }
        };
    }
}
