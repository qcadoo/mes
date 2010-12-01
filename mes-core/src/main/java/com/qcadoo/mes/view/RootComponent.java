/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
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
