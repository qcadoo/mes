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

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFields;
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
import com.qcadoo.mes.technologiesGenerator.domain.OutputProductComponentId;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyProductsCustomizer {

    private static final String VALIDATION_ERR_TEMPLATE = "Cannot customize operation product component "
            + "(model = '%s', id = '%s') because of validation errors.";

    @Autowired
    private TechnologyProductComponentsDataProvider technologyProductComponentsDataProvider;

    @Autowired
    private ProductCustomizer productCustomizer;

    @Autowired
    private TechnologyMainOutCompDataProvider technologyMainOutCompDataProvider;

    @Autowired
    private TechnologyDataProvider technologyDataProvider;

    public Either<String, TechnologyId> customize(final TechnologyId technologyId, final Entity mainProduct,
            final Entity newProduct, final GeneratorSettings settings) {
        CustomizationSettings cs = new CustomizationSettings(technologyId, ProductSuffixes.from(mainProduct));
        return technologyDataProvider.tryFind(technologyId.get())
                .map(technology -> prepareMainTechnologyProduct(technology, newProduct)
                        .<TechnologyId> flatMap(mOpicId -> customizeOutputs(cs, mOpicId, settings, mainProduct))
                        .<TechnologyId> flatMap(x -> customizeInputs(cs, settings, mainProduct)))
                .orElseGet(() -> Either.left("Technology not found"));
    }

    public Either<String, TechnologyId> customizeForTechnologyGeneration(final TechnologyId technologyId,
            final Entity mainProduct, final Entity newProduct, final GeneratorSettings settings) {
        CustomizationSettings cs = new CustomizationSettings(technologyId, ProductSuffixes.from(mainProduct));
        return technologyDataProvider.tryFind(technologyId.get())
                .map(technology -> prepareMainTechnologyProduct(technology, newProduct)
                        .<TechnologyId> flatMap(
                                mOpicId -> customizeOutputsForTechnologyGeneration(cs, mOpicId, settings, mainProduct))
                        .<TechnologyId> flatMap(x -> customizeInputsForTechnologyGeneration(cs, settings, mainProduct)))
                .orElseGet(() -> Either.left("Technology not found"));
    }

    private Either<String, TechnologyId> customizeOutputsForTechnologyGeneration(final CustomizationSettings cs,
            final Optional<OutputProductComponentId> excludedOutputComponentId, final GeneratorSettings settings,
            final Entity mainProduct) {
        List<Entity> outputProductComponents = technologyProductComponentsDataProvider.findOutputs(cs.technologyId,
                excludedOutputComponentId, true);
        return customizeOperationProducts(outputProductComponents, OperationProductOutComponentFields.PRODUCT, cs, true, settings,
                mainProduct);
    }

    private Either<String, TechnologyId> customizeInputsForTechnologyGeneration(final CustomizationSettings cs,
            final GeneratorSettings settings, final Entity mainProduct) {
        List<Entity> inputProductComponents = technologyProductComponentsDataProvider.findInputs(cs.technologyId, true);
        return customizeOperationProducts(inputProductComponents, OperationProductInComponentFields.PRODUCT, cs, true, settings,
                mainProduct);
    }

    private Either<String, TechnologyId> customizeOutputs(final CustomizationSettings cs,
            final Optional<OutputProductComponentId> excludedOutputComponentId, final GeneratorSettings settings,
            final Entity mainProduct) {
        List<Entity> outputProductComponents = technologyProductComponentsDataProvider.findOutputs(cs.technologyId,
                excludedOutputComponentId, false);
        return customizeOperationProducts(outputProductComponents, OperationProductOutComponentFields.PRODUCT, cs, false,
                settings, mainProduct);
    }

    private Either<String, TechnologyId> customizeInputs(final CustomizationSettings cs, final GeneratorSettings settings,
            final Entity mainProduct) {
        List<Entity> inputProductComponents = technologyProductComponentsDataProvider.findInputs(cs.technologyId, false);
        return customizeOperationProducts(inputProductComponents, OperationProductInComponentFields.PRODUCT, cs, false, settings,
                mainProduct);
    }

    private Either<String, TechnologyId> customizeOperationProducts(final List<Entity> productComponents,
            final String productFieldName, final CustomizationSettings cs, boolean generationMode,
            final GeneratorSettings settings, final Entity mainProduct) {
        return productComponents.stream()
                .reduce(Either.right(null), reduceStep(productFieldName, cs, generationMode, settings, mainProduct), combine)
                .map(r -> cs.technologyId);
    }

    private static final BinaryOperator<Either<String, ?>> combine = (a, b) -> a.flatMap(r -> b);

    private BiFunction<Either<String, ?>, Entity, Either<String, ?>> reduceStep(final String productFieldName,
            final CustomizationSettings cs, boolean generationMode, final GeneratorSettings settings, final Entity mainProduct) {
        return (acc, operationComponent) -> acc.flatMap(
                x -> customizeOperationProduct(operationComponent, mainProduct, productFieldName, cs, generationMode, settings));
    }

    private Either<String, Entity> customizeOperationProduct(final Entity operationComponent, final Entity mainProduct,
            final String productFieldName, final CustomizationSettings cs, boolean generationMode,
            final GeneratorSettings settings) {
        Entity currentProduct = operationComponent.getBelongsToField(productFieldName);
        if (generationMode) {
            currentProduct = currentProduct.getBelongsToField(ProductFields.PARENT);
        }
        Entity newProduct = productCustomizer.findOrCreate(currentProduct, mainProduct, cs.productSuffixes, settings);
        operationComponent.setField(productFieldName, newProduct);
        return saveOperationComponent(operationComponent);
    }

    private Either<String, Entity> saveOperationComponent(final Entity entity) {
        Entity savedEntity = entity.getDataDefinition().save(entity);
        if (savedEntity.isValid()) {
            return Either.right(savedEntity);
        }
        return Either.left(String.format(VALIDATION_ERR_TEMPLATE, entity.getDataDefinition().getName(), entity.getId()));
    }

    private static final class CustomizationSettings {

        public final TechnologyId technologyId;

        public final ProductSuffixes productSuffixes;

        private CustomizationSettings(final TechnologyId technologyId, final ProductSuffixes productSuffixes) {
            this.technologyId = technologyId;
            this.productSuffixes = productSuffixes;
        }
    }

    public Either<String, Optional<OutputProductComponentId>> prepareMainTechnologyProduct(final Entity technology,
            final Entity newProduct) {
        // O R D E R _ M A T T E R S !
        // Main output product won't be found after we override technology's product. We need to fetch them before
        // do any product updates. Have no idea how we actually could avoid such UGLY quirk.
        Optional<Entity> maybeMainOpoc = technologyMainOutCompDataProvider.find(technology.getId());
        technology.setField(TechnologyFields.PRODUCT, newProduct);
        Entity savedTechnology = technology.getDataDefinition().save(technology);
        if (savedTechnology.isValid()) {
            return customizeMainOutputComponent(newProduct, maybeMainOpoc);
        }
        return Either.left("Validation error while customizing technology product.");

    }

    private Either<String, Optional<OutputProductComponentId>> customizeMainOutputComponent(final Entity newMainProduct,
            final Optional<Entity> maybeMainOpoc) {
        return maybeMainOpoc
                .map(mainOpoc -> customizeMainOutputProduct(newMainProduct, mainOpoc)
                        .<Optional<OutputProductComponentId>> map(Optional::of))
                .orElse(Either.<String, Optional<OutputProductComponentId>> right(Optional.empty()));
    }

    private Either<String, OutputProductComponentId> customizeMainOutputProduct(final Entity newProduct, final Entity mainOpoc) {
        mainOpoc.setField(OperationProductOutComponentFields.PRODUCT, newProduct);
        return saveOperationComponent(mainOpoc).map(Entity::getId).map(OutputProductComponentId::new);
    }

}
