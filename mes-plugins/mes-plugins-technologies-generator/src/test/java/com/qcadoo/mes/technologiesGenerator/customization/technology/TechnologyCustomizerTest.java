/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.technologiesGenerator.customization.technology;

import com.google.common.collect.Lists;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.TechnologyNameAndNumberGenerator;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.dataProvider.TechnologyDataProvider;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.mes.technologiesGenerator.GeneratorSettings;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorTreeNodeFields;
import com.qcadoo.mes.technologiesGenerator.customization.product.ProductCustomizer;
import com.qcadoo.mes.technologiesGenerator.customization.product.domain.ProductNameSuffix;
import com.qcadoo.mes.technologiesGenerator.customization.product.domain.ProductNumberSuffix;
import com.qcadoo.mes.technologiesGenerator.customization.product.domain.ProductSuffixes;
import com.qcadoo.mes.technologiesGenerator.dataProvider.TechnologyStructureTreeDataProvider;
import com.qcadoo.mes.technologiesGenerator.domain.TechnologyStructureNodeType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.qcadoo.testing.model.EntityTestUtils.*;
import static com.qcadoo.testing.model.QcadooModelMatchers.anyEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class TechnologyCustomizerTest {

    private static final Long NODE_ID = 1L;

    private static final String MAIN_PROD_NAME = "someName";

    private static final String MAIN_PROD_NUMBER = "some-number-001";

    private TechnologyCustomizer technologyCustomizer;

    @Mock
    private TechnologyStructureTreeDataProvider technologyStructureTreeDataProvider;

    @Mock
    private ProductCustomizer productCustomizer;

    private Entity mainProduct;

    @Mock
    private GeneratorSettings generatorSettings;

    @Mock
    private TechnologyNameAndNumberGenerator technologyNameAndNumberGenerator;

    @Mock
    private TechnologyDataProvider technologyDataProvider;

    @Mock
    private TechnologyProductsCustomizer technologyOperationProductsCustomizer;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyCustomizer = new TechnologyCustomizer();

        ReflectionTestUtils.setField(technologyCustomizer, "technologyStructureTreeDataProvider",
                technologyStructureTreeDataProvider);
        ReflectionTestUtils.setField(technologyCustomizer, "productCustomizer", productCustomizer);
        ReflectionTestUtils.setField(technologyCustomizer, "technologyNameAndNumberGenerator", technologyNameAndNumberGenerator);
        ReflectionTestUtils.setField(technologyCustomizer, "technologyDataProvider", technologyDataProvider);
        ReflectionTestUtils.setField(technologyCustomizer, "technologyOperationProductsCustomizer",
                technologyOperationProductsCustomizer);

        stubNodeSearchResult(null);
        given(generatorSettings.shouldCreateAndSwitchProducts()).willReturn(true);
        given(technologyDataProvider.tryFind(anyLong())).willReturn(Optional.empty());
        given(technologyOperationProductsCustomizer.customize(any(TechnologyId.class), anyEntity(), anyEntity(), any(GeneratorSettings.class))).willAnswer(
                invocation -> Either.right((TechnologyId) invocation.getArguments()[0]));
        given(technologyOperationProductsCustomizer.prepareMainTechnologyProduct(anyEntity(), anyEntity())).willReturn(
                Either.right(Optional.empty()));

        mainProduct = mockProduct(ProductFamilyElementType.PRODUCTS_FAMILY);
        stubStringField(mainProduct, ProductFields.NAME, MAIN_PROD_NAME);
        stubStringField(mainProduct, ProductFields.NUMBER, MAIN_PROD_NUMBER);
    }

    private Entity mockNodeEntity(final Entity technology, final TechnologyStructureNodeType type, final Entity parent,
            final Entity product) {
        Entity node = mockEntity();
        stubBelongsToField(node, GeneratorTreeNodeFields.PARENT, parent);
        stubBelongsToField(node, GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, technology);
        stubStringField(node, GeneratorTreeNodeFields.ENTITY_TYPE, type.getStringValue());
        stubBelongsToField(node, GeneratorTreeNodeFields.PRODUCT, product);
        return node;
    }

    private Entity mockTechnology(final Long id, final boolean isValid, final Entity technologyCopy) {
        DataDefinition technologyDD = mockDataDefinition();
        Entity technology = mockEntity(id, technologyDD);
        if (technologyCopy != null) {
            given(technologyDD.copy(anyLong())).willReturn(Lists.newArrayList(technologyCopy));
        }
        given(technologyDD.get(anyLong())).willReturn(technology);
        given(technology.isValid()).willReturn(isValid);
        given(technologyDataProvider.tryFind(id)).willReturn(Optional.of(technology));
        return technology;
    }

    private Entity mockProduct(final ProductFamilyElementType type) {
        Entity product = mockEntity(1L);

        DataDefinition dataDefMock = product.getDataDefinition();
        given(dataDefMock.getPluginIdentifier()).willReturn(BasicConstants.PLUGIN_IDENTIFIER);
        given(dataDefMock.getName()).willReturn(BasicConstants.MODEL_PRODUCT);

        stubStringField(product, ProductFields.ENTITY_TYPE, type.getStringValue());
        return product;
    }

    private void stubNodeSearchResult(final Entity node) {
        given(technologyStructureTreeDataProvider.tryFind(anyLong())).willReturn(Optional.ofNullable(node));
    }

    private void stubProductCustomizer(final Entity customizedProduct) {
        given(productCustomizer.findOrCreate(anyEntity(),anyEntity(), any(ProductSuffixes.class), any(GeneratorSettings.class))).willReturn(customizedProduct);
    }

    private void stubIsValid(final Entity entity, final boolean isValid) {
        given(entity.isValid()).willReturn(isValid);
    }

    private void stubTechNameAndNumberGenerator(final Entity product, final String number, final String name) {
        given(technologyNameAndNumberGenerator.generateNumber(product)).willReturn(number);
        given(technologyNameAndNumberGenerator.generateName(product)).willReturn(name);
    }

    @Test
    public final void shouldCreateCustomizedTechnologyWithCopiedAndCustomizedProduct() {
        // given
        Entity technologyCopy = mockTechnology(2L, true, null);
        Entity technology = mockTechnology(1L, true, technologyCopy);
        stubIsValid(technology, true);

        Entity nodeProduct = mockProduct(ProductFamilyElementType.PRODUCTS_FAMILY);

        Entity node = mockNodeEntity(technology, TechnologyStructureNodeType.COMPONENT, mockEntity(), nodeProduct);
        stubIsValid(node, true);
        stubNodeSearchResult(node);

        Entity mainOpoc = mockEntity();
        stubIsValid(mainOpoc, true);

        Entity customizedProduct = mockEntity();
        stubIsValid(customizedProduct, true);
        stubProductCustomizer(customizedProduct);

        String generatedTechNumber = "generatedTechNumber";
        String generatedTechName = "generated tech name";
        stubTechNameAndNumberGenerator(customizedProduct, generatedTechNumber, generatedTechName);

        // when
        Optional<Either<String, TechnologyId>> result = technologyCustomizer.customize(NODE_ID, mainProduct, generatorSettings,
                false);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get().isRight());
        assertEquals((long) result.get().getRight().get(), 2L);
        verify(technologyOperationProductsCustomizer).customize(new TechnologyId(technologyCopy.getId()), mainProduct,
                customizedProduct, generatorSettings);
        verify(technologyOperationProductsCustomizer, never()).prepareMainTechnologyProduct(anyEntity(), anyEntity());
        verify(technologyNameAndNumberGenerator).generateName(customizedProduct);
        verify(technologyNameAndNumberGenerator).generateNumber(customizedProduct);
        verify(technologyCopy).setField(TechnologyFields.NUMBER, generatedTechNumber);
        verify(technologyCopy).setField(TechnologyFields.NAME, generatedTechName);
        verify(node).setField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, 2L);
        verify(productCustomizer).findOrCreate(anyEntity(),anyEntity(),
                eq(new ProductSuffixes(new ProductNumberSuffix(MAIN_PROD_NUMBER), new ProductNameSuffix(MAIN_PROD_NAME))), any(GeneratorSettings.class));
    }

    @Test
    public final void shouldCreateCustomizedTechnologyWithMainProduct() {
        // given
        Entity technologyCopy = mockTechnology(2L, true, null);
        Entity technology = mockTechnology(1L, true, technologyCopy);
        stubIsValid(technology, true);

        Entity nodeProduct = mockProduct(ProductFamilyElementType.PRODUCTS_FAMILY);

        Entity node = mockNodeEntity(technology, TechnologyStructureNodeType.COMPONENT, null, nodeProduct);
        stubIsValid(node, true);
        stubNodeSearchResult(node);

        Entity mainOpoc = mockEntity();
        stubIsValid(mainOpoc, true);

        String generatedTechNumber = "generatedTechNumber";
        String generatedTechName = "generated tech name";
        stubTechNameAndNumberGenerator(mainProduct, generatedTechNumber, generatedTechName);

        // when
        Optional<Either<String, TechnologyId>> result = technologyCustomizer.customize(NODE_ID, mainProduct, generatorSettings,
                false);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get().isRight());
        assertEquals((long) result.get().getRight().get(), 2L);
        verify(technologyOperationProductsCustomizer).customize(new TechnologyId(technologyCopy.getId()), mainProduct,
                mainProduct, generatorSettings);
        verify(technologyOperationProductsCustomizer, never()).prepareMainTechnologyProduct(anyEntity(), anyEntity());
        verify(technologyNameAndNumberGenerator).generateName(mainProduct);
        verify(technologyNameAndNumberGenerator).generateNumber(mainProduct);
        verify(technologyCopy).setField(TechnologyFields.NUMBER, generatedTechNumber);
        verify(technologyCopy).setField(TechnologyFields.NAME, generatedTechName);
        verify(node).setField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, 2L);
        verify(productCustomizer, never()).findOrCreate(anyEntity(), anyEntity(),any(ProductSuffixes.class), any(GeneratorSettings.class));
    }

    @Test
    public final void shouldCreateCustomizedTechnologyWithMainProductEvenIfTechnologyOutputProductIsNotAProductsFamily() {
        // given
        Entity technologyCopy = mockTechnology(2L, true, null);
        Entity technology = mockTechnology(1L, true, technologyCopy);
        stubIsValid(technology, true);

        Entity nodeProduct = mockProduct(ProductFamilyElementType.PARTICULAR_PRODUCT);

        Entity node = mockNodeEntity(technology, TechnologyStructureNodeType.COMPONENT, null, nodeProduct);
        stubIsValid(node, true);
        stubNodeSearchResult(node);

        Entity mainOpoc = mockEntity();
        stubIsValid(mainOpoc, true);

        String generatedTechNumber = "generatedTechNumber";
        String generatedTechName = "generated tech name";
        stubTechNameAndNumberGenerator(mainProduct, generatedTechNumber, generatedTechName);

        // when
        Optional<Either<String, TechnologyId>> result = technologyCustomizer.customize(NODE_ID, mainProduct, generatorSettings,
                false);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get().isRight());
        assertEquals((long) result.get().getRight().get(), 2L);
        verify(technologyOperationProductsCustomizer).customize(new TechnologyId(technologyCopy.getId()), mainProduct,
                mainProduct, generatorSettings);
        verify(technologyOperationProductsCustomizer, never()).prepareMainTechnologyProduct(anyEntity(), anyEntity());
        verify(technologyNameAndNumberGenerator).generateName(mainProduct);
        verify(technologyNameAndNumberGenerator).generateNumber(mainProduct);
        verify(technologyCopy).setField(TechnologyFields.NUMBER, generatedTechNumber);
        verify(technologyCopy).setField(TechnologyFields.NAME, generatedTechName);
        verify(node).setField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, 2L);
        verify(productCustomizer, never()).findOrCreate(anyEntity(),anyEntity(), any(ProductSuffixes.class), any(GeneratorSettings.class));
    }

    @Test
    public final void shouldCreateCustomizedTechnologyWithoutChangingProduct() {
        // given
        given(generatorSettings.shouldCreateAndSwitchProducts()).willReturn(false);

        Entity technologyCopy = mockTechnology(2L, true, null);
        Entity technology = mockTechnology(1L, true, technologyCopy);
        stubIsValid(technology, true);

        Entity nodeProduct = mockProduct(ProductFamilyElementType.PRODUCTS_FAMILY);
        stubIsValid(nodeProduct, true);

        Entity node = mockNodeEntity(technology, TechnologyStructureNodeType.COMPONENT, mockEntity(), nodeProduct);
        stubIsValid(node, true);
        stubNodeSearchResult(node);

        String generatedTechNumber = "generatedTechNumber";
        String generatedTechName = "generated tech name";
        stubTechNameAndNumberGenerator(nodeProduct, generatedTechNumber, generatedTechName);

        // when
        Optional<Either<String, TechnologyId>> result = technologyCustomizer.customize(NODE_ID, mainProduct, generatorSettings,
                false);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get().isRight());
        assertEquals((long) result.get().getRight().get(), 2L);
        verify(technologyOperationProductsCustomizer, never()).customize(any(TechnologyId.class), anyEntity(), anyEntity(), any(GeneratorSettings.class));
        verify(technologyOperationProductsCustomizer, never()).prepareMainTechnologyProduct(anyEntity(), anyEntity());
        verify(technologyNameAndNumberGenerator).generateName(nodeProduct);
        verify(technologyNameAndNumberGenerator).generateNumber(nodeProduct);
        verify(technologyCopy).setField(TechnologyFields.NUMBER, generatedTechNumber);
        verify(technologyCopy).setField(TechnologyFields.NAME, generatedTechName);
        verify(node).setField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, 2L);
        verify(productCustomizer, never()).findOrCreate(anyEntity(), anyEntity(),any(ProductSuffixes.class),any(GeneratorSettings.class));
    }

    @Test
    public final void shouldCreateCustomizedTechnologyWithAlteredMainOutputProduct() {
        // given
        given(generatorSettings.shouldCreateAndSwitchProducts()).willReturn(false);

        Entity technologyCopy = mockTechnology(2L, true, null);
        Entity technology = mockTechnology(1L, true, technologyCopy);
        stubIsValid(technology, true);

        Entity nodeProduct = mockProduct(ProductFamilyElementType.PRODUCTS_FAMILY);
        stubIsValid(nodeProduct, true);

        Entity node = mockNodeEntity(technology, TechnologyStructureNodeType.COMPONENT, null, nodeProduct);
        stubIsValid(node, true);
        stubNodeSearchResult(node);

        String generatedTechNumber = "generatedTechNumber";
        String generatedTechName = "generated tech name";
        stubTechNameAndNumberGenerator(mainProduct, generatedTechNumber, generatedTechName);

        // when
        Optional<Either<String, TechnologyId>> result = technologyCustomizer.customize(NODE_ID, mainProduct, generatorSettings,
                false);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get().isRight());
        assertEquals((long) result.get().getRight().get(), 2L);
        verify(technologyOperationProductsCustomizer, never()).customize(any(TechnologyId.class), anyEntity(), anyEntity(), any(GeneratorSettings.class));
        verify(technologyOperationProductsCustomizer).prepareMainTechnologyProduct(technologyCopy, mainProduct);
        verify(technologyNameAndNumberGenerator).generateName(mainProduct);
        verify(technologyNameAndNumberGenerator).generateNumber(mainProduct);
        verify(technologyCopy).setField(TechnologyFields.NUMBER, generatedTechNumber);
        verify(technologyCopy).setField(TechnologyFields.NAME, generatedTechName);
        verify(node).setField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, 2L);
        verify(productCustomizer, never()).findOrCreate(anyEntity(), anyEntity(),any(ProductSuffixes.class), any(GeneratorSettings.class));
    }

    @Test
    public final void shouldCreateCustomizedTechnologyWithAlteredMainOutputProduct2() {
        // given
        given(generatorSettings.shouldCreateAndSwitchProducts()).willReturn(false);

        Entity technologyCopy = mockTechnology(2L, true, null);
        Entity technology = mockTechnology(1L, true, technologyCopy);
        stubIsValid(technology, true);

        Entity nodeProduct = mockProduct(ProductFamilyElementType.PARTICULAR_PRODUCT);
        stubIsValid(nodeProduct, true);

        Entity node = mockNodeEntity(technology, TechnologyStructureNodeType.COMPONENT, null, nodeProduct);
        stubIsValid(node, true);
        stubNodeSearchResult(node);

        String generatedTechNumber = "generatedTechNumber";
        String generatedTechName = "generated tech name";
        stubTechNameAndNumberGenerator(mainProduct, generatedTechNumber, generatedTechName);

        // when
        Optional<Either<String, TechnologyId>> result = technologyCustomizer.customize(NODE_ID, mainProduct, generatorSettings,
                false);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get().isRight());
        assertEquals((long) result.get().getRight().get(), 2L);
        verify(technologyOperationProductsCustomizer, never()).customize(any(TechnologyId.class), anyEntity(), anyEntity(), any(GeneratorSettings.class));
        verify(technologyOperationProductsCustomizer).prepareMainTechnologyProduct(technologyCopy, mainProduct);
        verify(technologyNameAndNumberGenerator).generateName(mainProduct);
        verify(technologyNameAndNumberGenerator).generateNumber(mainProduct);
        verify(technologyCopy).setField(TechnologyFields.NUMBER, generatedTechNumber);
        verify(technologyCopy).setField(TechnologyFields.NAME, generatedTechName);
        verify(node).setField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, 2L);
        verify(productCustomizer, never()).findOrCreate(anyEntity(), anyEntity(),any(ProductSuffixes.class), any(GeneratorSettings.class));
    }

    @Test
    public final void shouldCreateCustomizedTechnologyWithoutAlteringMainOutputProduct() {
        // given
        given(generatorSettings.shouldCreateAndSwitchProducts()).willReturn(true);

        Entity technologyCopy = mockTechnology(2L, true, null);
        Entity technology = mockTechnology(1L, true, technologyCopy);
        stubIsValid(technology, true);

        Entity nodeProduct = mockProduct(ProductFamilyElementType.PARTICULAR_PRODUCT);
        stubIsValid(nodeProduct, true);

        Entity node = mockNodeEntity(technology, TechnologyStructureNodeType.COMPONENT, mockEntity(), nodeProduct);
        stubBelongsToField(node, GeneratorTreeNodeFields.PRODUCT, nodeProduct);
        stubIsValid(node, true);
        stubNodeSearchResult(node);

        String generatedTechNumber = "generatedTechNumber";
        String generatedTechName = "generated tech name";
        stubTechNameAndNumberGenerator(nodeProduct, generatedTechNumber, generatedTechName);

        // when
        Optional<Either<String, TechnologyId>> result = technologyCustomizer.customize(NODE_ID, mainProduct, generatorSettings,
                false);

        // then
        assertTrue(result.isPresent());
        assertTrue(result.get().isRight());
        assertEquals((long) result.get().getRight().get(), 2L);
        verify(technologyOperationProductsCustomizer).customize(new TechnologyId(technologyCopy.getId()), mainProduct,
                nodeProduct, generatorSettings);
        verify(technologyOperationProductsCustomizer, never()).prepareMainTechnologyProduct(anyEntity(), anyEntity());
        verify(technologyNameAndNumberGenerator).generateName(nodeProduct);
        verify(technologyNameAndNumberGenerator).generateNumber(nodeProduct);
        verify(technologyCopy).setField(TechnologyFields.NUMBER, generatedTechNumber);
        verify(technologyCopy).setField(TechnologyFields.NAME, generatedTechName);
        verify(node).setField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, 2L);
        verify(productCustomizer, never()).findOrCreate(anyEntity(),anyEntity(), any(ProductSuffixes.class), any(GeneratorSettings.class));
    }

}
