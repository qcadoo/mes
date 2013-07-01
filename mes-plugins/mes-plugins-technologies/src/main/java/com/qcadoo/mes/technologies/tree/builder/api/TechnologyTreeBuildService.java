package com.qcadoo.mes.technologies.tree.builder.api;

import com.qcadoo.model.api.EntityTree;

/**
 * Technology tree build service
 * 
 * @author Marcin Kubala
 * @since 1.2.1
 */
public interface TechnologyTreeBuildService {

    /**
     * Build entity tree (without persisting) from given root operation (of arbitrary type) using given technology tree adapter
     * 
     * @param <T>
     *            operation component type
     * @param <P>
     *            product type
     * @param from
     *            object representing the root of technology tree
     * @param adapter
     *            technology tree adapter
     * @return an (detached, not persisted) entity tree
     */
    <T, P> EntityTree build(final T from, final TechnologyTreeAdapter<T, P> adapter);

}
