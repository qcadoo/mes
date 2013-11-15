package com.qcadoo.mes.technologies.tree.builder.api;

import com.qcadoo.mes.technologies.tree.builder.TechnologyTreeBuilder;

/**
 * Abstraction for technology tree's operation node
 * 
 * @author Marcin Kubala
 * @since 1.2.1
 */
public interface TechnologyOperationComponent extends EntityWrapper {

    /**
     * Set custom field value. You should not set basic tree fields such as parent, children, in/out product components, operation
     * or entityType. The {@link TechnologyTreeBuilder} will deal with them for you.
     * 
     * @param name
     * @param value
     */
    void setField(final String name, final Object value);

}
