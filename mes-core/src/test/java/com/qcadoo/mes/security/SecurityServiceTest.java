/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
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
