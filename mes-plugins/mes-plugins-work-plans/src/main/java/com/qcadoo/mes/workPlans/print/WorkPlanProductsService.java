package com.qcadoo.mes.workPlans.print;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class WorkPlanProductsService {

    public Map<Entity, Map<Entity, BigDecimal>> getInProductsPerOperation(final Entity order) {
        Map<Entity, Map<Entity, BigDecimal>> operations = new HashMap<Entity, Map<Entity, BigDecimal>>();

        BigDecimal plannedQty = (BigDecimal) order.getField("plannedQuantity");

        Entity technology = order.getBelongsToField("technology");

        if (technology == null) {
            return operations;
        }

        EntityTree operationComponents = technology.getTreeField("operationComponents");

        for (Entity operationComponent : operationComponents) {
            Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();

            List<Entity> operationProdInComps = operationComponent.getHasManyField("operationProductInComponents");
            for (Entity operationProdInComp : operationProdInComps) {
                Entity product = operationProdInComp.getBelongsToField("product");
                BigDecimal neededQty = (BigDecimal) operationProdInComp.getField("quantity");

                if ("01perProductOut".equals(technology.getStringField("componentQuantityAlgorithm"))) {
                    neededQty = neededQty.multiply(plannedQty);
                }

                products.put(product, neededQty);
            }
            operations.put(operationComponent, products);
        }

        return operations;
    }
}
