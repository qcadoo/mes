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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;

import org.junit.Test;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;

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

        given(criteria.uniqueResult()).willReturn(simpleDatabaseObject);

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
        fieldDefinitionName.withType(fieldTypeFactory.integerType());

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

}
