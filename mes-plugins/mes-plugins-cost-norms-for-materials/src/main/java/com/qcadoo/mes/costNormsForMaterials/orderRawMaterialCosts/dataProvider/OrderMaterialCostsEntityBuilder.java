package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider;

import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithCosts;
import com.qcadoo.model.api.Entity;

/**
 * Builder for entities representing raw material costs in scope of given production order.
 * 
 * @since 1.4
 */
interface OrderMaterialCostsEntityBuilder {

    /**
     * Create a new, not persisted entity representing raw material costs in scope of given production order.
     * 
     * Created entity will be belonging to a given production order.
     * 
     * @param order
     *            order entity to which new material costs entity will be belonging to.
     * @param productWithCosts
     *            product and costs information provider
     * @return new material costs entity
     * 
     * @since 1.4
     */
    Entity create(final Entity order, final ProductWithCosts productWithCosts);

}
