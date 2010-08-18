package com.qcadoo.mes.data.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.definition.FieldTypes;
import com.qcadoo.mes.core.data.definition.FieldValidator;
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
        fieldDefinitions.add(createFieldDefinition("name", FieldTypes.stringType()));
        fieldDefinitions.add(createFieldDefinition("age", FieldTypes.stringType()));
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

    private FieldDefinition createFieldDefinition(final String name, final FieldType type) {
        return new FieldDefinition() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public FieldType getType() {
                return type;
            }

            @Override
            public Set<FieldValidator> getValidators() {
                return null;
            }

            @Override
            public boolean isEditable() {
                return false;
            }

            @Override
            public boolean isRequired() {
                return false;
            }

            @Override
            public boolean isCustomField() {
                return false;
            }

        };
    }

}
