package com.qcadoo.mes.technologies.tree.builder.api;

import java.util.Collection;

import com.qcadoo.model.api.Entity;

/**
 * TechnologyOperationComp API extensions for internal use.
 * 
 * @author Marcin Kubala
 * @since 1.2.1
 */
public interface InternalTechnologyOperationComponent extends TechnologyOperationComponent {

    /**
     * Assign operation Entity.
     * 
     * @param operation
     *            operation entity to be assigned
     * @throws IllegalArgumentException
     *             when given entity has data definition other than #technology_operation.
     */
    void setOperation(final Entity operation) throws IllegalArgumentException;

    /**
     * Add sub-operation or in other words assign child to current tree node.
     * 
     * @param technologyOperationComponent
     *            sub-operation to assing
     */
    void addSubOperation(final TechnologyOperationComponent technologyOperationComponent);

    /**
     * Add input product component.
     * 
     * @param productComponents
     */
    void addInputProducts(final Collection<OperationProductComponent> productComponents);

    /**
     * Add output product component.
     * 
     * @param productComponents
     */
    void addOutputProducts(final Collection<OperationProductComponent> productComponents);

}
