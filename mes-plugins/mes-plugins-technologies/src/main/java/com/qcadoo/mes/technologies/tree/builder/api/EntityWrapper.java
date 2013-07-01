package com.qcadoo.mes.technologies.tree.builder.api;

import com.qcadoo.model.api.Entity;

/**
 * This is a common interface for objects which wraps Entities
 * 
 * @author Marcin Kubala
 * @since 1.2.1
 */
public interface EntityWrapper {

    /**
     * Returns wrapped entity. Note for developers implementing this interface: please be aware of mutability and side effects.
     * Prefer returning entity.copy() instead of original one.
     * 
     * @return wrapped entity
     */
    Entity getWrappedEntity();

}
