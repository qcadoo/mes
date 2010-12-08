/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.model.search;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import junit.framework.Assert;

import org.hibernate.Criteria;
import org.hibernate.criterion.IlikeExpression;
import org.hibernate.criterion.NotNullExpression;
import org.hibernate.criterion.NullExpression;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.impl.CriteriaImpl;
import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.internal.DataAccessTest;

public final class SimpleRestricitonTest extends DataAccessTest {

    private SampleSimpleDatabaseObject simpleDatabaseObject = null;

    private Criteria criteria = null;

    @Before
    public void init() {
        simpleDatabaseObject = new SampleSimpleDatabaseObject(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        criteria = new CriteriaImpl(null, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateCriteriaWithEqRestriction() {
        // given
        Restriction restriction = Restrictions.eq(fieldDefinitionName, simpleDatabaseObject.getName());

        // when
        criteria = restriction.addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = ((CriteriaImpl) criteria).iterateExpressionEntries(); criterionIterator
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
        criteria = restriction.addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = ((CriteriaImpl) criteria).iterateExpressionEntries(); criterionIterator
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
        criteria = restriction.addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = ((CriteriaImpl) criteria).iterateExpressionEntries(); criterionIterator
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
        criteria = restriction.addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = ((CriteriaImpl) criteria).iterateExpressionEntries(); criterionIterator
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
        criteria = restriction.addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = ((CriteriaImpl) criteria).iterateExpressionEntries(); criterionIterator
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
        criteria = restriction.addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = ((CriteriaImpl) criteria).iterateExpressionEntries(); criterionIterator
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
        criteria = restriction.addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = ((CriteriaImpl) criteria).iterateExpressionEntries(); criterionIterator
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
        criteria = restriction.addToHibernateCriteria(criteria);

        // then
        for (Iterator<CriteriaImpl.CriterionEntry> criterionIterator = ((CriteriaImpl) criteria).iterateExpressionEntries(); criterionIterator
                .hasNext();) {
            CriteriaImpl.CriterionEntry entry = criterionIterator.next();
            IlikeExpression simpleExpression = (IlikeExpression) entry.getCriterion();
            assertEquals(simpleExpression.toString(), "name ilike " + "%Mr__" + "%");
        }
    }

    @Test
    public void shouldReturnNullIfOrderValidationResultsNotEmpty() throws Exception {
        // given

        // when
        Restriction restriction = Restrictions.eq(fieldDefinitionAge, "Mr");

        // then
        Assert.assertNull(restriction);
    }
}
