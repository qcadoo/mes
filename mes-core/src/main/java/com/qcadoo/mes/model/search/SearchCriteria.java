/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

import java.util.Set;

import com.qcadoo.mes.model.DataDefinition;

/**
 * Object represents the criteria for finding entities. It is used for building SQL query.
 * 
 * @apiviz.owns com.qcadoo.mes.model.search.Restriction
 * @apiviz.has com.qcadoo.mes.model.search.Order
 */
public interface SearchCriteria {

    /**
     * Return max results.
     * 
     * @return max results
     * @see SearchCriteriaBuilder#withMaxResults(int)
     */
    int getMaxResults();

    /**
     * Return first result.
     * 
     * @return first result
     * @see SearchCriteriaBuilder#withFirstResult(int)
     */
    int getFirstResult();

    /**
     * Return seach order.
     * 
     * @return order
     * @see SearchCriteriaBuilder#orderBy(Order)
     */
    Order getOrder();

    /**
     * Return list of search restrictions.
     * 
     * @return restrictions
     * @see SearchCriteriaBuilder#restrictedWith(Restriction)
     */
    Set<Restriction> getRestrictions();

    /**
     * Return data definition for searching entities.
     * 
     * @return data definition
     */
    DataDefinition getDataDefinition();

    /**
     * Is distinct enabled?
     * 
     * @return distinct
     */
    String getDistinctProperty();

}
