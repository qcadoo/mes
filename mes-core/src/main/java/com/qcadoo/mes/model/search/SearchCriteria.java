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
     * Return true if deleted entities should be included.
     * 
     * @return include deleted
     * @see SearchCriteriaBuilder#includeDeleted()
     */
    boolean isIncludeDeleted();

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

}
