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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Matchers;

import com.qcadoo.mes.beans.sample.SampleParentDatabaseObject;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.internal.EntityListImpl;
import com.qcadoo.model.internal.EntityTreeImpl;
import com.qcadoo.model.internal.types.IntegerType;

public final class DataAccessServiceGetTest extends DataAccessTest {

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfThereIsNoClassForGivenEntityName() throws Exception {
        // given
        dataDefinition.setFullyQualifiedClassName("not.existing.class.Name");

        // when
        dataDefinition.get(1L);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailIfIdIsNull() throws Exception {
        // when
        dataDefinition.get(null);
    }

    @Test
    public void shouldReturnValidEntity() throws Exception {
        // given
        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(simpleDatabaseObject);

        // when
        Entity entity = dataDefinition.get(1L);

        // then
        assertEquals(1L, entity.getId().longValue());
        assertEquals("Mr T", entity.getField("name"));
        assertEquals(66, entity.getField("age"));
    }

    @Test
    public void shouldNotFailIfFieldTypeIsNotValid() throws Exception {
        // given
        fieldDefinitionName.withType(new IntegerType());

        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(1L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        given(criteria.uniqueResult()).willReturn(simpleDatabaseObject);

        // when
        dataDefinition.get(1L);
    }

    public void shouldReturnNullIfEntityNotFound() throws Exception {
        // given
        dataDefinition.setFullyQualifiedClassName(SampleSimpleDatabaseObject.class.getCanonicalName());

        given(criteria.uniqueResult()).willReturn(null);

        // when
        Entity entity = dataDefinition.get(1L);

        // then
        assertNull(entity);
    }

    @Test
    public void shouldGetTreeField() throws Exception {
        // given
        SampleParentDatabaseObject parentDatabaseObject = new SampleParentDatabaseObject();
        parentDatabaseObject.setId(1L);
        parentDatabaseObject.setName("Mr T");

        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(parentDatabaseObject);

        // when
        Entity entity = parentDataDefinition.get(1L);

        // then
        assertEquals(1L, entity.getId().longValue());
        assertEquals("Mr T", entity.getField("name"));
        assertThat(entity.getField("tree"), CoreMatchers.instanceOf(EntityTreeImpl.class));

        EntityTree tree = entity.getTreeField("tree");

        assertEquals(1L, getField(tree, "belongsToId"));
        assertEquals("owner", ((FieldDefinition) getField(tree, "joinFieldDefinition")).getName());
        assertEquals("tree.entity", ((DataDefinition) getField(tree, "dataDefinition")).getName());
    }

    @Test
    public void shouldGetHasManyField() throws Exception {
        // given
        SampleParentDatabaseObject parentDatabaseObject = new SampleParentDatabaseObject();
        parentDatabaseObject.setId(1L);
        parentDatabaseObject.setName("Mr T");

        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(parentDatabaseObject);

        // when
        Entity entity = parentDataDefinition.get(1L);

        // then
        assertEquals(1L, entity.getId().longValue());
        assertEquals("Mr T", entity.getField("name"));
        assertThat(entity.getField("entities"), CoreMatchers.instanceOf(EntityListImpl.class));

        EntityList entities = entity.getHasManyField("entities");

        assertEquals(1L, getField(entities, "parentId"));
        assertEquals("belongsTo", ((FieldDefinition) getField(entities, "joinFieldDefinition")).getName());
        assertEquals("simple.entity", ((DataDefinition) getField(entities, "dataDefinition")).getName());
    }

}
