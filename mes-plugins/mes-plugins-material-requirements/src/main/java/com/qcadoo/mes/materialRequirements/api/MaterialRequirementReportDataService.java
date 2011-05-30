package com.qcadoo.mes.materialRequirements.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.qcadoo.model.api.Entity;

/**
 * Service for preparing data to material requirement reports.
 * 
 * @since 0.4.1
 * 
 */
public interface MaterialRequirementReportDataService {

    /**
     * Return map of products with it's quantity for orders entities.
     * 
     * @param orders
     *            list of orders
     * @param onlyComponents
     *            get only this products which have typeOfMaterial = component
     * @return map of products with it quantity
     */
    Map<Entity, BigDecimal> getQuantitiesForOrdersTechnologyProducts(final List<Entity> orders, final Boolean onlyComponents);

}
