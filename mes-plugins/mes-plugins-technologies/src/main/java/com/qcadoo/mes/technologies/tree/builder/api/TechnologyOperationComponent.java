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
