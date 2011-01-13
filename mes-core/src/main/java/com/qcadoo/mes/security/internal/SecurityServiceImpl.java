/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

    @Override
    @Transactional(readOnly = true)
    @Monitorable
    public String getCurrentUserName() {
        return getCurrentUser().getUserName();
    }

}
