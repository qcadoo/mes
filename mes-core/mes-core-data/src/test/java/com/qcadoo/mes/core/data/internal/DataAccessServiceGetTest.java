package com.qcadoo.mes.core.data.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
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

    @Test
    public void shouldReturnValidEntity() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();
        FieldDefinition fieldDefinitionName = mock(FieldDefinition.class, RETURNS_DEEP_STUBS);
        given(fieldDefinitionName.getName()).willReturn("name");
        given(fieldDefinitionName.isCustomField()).willReturn(false);
        given(fieldDefinitionName.getType().isValidType(anyString())).willReturn(true);
        FieldDefinition fieldDefinitionAge = mock(FieldDefinition.class, RETURNS_DEEP_STUBS);
        given(fieldDefinitionAge.getName()).willReturn("age");
        given(fieldDefinitionAge.isCustomField()).willReturn(false);
        given(fieldDefinitionAge.getType().isValidType(anyInt())).willReturn(true);
        fieldDefinitions.add(fieldDefinitionName);
        fieldDefinitions.add(fieldDefinitionAge);
        given(dataDefinitionService.get("entityWithClass")).willReturn(dataDefinition);
        given(dataDefinition.isVirtualTable()).willReturn(false);
        given(dataDefinition.getFullyQualifiedClassName()).willReturn(SimpleDatabaseObject.class.getCanonicalName());
        given(dataDefinition.getFields()).willReturn(fieldDefinitions);
        SimpleDatabaseObject simpleDatabaseObject = new SimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);
        given(hibernateTemplate.get(SimpleDatabaseObject.class, 1L)).willReturn(simpleDatabaseObject);

        // when
        Entity entity = dataAccessService.get("entityWithClass", 1L);

        // then
        Assert.assertEquals(1L, entity.getId().longValue());
        Assert.assertEquals("Mr T", entity.getField("name"));
        Assert.assertEquals(66, entity.getField("age"));
    }

}
