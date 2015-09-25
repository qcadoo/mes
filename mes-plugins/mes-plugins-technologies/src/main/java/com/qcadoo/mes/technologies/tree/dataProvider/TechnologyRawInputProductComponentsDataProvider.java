/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.technologies.tree.dataProvider;

import java.util.List;

import com.qcadoo.model.api.Entity;

/**
 * Data provider for raw materials - input product components, which don't come from any sub-operation in given technology.
 * 
 * @since 1.4
 */
public interface TechnologyRawInputProductComponentsDataProvider {

    /**
     * Find all input raw material components (technology operation input product components) from given technology, that match
     * given criteria.
     * 
     * @param criteria
     *            additional criteria, projection, search orders and so on
     * @return all matching input raw materials.
     * 
     * @since 1.4
     */
    List<Entity> findAll(final TechnologyRawInputProductComponentsCriteria criteria);

}
