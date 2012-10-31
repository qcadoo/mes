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
package com.qcadoo.mes.techSubcontracting.hooks;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TechInstOperCompHooksTSTest {

    private TechInstOperCompHooksTS techInstOperCompHooksTS;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity, techOperComp;

    @Before
    public void init() {
        techInstOperCompHooksTS = new TechInstOperCompHooksTS();
        MockitoAnnotations.initMocks(this);
        when(entity.getBelongsToField(TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT)).thenReturn(techOperComp);
    }

    @Test
    public void shouldSetTrueFromLowerInstance() throws Exception {
        // given
        when(techOperComp.getBooleanField("isSubcontracting")).thenReturn(true);
        // when
        techInstOperCompHooksTS.copySubstractingFieldFromLowerInstance(dataDefinition, entity);
        // then
        Mockito.verify(entity).setField("isSubcontracting", true);
    }

    @Test
    public void shouldSetFalseFromLowerInstance() throws Exception {
        // given
        when(techOperComp.getBooleanField("isSubcontracting")).thenReturn(false);
        // when
        techInstOperCompHooksTS.copySubstractingFieldFromLowerInstance(dataDefinition, entity);
        // then
        Mockito.verify(entity).setField("isSubcontracting", false);
    }
}
