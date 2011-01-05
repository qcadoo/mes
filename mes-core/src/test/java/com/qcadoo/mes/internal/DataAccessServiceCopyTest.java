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

import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.hibernate.criterion.Projections;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.model.validators.internal.UniqueValidator;

public final class DataAccessServiceCopyTest extends DataAccessTest {

    @Test
    public void shouldCopyEntity() throws Exception {
        // given
        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(13L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(simpleDatabaseObject);

        // when
        Entity entity = dataDefinition.copy(13L);

        // then
        verify(session).save(Mockito.any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
        Assert.assertEquals(66, entity.getField("age"));
        Assert.assertEquals("Mr T", entity.getField("name"));
    }

    @Test
    public void shouldCopyEntityWithUniqueField() throws Exception {
        // given
        fieldDefinitionName.withValidator(new UniqueValidator());

        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(13L);
        simpleDatabaseObject.setName("Mr T");
        simpleDatabaseObject.setAge(66);

        given(criteria.setProjection(Projections.rowCount()).uniqueResult()).willReturn(0);
        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(simpleDatabaseObject);

        // when
        Entity entity = dataDefinition.copy(13L);

        // then
        verify(session).save(Mockito.any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
        Assert.assertEquals(66, entity.getField("age"));
        Assert.assertEquals("Mr T(1)", entity.getField("name"));
    }

    @Test
    public void shouldCopyEntityWithUniqueField2() throws Exception {
        // given
        fieldDefinitionName.withValidator(new UniqueValidator());

        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(13L);
        simpleDatabaseObject.setName("Mr T(1)");
        simpleDatabaseObject.setAge(66);

        given(criteria.setProjection(Projections.rowCount()).uniqueResult()).willReturn(1, 0);
        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(simpleDatabaseObject);

        // when
        Entity entity = dataDefinition.copy(13L);

        // then
        verify(session).save(Mockito.any(SampleSimpleDatabaseObject.class));
        assertTrue(entity.isValid());
        Assert.assertEquals(66, entity.getField("age"));
        Assert.assertEquals("Mr T(3)", entity.getField("name"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailedCopyEntityWithUniqueField() throws Exception {
        // given
        fieldDefinitionName.withValidator(new UniqueValidator());

        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject();
        simpleDatabaseObject.setId(13L);
        simpleDatabaseObject.setName("Mr T(1)");
        simpleDatabaseObject.setAge(66);

        given(criteria.setProjection(Projections.rowCount()).uniqueResult()).willReturn(1);
        given(session.get(any(Class.class), Matchers.anyInt())).willReturn(simpleDatabaseObject);

        // when
        Entity entity = dataDefinition.copy(13L);
    }

}
