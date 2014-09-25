package com.qcadoo.mes.technologies.tree.dataProvider;

import java.util.List;

import com.qcadoo.model.api.Entity;

/**
 * Data provider for raw materials - input product components, which don't come from any sub-operation in given technology.
 * 
 * @since 1.4
 */
public interface TechnologyRawInputProductComponentsDataProvider {

    /**
     * Find all input raw material components (technology operation input product components) from given technology, that match
     * given criteria.
     * 
     * @param criteria
     *            additional criteria, projection, search orders and so on
     * @return all matching input raw materials.
     * 
     * @since 1.4
     */
    List<Entity> findAll(final TechnologyRawInputProductComponentsCriteria criteria);

}
