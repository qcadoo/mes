package com.qcadoo.mes.materialRequirements.api;

import java.math.BigDecimal;
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
     * Return map of products with it's quantity for MaterialRequirement entity.
     * 
     * @param entity
     *            MaterialRequirement entity
     * @return map of products with it quantity
     */
    Map<Entity, BigDecimal> prepareTechnologySeries(final Entity entity);

}
