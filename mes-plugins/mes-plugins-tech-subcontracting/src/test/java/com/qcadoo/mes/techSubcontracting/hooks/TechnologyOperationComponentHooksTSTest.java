/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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

import com.qcadoo.mes.techSubcontracting.constants.OperationFieldsTS;
import com.qcadoo.mes.techSubcontracting.constants.TechnologyOperationComponentFieldsTS;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class TechnologyOperationComponentHooksTSTest {

    private TechnologyOperationComponentHooksTS technologyOperationComponentHooksTS;

    @Mock
    private DataDefinition technologyOperationComponentDD;

    @Mock
    private Entity technologyOperationComponent, operation;

    @Before
    public void init() {
        technologyOperationComponentHooksTS = new TechnologyOperationComponentHooksTS();
        MockitoAnnotations.initMocks(this);
        when(technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION)).thenReturn(operation);
        when(technologyOperationComponent.getField(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING)).thenReturn(null);
    }

    @Test
    public void shouldSetTrueFromLowerInstance() throws Exception {
        // given
        when(technologyOperationComponent.getField(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING)).thenReturn(null);
        when(operation.getBooleanField(OperationFieldsTS.IS_SUBCONTRACTING)).thenReturn(true);
        // when
        technologyOperationComponentHooksTS.copySubcontractingFieldsFromLowerInstance(technologyOperationComponentDD,
                technologyOperationComponent);
        // then
        Mockito.verify(technologyOperationComponent).setField(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING, true);
    }

    @Test
    public void shouldSetFalseFromLowerInstance() throws Exception {
        // given
        when(technologyOperationComponent.getField(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING)).thenReturn(null);
        when(operation.getBooleanField(OperationFieldsTS.IS_SUBCONTRACTING)).thenReturn(false);
        // when
        technologyOperationComponentHooksTS.copySubcontractingFieldsFromLowerInstance(technologyOperationComponentDD,
                technologyOperationComponent);
        // then
        Mockito.verify(technologyOperationComponent).setField(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING, false);
    }

    @Test
    public final void shouldCopyFalseValueFromFieldFromTheSameLevel() throws Exception {
        // given
        when(technologyOperationComponent.getBooleanField(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING)).thenReturn(false);
        // when
        technologyOperationComponentHooksTS.copySubcontractingFieldsFromLowerInstance(technologyOperationComponentDD,
                technologyOperationComponent);
        // then
        boolean isSubcontracting = technologyOperationComponent.getBooleanField(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING);
        Assert.assertEquals(false, isSubcontracting);
    }

    @Test
    public final void shouldCopyTrueValueFromFieldFromTheSameLevel() throws Exception {
        // given
        when(technologyOperationComponent.getBooleanField(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING)).thenReturn(true);
        // when
        technologyOperationComponentHooksTS.copySubcontractingFieldsFromLowerInstance(technologyOperationComponentDD,
                technologyOperationComponent);
        // then
        boolean isSubcontracting = technologyOperationComponent.getBooleanField(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING);
        Assert.assertEquals(true, isSubcontracting);
    }

}
