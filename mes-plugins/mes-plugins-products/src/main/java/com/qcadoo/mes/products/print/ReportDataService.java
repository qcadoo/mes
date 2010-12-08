package com.qcadoo.mes.products.print;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.ProxyEntity;

@Service
public class ReportDataService {

    private static final String MATERIAL_COMPONENT = "component";

    public final Map<ProxyEntity, BigDecimal> getTechnologySeries(final Entity entity, final List<Entity> orders) {
        Map<ProxyEntity, BigDecimal> products = new HashMap<ProxyEntity, BigDecimal>();
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity technology = (Entity) order.getField("technology");
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            if (technology != null && plannedQuantity != null && plannedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                List<Entity> operationComponents = technology.getHasManyField("operationComponents");
                for (Entity operationComponent : operationComponents) {
                    ProxyEntity product = (ProxyEntity) operationComponent.getField("product");
                    if (!(Boolean) entity.getField("onlyComponents")
                            || MATERIAL_COMPONENT.equals(product.getField("typeOfMaterial"))) {
                        if (products.containsKey(product)) {
                            BigDecimal quantity = products.get(product);
                            quantity = ((BigDecimal) operationComponent.getField("quantity")).multiply(plannedQuantity).add(
                                    quantity);
                            products.put(product, quantity);
                        } else {
                            products.put(product,
                                    ((BigDecimal) operationComponent.getField("quantity")).multiply(plannedQuantity));
                        }
                    }
                }
            }
        }
        return products;
    }
}
