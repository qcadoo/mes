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

/**
 * Object represents the criteria builer for finding entities.
 * 
 * @see com.qcadoo.mes.model.search.SearchCriteria
 * @apiviz.owns com.qcadoo.mes.model.search.Restriction
 * @apiviz.has com.qcadoo.mes.model.search.Order
 * @apiviz.uses com.qcadoo.mes.internal.DataAccessService
 */
public interface SearchCriteriaBuilder {

    /**
     * Find entities using this criteria.
     * 
     * @return search result
     * @see com.qcadoo.mes.internal.DataAccessService#find(SearchCriteria)
     */
    SearchResult list();

    /**
     * Add the restriction.
     * 
     * @param restriction
     *            restriction
     * @return this search builder
     * @see SearchCriteria#getRestrictions()
     */
    SearchCriteriaBuilder restrictedWith(Restriction restriction);

    /**
     * Set the asc order by given field, by default there is an order by id.
     * 
     * @param order
     *            order
     * @return this search builder
     * @see SearchCriteria#getOrder()
     */
    SearchCriteriaBuilder orderAscBy(String fieldName);

    /**
     * Set the desc order by given field, by default there is an order by id.
     * 
     * @param order
     *            order
     * @return this search builder
     * @see SearchCriteria#getOrder()
     */
    SearchCriteriaBuilder orderDescBy(String fieldName);

    /**
     * Set the max results, by default there is no limit.
     * 
     * @param maxResults
     *            max results
     * @return this search builder
     * @see SearchCriteria#getMaxResults()
     */
    SearchCriteriaBuilder withMaxResults(int maxResults);

    /**
     * Set the first result, by default the first result is equal to zero.
     * 
     * @param firstResult
     *            first result
     * @return this search builder
     * @see SearchCriteria#getFirstResult()
     */
    SearchCriteriaBuilder withFirstResult(int firstResult);

    /**
     * Set distinct option.
     */
    void withDistinctProperty(String distinctProperty);

}
