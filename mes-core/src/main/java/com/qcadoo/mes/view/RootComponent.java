/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view;

import java.util.Set;

/**
 * Component which can be used as root component - windows.
 * 
 */
public interface RootComponent extends ContainerComponent<Object> {

    /**
     * Initialize component and all child components.
     * 
     * @see Component#initializeComponent(java.util.Map)
     */
    void initialize();

    /**
     * Return paths of all components' that listen for changes triggered by given component.
     * 
     * @param path
     *            path of component that trigger event
     * @return listeners' paths
     */
    Set<String> lookupListeners(String path);

    /**
     * Return component which has given path.
     * 
     * @param path
     *            path
     * @return component
     */
    Component<?> lookupComponent(String path);

}
