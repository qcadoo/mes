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
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;

public class DataAccessServiceFindTest extends DataAccessTest {

    @Test
    public void shouldReturnValidEntities() throws Exception {
        // given
        List<SampleSimpleDatabaseObject> databaseObjects = new ArrayList<SampleSimpleDatabaseObject>();
        databaseObjects.add(createDatabaseObject(1L, "name1", 1));
        databaseObjects.add(createDatabaseObject(2L, "name2", 2));
        databaseObjects.add(createDatabaseObject(3L, "name3", 3));
        databaseObjects.add(createDatabaseObject(4L, "name4", 4));

        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinition.find().withFirstResult(0).withMaxResults(4);

        given(criteria.uniqueResult()).willReturn(4);
        given(criteria.list()).willReturn(databaseObjects);

        // when
        SearchResult resultSet = searchCriteriaBuilder.list();

        // then
        assertEquals(4, resultSet.getTotalNumberOfEntities());
        assertEquals(4, resultSet.getEntities().size());
        assertEquals(Long.valueOf(1L), resultSet.getEntities().get(0).getId());
        assertEquals(Long.valueOf(2L), resultSet.getEntities().get(1).getId());
        assertEquals(Long.valueOf(3L), resultSet.getEntities().get(2).getId());
        assertEquals(Long.valueOf(4L), resultSet.getEntities().get(3).getId());
    }

    private SampleSimpleDatabaseObject createDatabaseObject(final Long id, final String name, final int age) {
        SampleSimpleDatabaseObject simpleDatabaseObject = new SampleSimpleDatabaseObject(id);
        simpleDatabaseObject.setName(name);
        simpleDatabaseObject.setAge(age);
        return simpleDatabaseObject;
    }

}
