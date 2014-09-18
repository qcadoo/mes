package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider;

import java.util.List;

import com.google.common.base.Optional;
import com.qcadoo.model.api.Entity;

/**
 * Order's material costs data provider
 * 
 * @since 1.4
 */
public interface OrderMaterialCostsDataProvider {

    /**
     * Find order's material costs entities that match given criteria
     * 
     * @param criteria
     *            search criteria
     * @return order's material costs entities matching given criteria
     * @since 1.4
     */
    List<Entity> findAll(final OrderMaterialCostsCriteria criteria);

    /**
     * Find first order's material costs entity that match given criteria
     *
     * @param criteria
     *            search criteria
     * @return order's material costs entity matching given criteria
     * @since 1.4
     */
    Optional<Entity> find(final OrderMaterialCostsCriteria criteria);

    /**
     * Find order's material costs entity that belongs to given order and product
     *
     * @param orderId
     *            id of an order
     * @param productId
     *            id of a product
     * @return order's material costs entity belonging to given order and product
     * @since 1.4
     */
    Optional<Entity> find(final Long orderId, final Long productId);
}
