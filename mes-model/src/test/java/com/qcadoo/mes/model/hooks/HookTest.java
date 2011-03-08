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

package com.qcadoo.mes.model.hooks;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.qcadoo.mes.beans.sample.CustomEntityService;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.internal.DataAccessTest;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.model.internal.hooks.EntityHookDefinitionImpl;

public class HookTest extends DataAccessTest {

    @Before
    public void init() {
        given(applicationContext.getBean(CustomEntityService.class)).willReturn(new CustomEntityService());
    }

    @Test
    public void shouldNotCallAnyHookIfNotDefined() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", null);
        entity.setField("age", null);

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals(null, entity.getField("name"));
        assertEquals(null, entity.getField("age"));
    }

    @Test
    public void shouldCallOnCreateHook() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.addCreateHook(new EntityHookDefinitionImpl(CustomEntityService.class.getName(), "onCreate",
                applicationContext));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals("create", entity.getField("name"));
        assertEquals(null, entity.getField("age"));
    }

    @Test
    public void shouldCallOnUpdateHook() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition, 1L);
        entity.setField("name", null);
        entity.setField("age", null);

        SampleSimpleDatabaseObject databaseObject = new SampleSimpleDatabaseObject(1L);

        given(sessionFactory.getCurrentSession().get(any(Class.class), Matchers.anyInt())).willReturn(databaseObject);

        dataDefinition.addUpdateHook(new EntityHookDefinitionImpl(CustomEntityService.class.getName(), "onUpdate",
                applicationContext));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals("update", entity.getField("name"));
        assertEquals(null, entity.getField("age"));
    }

    @Test
    public void shouldCallAllDefinedHooksWhileCreating() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.addCreateHook(new EntityHookDefinitionImpl(CustomEntityService.class.getName(), "onCreate",
                applicationContext));
        dataDefinition
                .addSaveHook(new EntityHookDefinitionImpl(CustomEntityService.class.getName(), "onSave", applicationContext));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals("create", entity.getField("name"));
        assertEquals(Integer.valueOf(11), entity.getField("age"));
    }

    @Test
    public void shouldCallOnSaveHookWhileUpdating() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition, 1L);
        entity.setField("name", null);
        entity.setField("age", null);

        SampleSimpleDatabaseObject databaseObject = new SampleSimpleDatabaseObject(1L);

        given(sessionFactory.getCurrentSession().get(any(Class.class), Matchers.anyInt())).willReturn(databaseObject);

        dataDefinition
                .addSaveHook(new EntityHookDefinitionImpl(CustomEntityService.class.getName(), "onSave", applicationContext));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals(null, entity.getField("name"));
        assertEquals(Integer.valueOf(11), entity.getField("age"));
    }

    @Test
    public void shouldCallAllDefinedHooksWhileUpdating() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition, 1L);
        entity.setField("name", null);
        entity.setField("age", null);

        SampleSimpleDatabaseObject databaseObject = new SampleSimpleDatabaseObject(1L);

        given(sessionFactory.getCurrentSession().get(any(Class.class), Matchers.anyInt())).willReturn(databaseObject);

        dataDefinition.addUpdateHook(new EntityHookDefinitionImpl(CustomEntityService.class.getName(), "onUpdate",
                applicationContext));
        dataDefinition
                .addSaveHook(new EntityHookDefinitionImpl(CustomEntityService.class.getName(), "onSave", applicationContext));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals("update", entity.getField("name"));
        assertEquals(Integer.valueOf(11), entity.getField("age"));
    }

    @Test
    public void shouldCallOnSaveHookWhileCreating() throws Exception {
        // given
        Entity entity = new DefaultEntity(dataDefinition);
        entity.setField("name", null);
        entity.setField("age", null);

        dataDefinition.addCreateHook(new EntityHookDefinitionImpl(CustomEntityService.class.getName(), "onSave",
                applicationContext));

        // when
        entity = dataDefinition.save(entity);

        // then
        assertEquals(null, entity.getField("name"));
        assertEquals(Integer.valueOf(11), entity.getField("age"));
    }

}
