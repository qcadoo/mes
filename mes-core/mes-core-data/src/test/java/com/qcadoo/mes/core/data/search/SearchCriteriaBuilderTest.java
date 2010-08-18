package com.qcadoo.mes.core.data.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.qcadoo.mes.core.data.search.Order;
import com.qcadoo.mes.core.data.search.Restrictions;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchCriteriaBuilder;

public final class SearchCriteriaBuilderTest {

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
                .withMaxResults(5).orderBy(Order.desc("field")).restrictedWith(Restrictions.eq("fieldname2", 5))
                .restrictedWith(Restrictions.like("fieldname3", "asb%")).build();

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
        SearchCriteriaBuilder.forEntity("virtual.test").restrictedWith(Restrictions.eq("fieldname1", 5))
                .restrictedWith(Restrictions.like("fieldname2", "asb%")).restrictedWith(Restrictions.like("fieldname3", "asb%"))
                .restrictedWith(Restrictions.like("fieldname4", "asb%")).restrictedWith(Restrictions.like("fieldname5", "asb%"))
                .restrictedWith(Restrictions.like("fieldname6", "asb%")).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfOrderIsNull() throws Exception {
        // when
        SearchCriteriaBuilder.forEntity("virtual.test").orderBy(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfrestrictionIsNull() throws Exception {
        // when
        SearchCriteriaBuilder.forEntity("virtual.test").restrictedWith(null).build();
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
