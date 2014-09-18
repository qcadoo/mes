package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts;

import java.util.List;

import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithCosts;
import com.qcadoo.model.api.Entity;

public interface OrderMaterialsCostDataGenerator {

    /**
     * Find products with costs, that currently aren't covered in existing material costs structure for given order.
     * 
     * @param order
     *            order to be scanned
     * @return list of products with their costs.
     * 
     * @since 1.4
     */
    List<Entity> generateUpdatedMaterialsListFor(final Entity order);

}
