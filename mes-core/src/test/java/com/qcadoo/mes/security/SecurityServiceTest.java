/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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

package com.qcadoo.mes.security;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.security.internal.SecurityServiceImpl;

public class SecurityServiceTest {

    private SecurityService securityService;

    private SecurityContext securityContext;

    private SessionFactory sessionFactory;

    @Before
    public void init() throws Exception {
        securityContext = mock(SecurityContext.class, RETURNS_DEEP_STUBS);
        SecurityContextHolder.setContext(securityContext);

        sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

        securityService = new SecurityServiceImpl();
        setField(securityService, "sessionFactory", sessionFactory);
    }

    @Test
    public void shouldGetCurrentUser() throws Exception {
        // given
        given(securityContext.getAuthentication().getName()).willReturn("login");

        UsersUser user = new UsersUser();

        given(sessionFactory.getCurrentSession().createCriteria(UsersUser.class).add(Mockito.any(Criterion.class)).uniqueResult())
                .willReturn(user);

        // when
        UsersUser currentUser = securityService.getCurrentUser();

        // then
        Assert.assertEquals(user, currentUser);
    }

}
