/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.search;

import org.hibernate.Criteria;

/**
 * Restriction represents the part of WHERE clause in SQL query.
 */
public interface Restriction {

    /**
     * Add this restriction to hibernate criteria.
     * 
     * @param criteria
     *            hibernate criteria
     * @return hibernate criteria
     */
    Criteria addToHibernateCriteria(Criteria criteria);

}
