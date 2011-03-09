/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.internal.EntityListImpl;

public class EntityListImplTest {

    @Test
    public void shouldBeEmptyIfParentIdIsNull() throws Exception {
        // given
        DataDefinition dataDefinition = mock(DataDefinition.class);
        EntityListImpl list = new EntityListImpl(dataDefinition, "hasMany", null);

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

        EntityListImpl list = new EntityListImpl(dataDefinition, "hasMany", 1L);

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

        EntityList list = new EntityListImpl(dataDefinition, "hasMany", 1L);

        // then
        assertEquals(searchCriteriaBuilder, list.find());
    }

}
