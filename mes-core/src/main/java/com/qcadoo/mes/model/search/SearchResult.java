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

package com.qcadoo.mes.model.search;

import java.util.List;

import com.qcadoo.mes.api.Entity;

/**
 * SearchResult contains list of entities, total number of entities and the search criteria used for produce this search result.
 * 
 * @apiviz.owns com.qcadoo.mes.api.Entity.
 * @apiviz.has com.qcadoo.mes.model.search.SearchCriteria
 */
public interface SearchResult {

    /**
     * Return list of entities matching given criteria.
     * 
     * @return list of entities
     */
    List<Entity> getEntities();

    /**
     * Return search criteria used for produce this search result.
     * 
     * @return criteria used for produce this result
     */
    SearchCriteria getCriteria();

    /**
     * Return total number of matching entities.
     * 
     * @return total number of matching entities
     */
    int getTotalNumberOfEntities();

}
