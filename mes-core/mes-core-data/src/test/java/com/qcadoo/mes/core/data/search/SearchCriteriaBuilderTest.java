package com.qcadoo.mes.core.data.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.qcadoo.mes.core.data.internal.DataAccessTest;

public final class SearchCriteriaBuilderTest extends DataAccessTest {

    @Test
    public void shouldCreateCriteriaWithDefaults() throws Exception {
        // when
        SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity(dataDefinition).build();

        // then
        assertEquals(0, searchCriteria.getFirstResult());
        assertEquals(25, searchCriteria.getMaxResults());
        assertEquals(dataDefinition, searchCriteria.getDataDefinition());
        assertNull(searchCriteria.getGridDefinition());
        assertEquals("id", searchCriteria.getOrder().getFieldName());
        assertTrue(searchCriteria.getOrder().isAsc());
        assertTrue(searchCriteria.getRestrictions().isEmpty());
    }

    @Test
    public void shouldCreateValidCriteria() throws Exception {
        // when
        // GridDefinition gridDefinition = new GridDefinition("", dataDefinition);
        //
        // SearchCriteria searchCriteria = SearchCriteriaBuilder.forEntity(dataDefinition).forGrid(gridDefinition)
        // .withFirstResult(2).withMaxResults(5).orderBy(Order.desc("age"))
        // .restrictedWith(Restrictions.eq(fieldDefinitionAge, 5))
        // .restrictedWith(Restrictions.eq(fieldDefinitionName, "asb%")).build();
        //
        // // then
        // assertEquals(2, searchCriteria.getFirstResult());
        // assertEquals(5, searchCriteria.getMaxResults());
        // assertEquals(dataDefinition, searchCriteria.getDataDefinition());
        // assertEquals(gridDefinition, searchCriteria.getGridDefinition());
        // assertEquals("age", searchCriteria.getOrder().getFieldName());
        // assertTrue(searchCriteria.getOrder().isDesc());
        // assertEquals(2, searchCriteria.getRestrictions().size());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrownAnExceptionIfThereIsTooManyRestrictions() throws Exception {
        // when
        SearchCriteriaBuilder.forEntity(dataDefinition).restrictedWith(Restrictions.eq(fieldDefinitionAge, 5))
                .restrictedWith(Restrictions.eq(fieldDefinitionName, "asb%"))
                .restrictedWith(Restrictions.eq(fieldDefinitionName, "asd%"))
                .restrictedWith(Restrictions.eq(fieldDefinitionName, "asw%"))
                .restrictedWith(Restrictions.eq(fieldDefinitionName, "asg%"))
                .restrictedWith(Restrictions.eq(fieldDefinitionName, "asu%")).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfOrderIsNull() throws Exception {
        // when
        SearchCriteriaBuilder.forEntity(dataDefinition).orderBy(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrownAnExceptionIfThereIsNoEntityName() throws Exception {
        // when
        SearchCriteriaBuilder.forEntity(null).build();
    }

}
