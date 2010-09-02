package com.qcadoo.mes.core.data.search.restrictions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.Iterator;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.NotNullExpression;
import org.hibernate.criterion.NullExpression;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.impl.CriteriaImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.DataAccessServiceImpl;
import com.qcadoo.mes.core.data.internal.EntityService;
import com.qcadoo.mes.core.data.internal.SimpleDatabaseObject;
import com.qcadoo.mes.core.data.internal.ValidationService;
import com.qcadoo.mes.core.data.internal.types.FieldTypeFactoryImpl;
import com.qcadoo.mes.core.data.search.HibernateRestriction;
import com.qcadoo.mes.core.data.search.Restriction;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;

public final class SimpleRestricitonTest {

    private final DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    private final SessionFactory sessionFactory = mock(SessionFactory.class, RETURNS_DEEP_STUBS);

    private SimpleDatabaseObject simpleDatabaseObject = new SimpleDatabaseObject();

    private final DictionaryService dictionaryService = mock(DictionaryService.class);

    private DataAccessService dataAccessService = null;

    private EntityService entityService = null;

    private ValidationService validationService = null;

    private CriteriaImpl criteria = null;

    private FieldDefinition fieldDefinitionName = null;

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

        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);
        criteria = new CriteriaImpl(null, null);
        fieldDefinitionName = new FieldDefinition("name");
        fieldDefinitionName.setType(fieldTypeFactory.stringType());
        fieldDefinitionName.setValidators();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateCriteriaWithEqRestriction() {
        // given
        Restriction restriction = Restrictions.eq(fieldDefinitionName, simpleDatabaseObject.getName());

        // when
        criteria = (CriteriaImpl) ((HibernateRestriction) restriction).addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = criteria.iterateExpressionEntries(); criterionIterator
                .hasNext();) {
            CriteriaImpl.CriterionEntry entry = criterionIterator.next();
            SimpleExpression simpleExpression = (SimpleExpression) entry.getCriterion();
            assertEquals(simpleExpression.toString(), "name=" + simpleDatabaseObject.getName());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateCriteriaWithGeRestriction() {
        // given
        Restriction restriction = Restrictions.ge(fieldDefinitionName, simpleDatabaseObject.getName());

        // when
        criteria = (CriteriaImpl) ((HibernateRestriction) restriction).addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = criteria.iterateExpressionEntries(); criterionIterator
                .hasNext();) {
            CriteriaImpl.CriterionEntry entry = criterionIterator.next();
            SimpleExpression simpleExpression = (SimpleExpression) entry.getCriterion();
            assertEquals(simpleExpression.toString(), "name>=" + simpleDatabaseObject.getName());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateCriteriaWithGtRestriction() {
        // given
        Restriction restriction = Restrictions.gt(fieldDefinitionName, simpleDatabaseObject.getName());

        // when
        criteria = (CriteriaImpl) ((HibernateRestriction) restriction).addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = criteria.iterateExpressionEntries(); criterionIterator
                .hasNext();) {
            CriteriaImpl.CriterionEntry entry = criterionIterator.next();
            SimpleExpression simpleExpression = (SimpleExpression) entry.getCriterion();
            assertEquals(simpleExpression.toString(), "name>" + simpleDatabaseObject.getName());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateCriteriaWithLeRestriction() {
        // given
        Restriction restriction = Restrictions.le(fieldDefinitionName, simpleDatabaseObject.getName());

        // when
        criteria = (CriteriaImpl) ((HibernateRestriction) restriction).addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = criteria.iterateExpressionEntries(); criterionIterator
                .hasNext();) {
            CriteriaImpl.CriterionEntry entry = criterionIterator.next();
            SimpleExpression simpleExpression = (SimpleExpression) entry.getCriterion();
            assertEquals(simpleExpression.toString(), "name<=" + simpleDatabaseObject.getName());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateCriteriaWithLtRestriction() {
        // given
        Restriction restriction = Restrictions.lt(fieldDefinitionName, simpleDatabaseObject.getName());

        // when
        criteria = (CriteriaImpl) ((HibernateRestriction) restriction).addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = criteria.iterateExpressionEntries(); criterionIterator
                .hasNext();) {
            CriteriaImpl.CriterionEntry entry = criterionIterator.next();
            SimpleExpression simpleExpression = (SimpleExpression) entry.getCriterion();
            assertEquals(simpleExpression.toString(), "name<" + simpleDatabaseObject.getName());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateCriteriaWithIsNullRestriction() {
        // given
        Restriction restriction = Restrictions.isNull(fieldDefinitionName);

        // when
        criteria = (CriteriaImpl) ((HibernateRestriction) restriction).addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = criteria.iterateExpressionEntries(); criterionIterator
                .hasNext();) {
            CriteriaImpl.CriterionEntry entry = criterionIterator.next();
            NullExpression nullExpression = (NullExpression) entry.getCriterion();
            assertEquals(nullExpression.toString(), "name is null");
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateCriteriaWithIsNotNullRestriction() {
        // given
        Restriction restriction = Restrictions.isNotNull(fieldDefinitionName);

        // when
        criteria = (CriteriaImpl) ((HibernateRestriction) restriction).addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = criteria.iterateExpressionEntries(); criterionIterator
                .hasNext();) {
            CriteriaImpl.CriterionEntry entry = criterionIterator.next();
            NotNullExpression notNullExpression = (NotNullExpression) entry.getCriterion();
            assertEquals(notNullExpression.toString(), "name is not null");
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateCriteriaWithStringLikeRestriction() {
        // given
        Restriction restriction = Restrictions.eq(fieldDefinitionName, "%Mr_?" + "*");

        // when
        criteria = (CriteriaImpl) ((HibernateRestriction) restriction).addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = criteria.iterateExpressionEntries(); criterionIterator
                .hasNext();) {
            CriteriaImpl.CriterionEntry entry = criterionIterator.next();
            SimpleExpression simpleExpression = (SimpleExpression) entry.getCriterion();
            assertEquals(simpleExpression.toString(), "name like " + "%Mr__" + "%");
        }
    }
}
