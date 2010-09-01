package com.qcadoo.mes.core.data.search.restrictions;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.hibernate.criterion.SimpleExpression;
import org.hibernate.impl.CriteriaImpl;
import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.core.data.internal.SimpleDatabaseObject;
import com.qcadoo.mes.core.data.search.HibernateRestriction;
import com.qcadoo.mes.core.data.search.Restriction;
import com.qcadoo.mes.core.data.search.Restrictions;

public final class EqOrLikeRestrictionTest {

    private SimpleDatabaseObject simpleDatabaseObject = new SimpleDatabaseObject();

    private CriteriaImpl criteria = null;

    @Before
    public void init() {

        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T ma duze oczy");
        simpleDatabaseObject.setAge(66);
        criteria = new CriteriaImpl(null, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateCriteriaWithStringEqRestriction() {
        // given
        Restriction restriction = Restrictions.eqOrLike("name", simpleDatabaseObject.getName());

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
    public void shouldCreateCriteriaWithStringLikeRestriction() {
        // given
        Restriction restriction = Restrictions.eqOrLike("name", "%Mr_?" + "*");

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
