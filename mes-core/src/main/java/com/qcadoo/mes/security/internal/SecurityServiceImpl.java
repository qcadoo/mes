/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.security.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.model.aop.internal.Monitorable;

@Service
public final class SecurityServiceImpl implements SecurityService {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public UsersUser getCurrentUser() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        UsersUser user = (UsersUser) sessionFactory.getCurrentSession().createCriteria(UsersUser.class)
                .add(Restrictions.eq("userName", login)).uniqueResult();
        checkNotNull(user, "Current user with login %s cannot be found", login);
        return user;
    }

}
