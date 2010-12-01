/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;

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

        given(criteria.uniqueResult()).willReturn(existingDatabaseObject);

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

}
