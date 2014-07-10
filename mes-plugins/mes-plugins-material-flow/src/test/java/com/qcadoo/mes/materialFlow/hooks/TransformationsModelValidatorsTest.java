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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_PRODUCTION;
import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static com.qcadoo.testing.model.EntityTestUtils.stubDecimalField;
import static com.qcadoo.testing.model.EntityTestUtils.stubHasManyField;
import static com.qcadoo.testing.model.EntityTestUtils.stubStringField;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.materialFlow.constants.TransferFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TransformationsModelValidatorsTest {

    private TransformationsModelValidators transformationsModelValidators;

    private static final String L_NUMBER_CONSUMPTION_1 = "0000C1";

    private static final String L_NUMBER_CONSUMPTION_2 = "0000C2";

    private static final String L_NUMBER_PRODUCTION_1 = "0000P1";

    @Mock
    private MaterialFlowService materialFlowService;

    @Mock
    private DataDefinition transformationsDD, transferDD;

    @Mock
    private Entity transformations, productConsumption, productProduction;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        transformationsModelValidators = new TransformationsModelValidators();

        setField(transformationsModelValidators, "materialFlowService", materialFlowService);

        stubHasManyField(transformations, TRANSFERS_PRODUCTION, Lists.<Entity> newArrayList());
        stubHasManyField(transformations, TRANSFERS_CONSUMPTION, Lists.<Entity> newArrayList());
    }

    @Test
    public void shouldReturnFalseWhenCheckIfTransfersAreValidAndTransfersArentNull() {
        // given
        Entity transferConsumption = mockTransfer(null, null, null);
        Entity transferProduction = mockTransfer(null, null, null);
        stubHasManyField(transformations, TRANSFERS_CONSUMPTION, Lists.newArrayList(transferConsumption));
        stubHasManyField(transformations, TRANSFERS_PRODUCTION, Lists.newArrayList(transferProduction));

        // when
        boolean result = transformationsModelValidators.checkIfTransfersAreValid(transformationsDD, transformations);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfTransfersAreValidAndTransferProductAlreadyAdded() {
        // given
        Entity transferConsumption1 = mockTransfer(L_NUMBER_CONSUMPTION_1, productConsumption, BigDecimal.ONE);
        Entity transferConsumption2 = mockTransfer(L_NUMBER_CONSUMPTION_2, productProduction, BigDecimal.ONE);
        stubHasManyField(transformations, TRANSFERS_CONSUMPTION, Lists.newArrayList(transferConsumption1, transferConsumption2));

        // when
        boolean result = transformationsModelValidators.checkIfTransfersAreValid(transformationsDD, transformations);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfTransfersAreValidAndTransfersNumersArentDistinct() {
        // given
        Entity transferConsumption1 = mockTransfer(L_NUMBER_CONSUMPTION_1, productConsumption, BigDecimal.ONE);
        Entity transferConsumption2 = mockTransfer(L_NUMBER_CONSUMPTION_1, productProduction, BigDecimal.ONE);
        stubHasManyField(transformations, TRANSFERS_CONSUMPTION, Lists.newArrayList(transferConsumption1, transferConsumption2));

        // when
        boolean result = transformationsModelValidators.checkIfTransfersAreValid(transformationsDD, transformations);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfTransfersAreValidAndTransfersArentNull() {
        // given
        Entity transferConsumption = mockTransfer(L_NUMBER_CONSUMPTION_1, productProduction, BigDecimal.ONE);
        Entity transferProduction = mockTransfer(L_NUMBER_PRODUCTION_1, productProduction, BigDecimal.ONE);

        stubHasManyField(transformations, TRANSFERS_CONSUMPTION, Lists.newArrayList(transferConsumption));
        stubHasManyField(transformations, TRANSFERS_PRODUCTION, Lists.newArrayList(transferProduction));

        // when
        boolean result = transformationsModelValidators.checkIfTransfersAreValid(transformationsDD, transformations);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfTransfersAreValidAndAllTransfersAreNull() {
        // when
        boolean result = transformationsModelValidators.checkIfTransfersAreValid(transformationsDD, transformations);

        // then
        assertTrue(result);
    }

    private Entity mockTransfer(final String number, final Entity product, final BigDecimal quantity) {
        Entity transfer = mockEntity(transferDD);
        stubStringField(transfer, TransferFields.NUMBER, number);
        stubBelongsToField(transfer, TransferFields.PRODUCT, product);
        stubDecimalField(transfer, TransferFields.QUANTITY, quantity);
        given(transfer.getId()).willReturn(null);
        return transfer;
    }
}
