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

package com.qcadoo.mes.internal;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.beans.sample.SampleParentDatabaseObject;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.beans.sample.SampleTreeDatabaseObject;

public final class DataAccessServiceSaveTest extends DataAccessTest {

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfEntityWithGivenIdNotExist() throws Exception {
        // then
        dataDefinition.save(new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName(), 1L));
    }

    @Test
    public void shouldSaveNewEntity() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
        entity.setField("name", "Mr T");
        entity.setField("age", 66);

        SampleSimpleDatabaseObject databaseObject = new SampleSimpleDatabaseObject();
        databaseObject.setName("Mr T");
        databaseObject.setAge(66);

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(databaseObject);
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldSaveExistingEntity() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName(), 1L);
        entity.setField("name", "Mr T");
        entity.setField("age", 66);

        SampleSimpleDatabaseObject existingDatabaseObject = new SampleSimpleDatabaseObject();
        existingDatabaseObject.setId(1L);
        existingDatabaseObject.setName("Mr X");
        existingDatabaseObject.setAge(33);

        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(existingDatabaseObject);

        SampleSimpleDatabaseObject databaseObject = new SampleSimpleDatabaseObject();
        databaseObject.setId(1L);
        databaseObject.setName("Mr T");
        databaseObject.setAge(66);

        // when
        entity = dataDefinition.save(entity);

        // then
        verify(session).save(databaseObject);
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldFailIfFieldTypeIsNotValid() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
        entity.setField("name", "Mr T");
        entity.setField("age", "r");

        // when
        entity = dataDefinition.save(entity);

        // then
        assertFalse(entity.isValid());
    }

    @Test
    public void shouldConvertTypeFromInteger() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
        entity.setField("name", "Mr T");
        entity.setField("age", "66");

        // when
        entity = dataDefinition.save(entity);

        // then
        assertTrue(entity.isValid());
    }

    @Test
    public void shouldSaveHasManyField() throws Exception {
        // given
        Entity child1 = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName(), 2L);
        child1.setField("name", "Mr T");
        child1.setField("age", "66");

        Entity child2 = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName());
        child2.setField("name", "Mr X");
        child2.setField("age", "67");

        List<Entity> children = Arrays.asList(new Entity[] { child1, child2 });

        Entity parent = new DefaultEntity(parentDataDefinition.getPluginIdentifier(), parentDataDefinition.getName(), 1L);
        parent.setField("entities", children);

        SampleParentDatabaseObject existingParent = new SampleParentDatabaseObject(1L);
        SampleSimpleDatabaseObject existingChild = new SampleSimpleDatabaseObject(2L);
        SampleSimpleDatabaseObject existingChildToDelete = new SampleSimpleDatabaseObject(13L);

        given(session.get(any(Class.class), Matchers.eq(1L))).willReturn(existingParent);
        given(session.get(any(Class.class), Matchers.eq(2L))).willReturn(existingChild);
        given(session.get(any(Class.class), Matchers.eq(13L))).willReturn(existingChildToDelete);
        given(session.load(any(Class.class), Matchers.eq(1L))).willReturn(existingParent);
        given(criteria.uniqueResult()).willReturn(1L);
        given(criteria.list()).willReturn(
                Arrays.asList(new SampleSimpleDatabaseObject[] { existingChild, existingChildToDelete }));

        // when
        Entity entity = parentDataDefinition.save(parent);

        // then
        assertTrue(entity.isValid());

        List<Entity> entities = (List<Entity>) entity.getField("entities");

        assertEquals(2, entities.size());
        assertEquals(Long.valueOf(1L), entities.get(0).getBelongsToField("belongsTo").getId());
        assertEquals(Long.valueOf(1L), entities.get(1).getBelongsToField("belongsTo").getId());

        verify(session, times(3)).save(Mockito.any());
        verify(session, times(1)).delete(existingChildToDelete);
    }

    @Test
    public void shouldNotSaveEntityListField() throws Exception {
        // given
        EntityList entities = new EntityList(dataDefinition, "", 1L);

        Entity parent = new DefaultEntity(parentDataDefinition.getPluginIdentifier(), parentDataDefinition.getName(), 1L);
        parent.setField("entities", entities);

        SampleParentDatabaseObject existingParent = new SampleParentDatabaseObject(1L);

        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(existingParent);
        given(session.load(any(Class.class), Matchers.eq(1L))).willReturn(existingParent);
        given(criteria.uniqueResult()).willReturn(1L);

        // when
        Entity entity = parentDataDefinition.save(parent);

        // then
        assertTrue(entity.isValid());

        assertEquals(entities, entity.getField("entities"));

        verify(session, times(1)).save(Mockito.any());
    }

    @Test
    public void shouldSaveTreeField() throws Exception {
        // given
        Entity child1 = new DefaultEntity(treeDataDefinition.getPluginIdentifier(), treeDataDefinition.getName());
        child1.setField("name", "Mr T");

        Entity root = new DefaultEntity(treeDataDefinition.getPluginIdentifier(), treeDataDefinition.getName(), 2L);
        root.setField("name", "Mr X");
        root.setField("children", Collections.singletonList(child1));

        Entity parent = new DefaultEntity(parentDataDefinition.getPluginIdentifier(), parentDataDefinition.getName(), 1L);
        parent.setField("tree", Collections.singletonList(root));

        SampleParentDatabaseObject existingParent = new SampleParentDatabaseObject(1L);
        SampleTreeDatabaseObject existingChild = new SampleTreeDatabaseObject(2L);

        given(session.get(any(Class.class), Matchers.eq(1L))).willReturn(existingParent);
        given(session.get(any(Class.class), Matchers.eq(2L))).willReturn(existingChild);
        given(session.load(any(Class.class), Matchers.eq(1L))).willReturn(existingParent);
        given(session.load(any(Class.class), Matchers.eq(2L))).willReturn(existingChild);
        given(criteria.uniqueResult()).willReturn(1L);

        // when
        Entity entity = parentDataDefinition.save(parent);

        // then
        assertTrue(entity.isValid());

        List<Entity> rootEntities = (List<Entity>) entity.getField("tree");

        assertEquals(1, rootEntities.size());
        assertEquals(Long.valueOf(1L), rootEntities.get(0).getBelongsToField("owner").getId());
        assertNull(rootEntities.get(0).getBelongsToField("parent"));

        List<Entity> childEntities = (List<Entity>) rootEntities.get(0).getField("children");

        assertEquals(1, childEntities.size());
        assertEquals(Long.valueOf(1L), childEntities.get(0).getBelongsToField("owner").getId());
        assertEquals(Long.valueOf(2L), childEntities.get(0).getBelongsToField("parent").getId());

        verify(session, times(3)).save(Mockito.any());
    }

    @Test
    public void shouldNotSaveEntityTreeField() throws Exception {
        // given
        EntityTree tree = new EntityTree(dataDefinition, "", 1L);

        Entity parent = new DefaultEntity(parentDataDefinition.getPluginIdentifier(), parentDataDefinition.getName(), 1L);
        parent.setField("tree", tree);

        SampleParentDatabaseObject existingParent = new SampleParentDatabaseObject(1L);

        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(existingParent);
        given(session.load(any(Class.class), Matchers.eq(1L))).willReturn(existingParent);
        given(criteria.uniqueResult()).willReturn(1L);

        // when
        Entity entity = parentDataDefinition.save(parent);

        // then
        assertTrue(entity.isValid());

        assertEquals(tree, entity.getField("tree"));

        verify(session, times(1)).save(Mockito.any());
    }

}
