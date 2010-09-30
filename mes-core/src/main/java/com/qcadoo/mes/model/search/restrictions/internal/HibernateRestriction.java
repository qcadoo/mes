package com.qcadoo.mes.model.search.restrictions.internal;

import org.hibernate.Criteria;

public interface HibernateRestriction {

    Criteria addToHibernateCriteria(Criteria criteria);

}
