package com.qcadoo.mes.core.data.internal;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;

public class DataAccessServiceFindTest {

    private DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    private SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

    private DataAccessService dataAccessService = null;

    @Before
    public void init() {
        dataAccessService = new DataAccessServiceImpl();
        ReflectionTestUtils.setField(dataAccessService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(dataAccessService, "sessionFactory", sessionFactory);
    }

    @Test
    public void shouldReturnValidEntities() throws Exception {

    }
}
