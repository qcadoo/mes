package com.qcadoo.mes.data.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.internal.DataAccessServiceImpl;

public class DataAccessServiceGetTest {

    private DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    private HibernateTemplate hibernateTemplate = mock(HibernateTemplate.class);

    private DataAccessService dataAccessService = null;

    @Before
    public void init() {
        dataAccessService = new DataAccessServiceImpl();
        ReflectionTestUtils.setField(dataAccessService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(dataAccessService, "hibernateTemplate", hibernateTemplate);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailIfThereIsNoDataDefinitionForGivenEntityName() throws Exception {
        // when
        dataAccessService.get("not existing entity name", 1L);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfThereIsNoClassForGivenEntityName() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(dataDefinitionService.get("entityWithNoClass")).willReturn(dataDefinition);
        given(dataDefinition.isVirtualTable()).willReturn(false);
        given(dataDefinition.getFullyQualifiedClassName()).willReturn("not.existing.class.Name");

        // when
        dataAccessService.get("entityWithNoClass", 1L);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldReturnValidEntity() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(dataDefinitionService.get("entityWithClass")).willReturn(dataDefinition);
        given(dataDefinition.isVirtualTable()).willReturn(false);
        given(dataDefinition.getFullyQualifiedClassName()).willReturn("com.qcadoo.mes.data.internal.SimpleDatabaseObject");
        SimpleDatabaseObject simpleDatabaseObject = new SimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        given(hibernateTemplate.get(SimpleDatabaseObject.class, 1L)).willReturn(simpleDatabaseObject);

        // when
        Entity entity = dataAccessService.get("entityWithClass", 1L);

        // then
        Assert.assertEquals(1L, entity.getId().longValue());
    }

    private static class SimpleDatabaseObject {

        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

    }

}
