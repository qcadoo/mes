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
package com.qcadoo.mes.basic;

import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PARTICULAR_PRODUCT;
import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PRODUCTS_FAMILY;
import static com.qcadoo.mes.basic.constants.ProductFields.ENTITY_TYPE;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.SubstituteComponentFields;
import com.qcadoo.mes.basic.constants.SubstituteFields;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

public class ProductServiceTest {

    private static final String L_SZT = "szt";

    private ProductService productService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private UnitService unitService;

    @Mock
    private DataDefinition productDD, substituteDD, substituteComponentDD;

    @Mock
    private Entity product, substitute, substituteComponent;

    @Before
    public final void init() {
        productService = new ProductService();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(productService, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(productService, "unitService", unitService);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfProductEntityTypeIsCorrect() throws Exception {
        // given
        given(product.getStringField(ENTITY_TYPE)).willReturn(PARTICULAR_PRODUCT.getStringValue());

        // when
        boolean result = productService.checkIfProductEntityTypeIsCorrect(product, PARTICULAR_PRODUCT);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfProductEntityTypeIsCorrect() throws Exception {
        // given
        given(product.getStringField(ENTITY_TYPE)).willReturn(PARTICULAR_PRODUCT.getStringValue());

        // when
        boolean result = productService.checkIfProductEntityTypeIsCorrect(product, PRODUCTS_FAMILY);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenProductIsNotRemoved() throws Exception {
        // when
        boolean result = productService.checkIfProductIsNotRemoved(productDD, product);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenEntityDoesnotExists() throws Exception {
        // given
        DataDefinition productDD = Mockito.mock(DataDefinition.class);
        Long productId = 1L;

        given(substitute.getBelongsToField(SubstituteFields.PRODUCT)).willReturn(product);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)).willReturn(productDD);
        given(product.getId()).willReturn(productId);

        given(productDD.get(productId)).willReturn(null);

        // when
        boolean result = productService.checkIfProductIsNotRemoved(substituteDD, substitute);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenProductExists() throws Exception {
        // given
        DataDefinition productDD = Mockito.mock(DataDefinition.class);
        Long productId = 1L;

        given(substitute.getBelongsToField(SubstituteFields.PRODUCT)).willReturn(product);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)).willReturn(productDD);
        given(product.getId()).willReturn(productId);

        given(productDD.get(productId)).willReturn(product);

        // when
        boolean result = productService.checkIfProductIsNotRemoved(substituteDD, substitute);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenSubstituteDoesnotExists() throws Exception {
        // given
        DataDefinition substituteDD = Mockito.mock(DataDefinition.class);
        Long substituteId = 1L;

        given(substituteComponent.getBelongsToField(SubstituteComponentFields.SUBSTITUTE)).willReturn(substitute);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBSTITUTE)).willReturn(
                substituteDD);
        given(substitute.getId()).willReturn(substituteId);
        given(substituteDD.get(substituteId)).willReturn(null);

        // when
        boolean result = productService.checkIfSubstituteIsNotRemoved(substituteComponentDD, substituteComponent);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenEntityDoesnotHaveBTSubstitute() throws Exception {
        // when
        boolean result = productService.checkIfSubstituteIsNotRemoved(substituteComponentDD, substituteComponent);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenSubstitueExists() throws Exception {
        // given
        DataDefinition substituteDD = Mockito.mock(DataDefinition.class);
        Long substituteId = 1L;

        given(substituteComponent.getBelongsToField(SubstituteComponentFields.SUBSTITUTE)).willReturn(substitute);

        given(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBSTITUTE)).willReturn(
                substituteDD);
        given(substitute.getId()).willReturn(substituteId);

        given(substituteDD.get(substituteId)).willReturn(substitute);

        // when
        boolean result = productService.checkIfSubstituteIsNotRemoved(substituteComponentDD, substituteComponent);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldntFillUnitIfUnitIsntNull() {
        // given
        Entity product = mock(Entity.class);
        DataDefinition productDD = mock(DataDefinition.class);

        given(product.getField(UNIT)).willReturn(L_SZT);

        // when
        productService.fillUnit(productDD, product);

        // then
        verify(product, never()).setField(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void shouldFillUnitIfUnitIsNull() {
        // given
        Entity product = mock(Entity.class);
        DataDefinition productDD = mock(DataDefinition.class);

        given(product.getField(UNIT)).willReturn(null);

        given(unitService.getDefaultUnitFromSystemParameters()).willReturn(L_SZT);

        // when
        productService.fillUnit(productDD, product);

        // then
        verify(product).setField(UNIT, L_SZT);
    }

}
