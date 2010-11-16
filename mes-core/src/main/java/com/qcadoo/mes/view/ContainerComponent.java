/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view;

import java.util.Map;

/**
 * Component which can holds other components - windows, forms, etc.
 * 
 * @param <T>
 *            class of the component's value
 */
public interface ContainerComponent<T> extends Component<T> {

    /**
     * Return all child components.
     * 
     * @return child component
     */
    Map<String, Component<?>> getComponents();

}
