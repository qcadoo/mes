/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
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
     * Set that deleted entities should be included, by default they aren't included.
     * 
     * @return this search builder
     * @see SearchCriteria#isIncludeDeleted()
     */
    SearchCriteriaBuilder includeDeleted();

}
