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
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.dataProvider.TechnologyDataProvider;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.mes.technologiesGenerator.GeneratorSettings;
import com.qcadoo.mes.technologiesGenerator.customization.product.ProductCustomizer;
import com.qcadoo.mes.technologiesGenerator.customization.product.domain.ProductSuffixes;
import com.qcadoo.mes.technologiesGenerator.dataProvider.TechnologyMainOutCompDataProvider;
import com.qcadoo.mes.technologiesGenerator.dataProvider.TechnologyProductComponentsDataProvider;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.testing.model.EntityTestUtils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.qcadoo.testing.model.EntityTestUtils.mockDataDefinition;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static com.qcadoo.testing.model.QcadooModelMatchers.anyEntity;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class TechnologyProductsCustomizerTest {

    private static final Long TECH_ID = 1L;

    private TechnologyProductsCustomizer technologyProductsCustomizer;

    @Mock
    private TechnologyProductComponentsDataProvider technologyProductComponentsDataProvider;

    @Mock
    private ProductCustomizer productCustomizer;

    @Mock
    private TechnologyMainOutCompDataProvider technologyMainOutCompDataProvider;

    @Mock
    private TechnologyDataProvider technologyDataProvider;

    @Mock
    private GeneratorSettings generatorSettings;
    
    private Entity newProduct, mainProduct;

    private final TechnologyId technologyId = new TechnologyId(TECH_ID);

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyProductsCustomizer = new TechnologyProductsCustomizer();

        ReflectionTestUtils.setField(technologyProductsCustomizer, "technologyProductComponentsDataProvider",
                technologyProductComponentsDataProvider);
        ReflectionTestUtils.setField(technologyProductsCustomizer, "productCustomizer", productCustomizer);
        ReflectionTestUtils.setField(technologyProductsCustomizer, "technologyMainOutCompDataProvider",
                technologyMainOutCompDataProvider);
        ReflectionTestUtils.setField(technologyProductsCustomizer, "technologyDataProvider", technologyDataProvider);

        newProduct = EntityTestUtils.mockEntity();
        mainProduct = EntityTestUtils.mockEntity();

        stubTechnologyLookup(null);
        stubMainOutputProductComponentLookup(null);
    }

    private void stubTechnologyLookup(final Entity technology) {
        given(technologyDataProvider.tryFind(TECH_ID)).willReturn(Optional.ofNullable(technology));
    }

    private void stubMainOutputProductComponentLookup(final Entity outputProductComponent) {
        given(technologyMainOutCompDataProvider.find(TECH_ID)).willReturn(Optional.ofNullable(outputProductComponent));
    }

    private Entity mockEntity(final Long id, final boolean isValid) {
        return mockEntity(id, isValid, null);
    }

    private Entity mockEntity(final Long id, final boolean isValid, final Entity savedEntity) {
        DataDefinition dataDefinition = mockDataDefinition();
        given(dataDefinition.getName()).willReturn("model-" + id);
        Entity entity = EntityTestUtils.mockEntity(id, dataDefinition);
        given(entity.isValid()).willReturn(isValid);
        if (savedEntity != null) {
            given(dataDefinition.save(entity)).willReturn(savedEntity);
        } else {
            given(dataDefinition.save(entity)).willReturn(entity);
        }
        return entity;
    }

    private String productComponentValidationErrorMessage(final Entity entity) {
        return String.format(
                "Cannot customize operation product component (model = 'model-%s', id = '%s') because of validation errors.",
                entity.getId(), entity.getId());
    }

    private void stubOutputComponents(final Entity... outputComponents) {
        given(technologyProductComponentsDataProvider.findOutputs(eq(technologyId), any(Optional.class), false)).willAnswer(
                (invocation) -> Lists.newArrayList(outputComponents));
    }

    private void stubInputComponents(final Entity inputComponents) {
        given(technologyProductComponentsDataProvider.findInputs(eq(technologyId), false)).willAnswer(
                (invocation) -> Lists.newArrayList(inputComponents));
    }

    private Entity mockOutputComponent(final Long id, final boolean saveWithoutErrors, final Entity customizedProduct) {
        return mockProductComponent(id, saveWithoutErrors, customizedProduct, OperationProductOutComponentFields.PRODUCT);
    }

    private Entity mockInputComponent(final Long id, final boolean saveWithoutErrors, final Entity customizedProduct) {
        return mockProductComponent(id, saveWithoutErrors, customizedProduct, OperationProductInComponentFields.PRODUCT);
    }

    private Entity mockProductComponent(final Long id, final boolean saveWithoutErrors, final Entity customizedProduct,
            final String productFieldName) {
        Entity productComponent = mockEntity(id, true, mockEntity(id, saveWithoutErrors));
        Entity opocProd = mockEntity(id + 100000L, true);
        stubBelongsToField(productComponent, productFieldName, opocProd);
        given(productCustomizer.findOrCreate(eq(opocProd),anyEntity(), any(ProductSuffixes.class), any(GeneratorSettings.class))).willReturn(customizedProduct);
        return productComponent;
    }

    @Test
    public final void shouldFailDueToMissingTechnology() {
        // given

        // when
        Either<String, TechnologyId> result = technologyProductsCustomizer.customize(technologyId, mainProduct, newProduct, generatorSettings);

        // then
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("Technology not found", result.getLeft());
    }

    @Test
    public final void shouldFailDueToTechnologyValidationErrors() {
        // given
        Entity technology = mockEntity(TECH_ID, true, mockEntity(TECH_ID, false));
        stubTechnologyLookup(technology);

        // when
        Either<String, TechnologyId> result = technologyProductsCustomizer.customize(technologyId, mainProduct, newProduct, generatorSettings);

        // then
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals("Validation error while customizing technology product.", result.getLeft());
    }

    @Test
    public final void shouldFailDueToMainOutputProductComponentValidationErrors() {
        // given
        Entity technology = mockEntity(TECH_ID, true);
        stubTechnologyLookup(technology);

        Long mainOpocId = 100L;
        Entity mainOutputProductComponent = mockOutputComponent(mainOpocId, false, EntityTestUtils.mockEntity());
        stubMainOutputProductComponentLookup(mainOutputProductComponent);

        // when
        Either<String, TechnologyId> result = technologyProductsCustomizer.customize(technologyId, mainProduct, newProduct, generatorSettings);

        // then
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals(productComponentValidationErrorMessage(mainOutputProductComponent), result.getLeft());
    }

    @Test
    @Ignore
    public final void shouldFailDueToOutputOperationComponentValidationErrors() {
        // given
        Entity technology = mockEntity(TECH_ID, true);
        stubTechnologyLookup(technology);

        Long mainOpocId = 100L;
        Entity mainOutputProductComponent = mockEntity(mainOpocId, true);
        stubMainOutputProductComponentLookup(mainOutputProductComponent);

        Entity opoc = mockOutputComponent(300L, false, EntityTestUtils.mockEntity());
        stubOutputComponents(opoc);

        // when
        Either<String, TechnologyId> result = technologyProductsCustomizer.customize(technologyId, mainProduct, newProduct, generatorSettings);

        // then
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals(productComponentValidationErrorMessage(opoc), result.getLeft());
    }

    @Test
    @Ignore
    public final void shouldFailDueToInputOperationComponentValidationErrors() {
        // given
        Entity technology = mockEntity(TECH_ID, true);
        stubTechnologyLookup(technology);

        Long mainOpocId = 100L;
        Entity mainOutputProductComponent = mockEntity(mainOpocId, true);
        stubMainOutputProductComponentLookup(mainOutputProductComponent);

        stubOutputComponents(mockOutputComponent(300L, true, EntityTestUtils.mockEntity()));

        Entity opic = mockInputComponent(400L, false, EntityTestUtils.mockEntity());
        stubInputComponents(opic);

        // when
        Either<String, TechnologyId> result = technologyProductsCustomizer.customize(technologyId, mainProduct, newProduct, generatorSettings);

        // then
        Assert.assertTrue(result.isLeft());
        Assert.assertEquals(productComponentValidationErrorMessage(opic), result.getLeft());
    }

    @Test
    @Ignore
    public final void shouldCustomize() {
        // given
        Entity technology = mockEntity(TECH_ID, true);
        stubTechnologyLookup(technology);

        Long mainOpocId = 100L;
        Entity mainOutputProductComponent = mockEntity(mainOpocId, true);
        stubMainOutputProductComponentLookup(mainOutputProductComponent);

        Entity customizedOpocProd = mockEntity(1101L, true);
        Entity opoc = mockOutputComponent(300L, true, customizedOpocProd);
        stubOutputComponents(opoc);

        Entity customizedOpicProd = mockEntity(2101L, true);
        Entity opic = mockInputComponent(400L, true, customizedOpicProd);
        stubInputComponents(opic);

        // when
        Either<String, TechnologyId> result = technologyProductsCustomizer.customize(technologyId, mainProduct, newProduct, generatorSettings);

        // then
        Assert.assertTrue(result.isRight());
        Assert.assertEquals(technologyId, result.getRight());

        verify(technology).setField(TechnologyFields.PRODUCT, newProduct);
        verify(opic).setField(OperationProductInComponentFields.PRODUCT, customizedOpicProd);
        verify(opoc).setField(OperationProductOutComponentFields.PRODUCT, customizedOpocProd);
        verify(mainOutputProductComponent).setField(OperationProductOutComponentFields.PRODUCT, newProduct);
    }

}
