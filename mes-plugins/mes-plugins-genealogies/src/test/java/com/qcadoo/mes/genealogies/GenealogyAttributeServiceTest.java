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

package com.qcadoo.mes.genealogies;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;

public class GenealogyAttributeServiceTest {

    @Test
    public void shouldReturnExistingGenealogyAttributeId() throws Exception {
        // given
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new DefaultEntity("", "", 13L));
        entities.add(new DefaultEntity("", "", 14L));

        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        given(dataDefinitionService.get("genealogies", "currentAttribute").find().withMaxResults(1).list().getEntities())
                .willReturn(entities);

        GenealogyAttributeService genealogyAttributeService = new GenealogyAttributeService();
        setField(genealogyAttributeService, "dataDefinitionService", dataDefinitionService);

        // when
        Long id = genealogyAttributeService.getGenealogyAttributeId();

        // then
        assertEquals(Long.valueOf(13L), id);
    }

    @Test
    public void shouldReturnNewGenealogyAttributeId() throws Exception {
        // given
        Entity newEntity = new DefaultEntity("genealogies", "currentAttribute");
        newEntity.setField("shift", "");
        newEntity.setField("post", "");
        newEntity.setField("other", "");

        Entity savedEntity = new DefaultEntity("genealogies", "currentAttribute", 15L);

        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        given(dataDefinitionService.get("genealogies", "currentAttribute").find().withMaxResults(1).list().getEntities())
                .willReturn(new ArrayList<Entity>());
        given(dataDefinitionService.get("genealogies", "currentAttribute").save(newEntity)).willReturn(savedEntity);

        GenealogyAttributeService genealogyAttributeService = new GenealogyAttributeService();
        setField(genealogyAttributeService, "dataDefinitionService", dataDefinitionService);

        // when
        Long id = genealogyAttributeService.getGenealogyAttributeId();

        // then
        verify(dataDefinitionService.get("genealogies", "currentAttribute")).save(newEntity);
        assertEquals(Long.valueOf(15L), id);
    }

}
