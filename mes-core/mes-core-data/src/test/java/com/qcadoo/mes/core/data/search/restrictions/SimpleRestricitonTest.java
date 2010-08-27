package com.qcadoo.mes.core.data.search.restrictions;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.hibernate.criterion.NotNullExpression;
import org.hibernate.criterion.NullExpression;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.impl.CriteriaImpl;
import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.core.data.internal.SimpleDatabaseObject;
import com.qcadoo.mes.core.data.search.HibernateRestriction;
import com.qcadoo.mes.core.data.search.Restriction;
import com.qcadoo.mes.core.data.search.Restrictions;

public final class SimpleRestricitonTest {

    private SimpleDatabaseObject simpleDatabaseObject = new SimpleDatabaseObject();

    private CriteriaImpl criteria = null;

    @Before
    public void init() {

        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);
        criteria = new CriteriaImpl(null, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateCriteriaWithEqRestriction() {
        // given
        Restriction restriction = Restrictions.eq("name", simpleDatabaseObject.getName());

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
        Restriction restriction = Restrictions.ge("name", simpleDatabaseObject.getName());

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
        Restriction restriction = Restrictions.gt("name", simpleDatabaseObject.getName());

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
        Restriction restriction = Restrictions.le("name", simpleDatabaseObject.getName());

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
        Restriction restriction = Restrictions.lt("name", simpleDatabaseObject.getName());

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
        Restriction restriction = Restrictions.isNull("name");

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
        Restriction restriction = Restrictions.isNotNull("name");

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
}
