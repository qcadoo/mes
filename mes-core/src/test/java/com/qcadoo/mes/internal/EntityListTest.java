package com.qcadoo.mes.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Test;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;

import edu.emory.mathcs.backport.java.util.Collections;

public class EntityListTest {

    @Test
    public void shouldBeEmptyIfParentIdIsNull() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        EntityList list = new EntityList(dataDefinition, "hasMany", null);

        // then
        assertTrue(list.isEmpty());
    }

    @Test
    public void shouldLoadEntities() throws Exception {
        // given
        Entity entity = mock(Entity.class);
        List<Entity> entities = Collections.singletonList(entity);

        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(dataDefinition.getField("hasMany")).willReturn(fieldDefinition);
        given(dataDefinition.find().restrictedWith(Restrictions.belongsTo(fieldDefinition, 1L)).list().getEntities()).willReturn(
                entities);

        EntityList list = new EntityList(dataDefinition, "hasMany", 1L);

        // then
        assertEquals(1, list.size());
        assertEquals(entity, list.get(0));
    }

    @Test
    public void shouldReturnCriteriaBuilder() throws Exception {
        // given
        FieldDefinition fieldDefinition = mock(FieldDefinition.class);
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(dataDefinition.getField("hasMany")).willReturn(fieldDefinition);
        SearchCriteriaBuilder searchCriteriaBuilder = mock(SearchCriteriaBuilder.class);
        given(dataDefinition.find().restrictedWith(Restrictions.belongsTo(fieldDefinition, 1L)))
                .willReturn(searchCriteriaBuilder);

        EntityList list = new EntityList(dataDefinition, "hasMany", 1L);

        // then
        assertEquals(searchCriteriaBuilder, list.find());
    }

}
