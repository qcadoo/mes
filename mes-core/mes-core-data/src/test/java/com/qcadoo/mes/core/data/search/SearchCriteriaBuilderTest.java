package com.qcadoo.mes.core.data.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.DataAccessServiceImpl;
import com.qcadoo.mes.core.data.internal.EntityService;
import com.qcadoo.mes.core.data.internal.ValidationService;
import com.qcadoo.mes.core.data.internal.types.FieldTypeFactoryImpl;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;

public final class SearchCriteriaBuilderTest {

    private final DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    private final SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

    private final DictionaryService dictionaryService = mock(DictionaryService.class);

    private DataAccessService dataAccessService = null;

    private EntityService entityService = null;

    private ValidationService validationService = null;

    private FieldDefinition fieldDefinitionName1 = null;

    private FieldDefinition fieldDefinitionName2 = null;

    private FieldDefinition fieldDefinitionName3 = null;

    private FieldDefinition fieldDefinitionName4 = null;

    private FieldDefinition fieldDefinitionName5 = null;

    private FieldDefinition fieldDefinitionName6 = null;

    private FieldTypeFactory fieldTypeFactory = null;

    @Before
    public void init() {

        validationService = new ValidationService();
        ReflectionTestUtils.setField(validationService, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(validationService, "dataDefinitionService", dataDefinitionService);

        entityService = new EntityService();
        ReflectionTestUtils.setField(entityService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(entityService, "validationService", validationService);

        dataAccessService = new DataAccessServiceImpl();
        ReflectionTestUtils.setField(dataAccessService, "entityService", entityService);
        ReflectionTestUtils.setField(dataAccessService, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(dataAccessService, "dataDefinitionService", dataDefinitionService);

        fieldTypeFactory = new FieldTypeFactoryImpl();
        ReflectionTestUtils.setField(fieldTypeFactory, "dictionaryService", dictionaryService);
        ReflectionTestUtils.setField(fieldTypeFactory, "dataAccessService", dataAccessService);

        fieldDefinitionName1 = new FieldDefinition("fieldname1");
        fieldDefinitionName1.setType(fieldTypeFactory.stringType());
        fieldDefinitionName1.setValidators();

        fieldDefinitionName2 = new FieldDefinition("fieldname2");
        fieldDefinitionName2.setType(fieldTypeFactory.stringType());
        fieldDefinitionName2.setValidators();

        fieldDefinitionName3 = new FieldDefinition("fieldname3");
        fieldDefinitionName3.setType(fieldTypeFactory.stringType());
        fieldDefinitionName3.setValidators();

        fieldDefinitionName4 = new FieldDefinition("fieldname4");
        fieldDefinitionName4.setType(fieldTypeFactory.stringType());
        fieldDefinitionName4.setValidators();

        fieldDefinitionName5 = new FieldDefinition("fieldname5");
        fieldDefinitionName5.setType(fieldTypeFactory.stringType());
        fieldDefinitionName5.setValidators();

        fieldDefinitionName6 = new FieldDefinition("fieldname6");
        fieldDefinitionName6.setType(fieldTypeFactory.stringType());
        fieldDefinitionName6.setValidators();

    }

    @Test
    public void shouldCreateCriteriaWithDefaults() throws Exception {
        // when
        SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity("virtual.test").build();

        // then
        assertEquals(0, searchCriteria.getFirstResult());
        assertEquals(25, searchCriteria.getMaxResults());
        assertEquals("virtual.test", searchCriteria.getEntityName());
        assertNull(searchCriteria.getGridName());
        assertEquals("id", searchCriteria.getOrder().getFieldName());
        assertTrue(searchCriteria.getOrder().isAsc());
        assertTrue(searchCriteria.getRestrictions().isEmpty());
    }

    @Test
    public void shouldCreateValidCriteria() throws Exception {
        // when
        SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity("virtual.test").forGrid("gridname").withFirstResult(2)
                .withMaxResults(5).orderBy(Order.desc("field")).restrictedWith(Restrictions.eq(fieldDefinitionName2, 5))
                .restrictedWith(Restrictions.eq(fieldDefinitionName3, "asb%")).build();

        // then
        assertEquals(2, searchCriteria.getFirstResult());
        assertEquals(5, searchCriteria.getMaxResults());
        assertEquals("virtual.test", searchCriteria.getEntityName());
        assertEquals("gridname", searchCriteria.getGridName());
        assertEquals("field", searchCriteria.getOrder().getFieldName());
        assertTrue(searchCriteria.getOrder().isDesc());
        assertEquals(2, searchCriteria.getRestrictions().size());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrownAnExceptionIfThereIsTooManyRestrictions() throws Exception {
        // when
        SearchCriteriaBuilder.forEntity("virtual.test").restrictedWith(Restrictions.eq(fieldDefinitionName1, 5))
                .restrictedWith(Restrictions.eq(fieldDefinitionName2, "asb%"))
                .restrictedWith(Restrictions.eq(fieldDefinitionName3, "asb%"))
                .restrictedWith(Restrictions.eq(fieldDefinitionName4, "asb%"))
                .restrictedWith(Restrictions.eq(fieldDefinitionName5, "asb%"))
                .restrictedWith(Restrictions.eq(fieldDefinitionName6, "asb%")).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfOrderIsNull() throws Exception {
        // when
        SearchCriteriaBuilder.forEntity("virtual.test").orderBy(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfThereIsNoEntityName() throws Exception {
        // when
        SearchCriteriaBuilder.forEntity(null).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrownAnExceptionIfEntityNameIsEmpty() throws Exception {
        // when
        SearchCriteriaBuilder.forEntity("").build();
    }

}
