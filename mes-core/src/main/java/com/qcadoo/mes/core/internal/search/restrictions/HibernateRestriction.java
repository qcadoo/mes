package com.qcadoo.mes.core.internal.search.restrictions;

import org.hibernate.Criteria;

public interface HibernateRestriction {

    Criteria addToHibernateCriteria(Criteria criteria);

}
