package com.qcadoo.mes.technologies.tree.builder.api;

import java.util.Collection;

import com.qcadoo.mes.technologies.tree.builder.TechnologyTreeBuilder;
import com.qcadoo.model.api.Entity;

/**
 * Adapter interface used by {@link TechnologyTreeBuilder}
 * 
 * @author Marcin Kubala
 * @since 1.2.1
 * 
 * @param <T>
 *            operation component type
 * @param <P>
 *            product type
 */
public interface TechnologyTreeAdapter<T, P> {

    /**
     * Set operation component's custom fields. You shouldn't define basic fields such as operation, entityType, input/output
     * products, children or parent. The technology tree building framework will do it for you.
     * 
     * @param toc
     *            technology operation component to fill.
     * @param from
     *            corresponding object from your data representation.
     */
    void setOpCompCustomFields(final TechnologyOperationComponent toc, final T from);

    /**
     * Set operation product component's custom fields. You shouldn't define basic fields such as product, quantity or operation
     * component. The technology tree building framework will do it for you.
     * 
     * @param opc
     *            operation product component to fill.
     * @param from
     *            corresponding object for operation product component from your data representation.
     */
    void setOpProductCompCustomFields(final OperationProductComponent opc, final P from);

    /**
     * Returns collection of input products assigned to given technology operation's corresponding object.
     * 
     * @param from
     *            corresponding object for technology operation component from your data representation.
     * @return collection of input products wrapped in {@link ItemWithQuantity}.
     */
    Collection<ItemWithQuantity<P>> extractInputProducts(final T from);

    /**
     * Returns collection of output products assigned to given technology operation's corresponding object.
     * 
     * @param from
     *            corresponding object for technology operation component from your data representation.
     * @return collection of output products wrapped in {@link ItemWithQuantity}.
     */
    Collection<ItemWithQuantity<P>> extractOutputProducts(final T from);

    /**
     * Returns an Iterable structure containing sub-operations from given object.
     * 
     * @param from
     *            corresponding object for technology operation component from your data representation.
     * @return Iterable structure containing sub-operations from given object.
     */
    Iterable<T> extractSubOperations(final T from);

    /**
     * Produce a basic product (#basic_product) {@link Entity} from given operation product component.
     * 
     * @param from
     *            corresponding object for operation product component from your data representation.
     * @return basic product {@link Entity}
     */
    Entity buildProductEntity(final P from);

    /**
     * Produce an operation (#technology_operation) {@link Entity} from given technology operation component.
     * 
     * @param from
     *            corresponding object for technology operation component from your data representation.
     * @return operation {@link Entity}
     */
    Entity buildOperationEntity(final T from);

}
