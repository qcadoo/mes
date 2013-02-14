/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_CONSUMPTION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class TransformationsModelValidatorsTest {

    private TransformationsModelValidators transformationsModelValidators;

    private static final String L_NUMBER_CONSUMPTION_1 = "0000C1";

    private static final String L_NUMBER_CONSUMPTION_2 = "0000C2";

    private static final String L_NUMBER_PRODUCTION_1 = "0000P1";

    private static final String L_NUMBER_PRODUCTION_2 = "0000P2";

    @Mock
    private MaterialFlowService materialFlowService;

    @Mock
    private DataDefinition transformationsDD, transferDD;

    @Mock
    private Entity transformations, transferConsumption1, transferConsumption2, transferProduction1, transferProduction2,
            productConsumption1, productProduction1;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        transformationsModelValidators = new TransformationsModelValidators();

        setField(transformationsModelValidators, "materialFlowService", materialFlowService);
    }

    private static EntityList mockEntityListIterator(final List<Entity> list) {
        EntityList entityList = mock(EntityList.class);

        given(entityList.iterator()).willReturn(list.iterator());

        return entityList;
    }

    @Test
    public void shouldReturnFalseWhenCheckIfTransfersAreValidAndTransfersArentNull() {
        // given
        EntityList transfersConsumption = mockEntityListIterator(Arrays.asList(transferConsumption1));
        EntityList transfersProduction = mockEntityListIterator(Arrays.asList(transferProduction1));

        given(transferConsumption1.getDataDefinition()).willReturn(transferDD);
        given(transferProduction1.getDataDefinition()).willReturn(transferDD);

        given(transferConsumption1.getStringField(NUMBER)).willReturn(null);
        given(transferConsumption1.getBelongsToField(PRODUCT)).willReturn(null);
        given(transferConsumption1.getDecimalField(QUANTITY)).willReturn(null);

        given(transferProduction1.getStringField(NUMBER)).willReturn(null);
        given(transferProduction1.getBelongsToField(PRODUCT)).willReturn(null);
        given(transferProduction1.getDecimalField(QUANTITY)).willReturn(null);

        given(transformations.getHasManyField(TRANSFERS_CONSUMPTION)).willReturn(transfersConsumption);
        given(transformations.getHasManyField(TRANSFERS_CONSUMPTION)).willReturn(transfersProduction);

        // when
        boolean result = transformationsModelValidators.checkIfTransfersAreValid(transformationsDD, transformations);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfTransfersAreValidAndTransferProductAlreadyAdded() {
        // given
        given(transferConsumption1.getDataDefinition()).willReturn(transferDD);
        given(transferConsumption2.getDataDefinition()).willReturn(transferDD);
        given(transferProduction1.getDataDefinition()).willReturn(transferDD);

        given(transferConsumption1.getStringField(NUMBER)).willReturn(L_NUMBER_CONSUMPTION_1);
        given(transferConsumption1.getBelongsToField(PRODUCT)).willReturn(productConsumption1);
        given(transferConsumption1.getDecimalField(QUANTITY)).willReturn(new BigDecimal(1L));
        given(transferConsumption1.getId()).willReturn(null);

        given(transferConsumption2.getStringField(NUMBER)).willReturn(L_NUMBER_CONSUMPTION_2);
        given(transferConsumption2.getBelongsToField(PRODUCT)).willReturn(productConsumption1);
        given(transferConsumption2.getDecimalField(QUANTITY)).willReturn(new BigDecimal(1L));
        given(transferConsumption2.getId()).willReturn(null);

        EntityList transfersConsumption = mockEntityListIterator(Arrays.asList(transferConsumption1, transferConsumption2));
        EntityList transfersProduction = mockEntityListIterator(Arrays.asList(transferProduction1));

        given(transformations.getHasManyField(TRANSFERS_CONSUMPTION)).willReturn(transfersConsumption);
        given(transformations.getHasManyField(TRANSFERS_CONSUMPTION)).willReturn(transfersProduction);

        // when
        boolean result = transformationsModelValidators.checkIfTransfersAreValid(transformationsDD, transformations);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfTransfersAreValidAndTransfersNumersArentDistinct() {
        // given
        given(transferConsumption1.getDataDefinition()).willReturn(transferDD);
        given(transferConsumption2.getDataDefinition()).willReturn(transferDD);
        given(transferProduction1.getDataDefinition()).willReturn(transferDD);

        given(transferConsumption1.getStringField(NUMBER)).willReturn(L_NUMBER_CONSUMPTION_1);
        given(transferConsumption1.getBelongsToField(PRODUCT)).willReturn(productConsumption1);
        given(transferConsumption1.getDecimalField(QUANTITY)).willReturn(new BigDecimal(1L));
        given(transferConsumption1.getId()).willReturn(null);

        given(transferConsumption2.getStringField(NUMBER)).willReturn(L_NUMBER_CONSUMPTION_1);
        given(transferConsumption2.getBelongsToField(PRODUCT)).willReturn(productConsumption1);
        given(transferConsumption2.getDecimalField(QUANTITY)).willReturn(new BigDecimal(1L));
        given(transferConsumption2.getId()).willReturn(null);

        EntityList transfersConsumption = mockEntityListIterator(Arrays.asList(transferConsumption1, transferConsumption2));
        EntityList transfersProduction = mockEntityListIterator(Arrays.asList(transferProduction1));

        given(transformations.getHasManyField(TRANSFERS_CONSUMPTION)).willReturn(transfersConsumption);
        given(transformations.getHasManyField(TRANSFERS_CONSUMPTION)).willReturn(transfersProduction);

        // when
        boolean result = transformationsModelValidators.checkIfTransfersAreValid(transformationsDD, transformations);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfTransfersAreValidAndTransfersArentNull() {
        // given
        given(transferConsumption1.getDataDefinition()).willReturn(transferDD);
        given(transferProduction1.getDataDefinition()).willReturn(transferDD);

        given(transferConsumption1.getStringField(NUMBER)).willReturn(L_NUMBER_CONSUMPTION_1);
        given(transferConsumption1.getBelongsToField(PRODUCT)).willReturn(productConsumption1);
        given(transferConsumption1.getDecimalField(QUANTITY)).willReturn(new BigDecimal(1L));
        given(transferConsumption1.getId()).willReturn(null);

        given(transferProduction1.getStringField(NUMBER)).willReturn(L_NUMBER_PRODUCTION_1);
        given(transferProduction1.getBelongsToField(PRODUCT)).willReturn(productProduction1);
        given(transferProduction1.getDecimalField(QUANTITY)).willReturn(new BigDecimal(1L));
        given(transferProduction1.getId()).willReturn(null);

        EntityList transfersConsumption = mockEntityListIterator(Arrays.asList(transferConsumption1));
        EntityList transfersProduction = mockEntityListIterator(Arrays.asList(transferProduction1));

        given(transformations.getHasManyField(TRANSFERS_CONSUMPTION)).willReturn(transfersConsumption);
        given(transformations.getHasManyField(TRANSFERS_CONSUMPTION)).willReturn(transfersProduction);

        // when
        boolean result = transformationsModelValidators.checkIfTransfersAreValid(transformationsDD, transformations);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfTransfersAreValidAndAllTransfersAreNull() {
        // given
        given(transformations.getHasManyField(TRANSFERS_CONSUMPTION)).willReturn(null);
        given(transformations.getHasManyField(TRANSFERS_CONSUMPTION)).willReturn(null);

        // when
        boolean result = transformationsModelValidators.checkIfTransfersAreValid(transformationsDD, transformations);

        // then
        assertTrue(result);
    }
}
