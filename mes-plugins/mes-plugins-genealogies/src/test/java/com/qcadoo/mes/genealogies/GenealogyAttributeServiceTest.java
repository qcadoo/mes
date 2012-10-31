/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import com.qcadoo.mes.genealogies.constants.GenealogiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.internal.DefaultEntity;

public class GenealogyAttributeServiceTest {

    @Test
    public void shouldReturnExistingGenealogyAttributeId() throws Exception {
        // given
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new DefaultEntity(null, 13L));
        entities.add(new DefaultEntity(null, 14L));

        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        given(
                dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE)
                        .find().setMaxResults(1).list().getEntities()).willReturn(entities);

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
        Entity newEntity = mock(Entity.class);
        DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE))
                .willReturn(dataDefinition);
        given(dataDefinition.create()).willReturn(newEntity);

        Entity savedEntity = new DefaultEntity(dataDefinition, 15L);

        given(dataDefinition.find().setMaxResults(1).list().getEntities()).willReturn(new ArrayList<Entity>());
        given(dataDefinition.save(newEntity)).willReturn(savedEntity);

        GenealogyAttributeService genealogyAttributeService = new GenealogyAttributeService();
        setField(genealogyAttributeService, "dataDefinitionService", dataDefinitionService);

        // when
        Long id = genealogyAttributeService.getGenealogyAttributeId();

        // then
        verify(dataDefinitionService.get(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.MODEL_CURRENT_ATTRIBUTE))
                .save(newEntity);
        verify(newEntity).setField("shift", "");
        verify(newEntity).setField("post", "");
        verify(newEntity).setField("other", "");
        assertEquals(Long.valueOf(15L), id);
    }
}
