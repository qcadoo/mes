package com.qcadoo.mes.core.search;

import org.hibernate.Criteria;

public interface HibernateRestriction {

    Criteria addToHibernateCriteria(Criteria criteria);

}
