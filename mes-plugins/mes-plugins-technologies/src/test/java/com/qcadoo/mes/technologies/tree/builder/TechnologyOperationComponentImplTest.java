/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.technologies.tree.builder;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TechnologyOperationComponentImplTest {

    private TechnologyOperationCompImpl toc;

    @Mock
    private DataDefinition dataDef;

    @Mock
    private Entity wrappedEntity, operation;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        given(dataDef.create()).willReturn(wrappedEntity);
        given(wrappedEntity.copy()).willReturn(wrappedEntity);

        toc = new TechnologyOperationCompImpl(dataDef);
    }

    @Test
    public final void shouldThrowExceptionWhenTryToAddOperationOfIncorrectType() {
        // given
        stubOperation(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION + "xyz");

        // when & then
        try {
            toc.setOperation(operation);
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            verify(wrappedEntity, Mockito.never()).setField(TechnologyOperationComponentFields.OPERATION, operation);
        }
    }

    @Test
    public final void shouldNotThrowExceptionWhenAddingOperationOfCorrectType() {
        // given
        stubOperation(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION);

        // when
        toc.setOperation(operation);

        // then
        verify(wrappedEntity).setField(TechnologyOperationComponentFields.OPERATION, operation);
    }

    @Test
    public final void shouldCreateOperationComponentWithEntityTypeSetToOperation() {
        // given
        stubOperation(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION);

        // when
        Entity wrappedEntity = toc.getWrappedEntity();

        // then
        verify(wrappedEntity).setField(TechnologyOperationComponentFields.ENTITY_TYPE,
                TechnologyOperationComponentType.OPERATION.getStringValue());
    }

    private void stubOperation(final String pluginId, final String modelName) {
        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(operation.getDataDefinition()).willReturn(dataDefinition);
        given(dataDefinition.getPluginIdentifier()).willReturn(pluginId);
        given(dataDefinition.getName()).willReturn(modelName);
    }

}
