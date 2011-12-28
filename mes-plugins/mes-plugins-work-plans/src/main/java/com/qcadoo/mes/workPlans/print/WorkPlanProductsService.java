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

    public Map<Entity, Map<Entity, BigDecimal>> getInProductsForOrder(Entity order, String type) {
        Map<Entity, Map<Entity, BigDecimal>> operations = new HashMap<Entity, Map<Entity, BigDecimal>>();

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
                products.put(product, new BigDecimal(1));
            }
            operations.put(operationComponent, products);
        }

        return operations;
    }
}
