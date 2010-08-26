package com.qcadoo.mes.core.data.search;

import org.hibernate.Criteria;

public interface HibernateRestriction {

    Criteria addToHibernateCriteria(Criteria criteria);

}
