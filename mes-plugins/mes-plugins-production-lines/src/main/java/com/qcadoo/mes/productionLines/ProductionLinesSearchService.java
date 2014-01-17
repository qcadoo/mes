package com.qcadoo.mes.productionLines;

import java.util.Set;

/**
 * Production lines' search service.
 * 
 * @since 1.2.1
 */
public interface ProductionLinesSearchService {

    /**
     * Get ids for all production lines
     * 
     * @return set of production line ids
     */
    Set<Long> findAllLines();

    /**
     * Get ids for production lines which support given technology with <b>or support all technologies</b>.
     * 
     * @param technologyId
     *            technology's id
     * @return set of production line ids
     */
    Set<Long> findLinesSupportingTechnology(final Long technologyId);

    /**
     * Get ids for production lines which support given technology group <b>or support all technologies</b>.
     * 
     * @param technologyGroupId
     *            technology group's id
     * @return set of production line ids
     */
    Set<Long> findLinesSupportingTechnologyGroup(final Long technologyGroupId);

}
