package com.qcadoo.mes.technologies.tree;

import java.util.Map;
import java.util.Set;

import com.qcadoo.model.api.EntityTree;

/**
 * Validation service for technology operation's tree.
 * 
 * @since 1.1.6
 */
public interface TechnologyTreeValidationService {

    /**
     * @param technologyTree
     *            tree structure of operation to be validates
     * @return {@link Map} of parent node number mapped to {@link Set} of node numbers of children operations which produce more
     *         than one parent's input products.
     */
    Map<String, Set<String>> checkConsumingManyProductsFromOneSubOp(EntityTree technologyTree);

}
