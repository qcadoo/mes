/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
    void setOperation(final Entity operation);

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
