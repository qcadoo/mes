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

import static com.qcadoo.mes.techSubcontracting.constants.TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TechnologyInstanceOperCompHooksTSTest {

    private TechnologyInstanceOperCompHooksTS technologyInstanceOperCompHooksTS;

    @Mock
    private DataDefinition technologyInstanceOperationComponentDD;

    @Mock
    private Entity technologyInstanceOperationComponent, technologyOperationComponent;

    @Before
    public void init() {
        technologyInstanceOperCompHooksTS = new TechnologyInstanceOperCompHooksTS();
        MockitoAnnotations.initMocks(this);
        when(technologyInstanceOperationComponent.getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT)).thenReturn(
                technologyOperationComponent);
    }

    @Test
    public void shouldSetTrueFromLowerInstance() throws Exception {
        // given
        when(technologyOperationComponent.getBooleanField(IS_SUBCONTRACTING)).thenReturn(true);
        // when
        technologyInstanceOperCompHooksTS.copySubstractingFieldFromLowerInstance(technologyInstanceOperationComponentDD,
                technologyInstanceOperationComponent);
        // then
        Mockito.verify(technologyInstanceOperationComponent).setField(IS_SUBCONTRACTING, true);
    }

    @Test
    public void shouldSetFalseFromLowerInstance() throws Exception {
        // given
        when(technologyOperationComponent.getBooleanField(IS_SUBCONTRACTING)).thenReturn(false);
        // when
        technologyInstanceOperCompHooksTS.copySubstractingFieldFromLowerInstance(technologyInstanceOperationComponentDD,
                technologyInstanceOperationComponent);
        // then
        Mockito.verify(technologyInstanceOperationComponent).setField(IS_SUBCONTRACTING, false);
    }

}
