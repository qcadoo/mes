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

import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.technologies.TechnologyNameAndNumberGenerator;
import com.qcadoo.mes.technologies.constants.QualityCardFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.dataProvider.TechnologyDataProvider;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.mes.technologiesGenerator.GeneratorSettings;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorTreeNodeFields;
import com.qcadoo.mes.technologiesGenerator.customization.product.ProductCustomizer;
import com.qcadoo.mes.technologiesGenerator.customization.product.domain.ProductSuffixes;
import com.qcadoo.mes.technologiesGenerator.dataProvider.TechnologyStructureTreeDataProvider;
import com.qcadoo.mes.technologiesGenerator.domain.TechnologyStructureNodeType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class TechnologyCustomizer {

    @Autowired
    private TechnologyStructureTreeDataProvider technologyStructureTreeDataProvider;

    @Autowired
    private ProductCustomizer productCustomizer;

    @Autowired
    private TechnologyNameAndNumberGenerator technologyNameAndNumberGenerator;

    @Autowired
    private TechnologyDataProvider technologyDataProvider;

    @Autowired
    private TechnologyProductsCustomizer technologyOperationProductsCustomizer;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Transactional
    public Optional<Either<String, TechnologyId>> customize(final Long nodeId, final Entity mainProduct,
            final GeneratorSettings settings, boolean generationMode) {
        Optional<Entity> maybeNode = technologyStructureTreeDataProvider.tryFind(nodeId);
        if (!maybeNode.isPresent()) {
            return Optional.of(Either.left(String.format("Cannot find generator node with id = '%s'", nodeId)));
        }
        Optional<Either<String, TechnologyId>> customizationResult = maybeNode.filter(this::isCustomizableNode).map(
                node -> customize(node, mainProduct, settings, generationMode));
        if (customizationResult.map(Either::isLeft).orElse(false)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return customizationResult;
    }


    public void addCustomizedProductToQualityCard(TechnologyId technologyId) {
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY)
                .get(technologyId.get());
        if (Objects.nonNull(technology.getBelongsToField(TechnologyFields.QUALITY_CARD))) {
            Entity qualityCard = technology.getBelongsToField(TechnologyFields.QUALITY_CARD);
            List<Entity> products = qualityCard.getManyToManyField(QualityCardFields.PRODUCTS);
            products.add(technology.getBelongsToField(TechnologyFields.PRODUCT));
            qualityCard.setField(QualityCardFields.PRODUCTS, products);
            qualityCard.getDataDefinition().save(qualityCard);
        }
    }

    private boolean isCustomizableNode(final Entity nodeEntity) {
        TechnologyStructureNodeType type = TechnologyStructureNodeType.of(nodeEntity);
        return type == TechnologyStructureNodeType.COMPONENT || type == TechnologyStructureNodeType.CUSTOMIZED_COMPONENT;
    }

    private Either<String, TechnologyId> customize(final Entity node, final Entity mainProduct, final GeneratorSettings settings, boolean generationMode) {
        Entity productTechnology = node.getBelongsToField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY);
        if (productTechnology == null) {
            return Either.left("Cannot find technology for product");
        }
        if (TechnologyStructureNodeType.of(node) == TechnologyStructureNodeType.CUSTOMIZED_COMPONENT && ! generationMode) {
            return Either.right(new TechnologyId(productTechnology.getId()));
        }
        return copy(productTechnology).flatMap(t -> customize(node, mainProduct, settings, t, generationMode));
    }

    private Either<String, TechnologyId> customize(final Entity node, final Entity mainProduct, final GeneratorSettings settings,
            final Entity technology, boolean generationMode) {
        TechnologyId technologyId = new TechnologyId(technology.getId());
        if (settings.shouldCreateAndSwitchProducts()) {
            Entity newProduct = resolveProduct(node, mainProduct, settings);
            return technologyOperationProductsCustomizer.customize(technologyId, mainProduct, newProduct, settings)
                    .flatMap(techId -> setupTechnologyNumberAndName(techId, newProduct))
                    .flatMap(techId -> updateNode(node, techId, Optional.of(newProduct)));
        } else if (node.getBelongsToField(GeneratorTreeNodeFields.PARENT) == null) {
            return technologyOperationProductsCustomizer.prepareMainTechnologyProduct(technology, mainProduct)
                    .map(x -> technologyId).flatMap(techId -> setupTechnologyNumberAndName(techId, mainProduct))
                    .flatMap(techId -> updateNode(node, techId, Optional.of(mainProduct)));
        } else {
            Entity nodeProduct = node.getBelongsToField(GeneratorTreeNodeFields.PRODUCT);
            return setupTechnologyNumberAndName(technology, nodeProduct).flatMap(
                    (techId) -> updateNode(node, techId, Optional.empty()));
        }
    }

    private Either<String, Entity> copy(final Entity technology) {
        DataDefinition technologyDD = technology.getDataDefinition();
        Entity copy = technologyDD.copy(technology.getId()).get(0);
        if (copy.isValid()) {
            return Either.right(copy);
        }
        return Either.left("Cannot copy technology due to validation errors.");
    }

    private Either<String, TechnologyId> setupTechnologyNumberAndName(final TechnologyId technologyId, final Entity product) {
        // I'm aware that loading & mapping the whole technology isn't cheap, but it will be far more easier to maintain if we
        // won't be depending on the operations' order. Trust me, avoiding issues with unwanted overriding some data by further
        // cascade saving is worth of it. Really.
        Optional<Either<String, TechnologyId>> setupResults = technologyDataProvider.tryFind(technologyId.get()).map(
                technology -> setupTechnologyNumberAndName(technology, product));
        return setupResults
                .orElseGet(() -> Either.left(String.format("Cannot find technology with id = '%s'", technologyId.get())));
    }

    private Either<String, TechnologyId> setupTechnologyNumberAndName(final Entity technology, final Entity product) {
        technology.setField(TechnologyFields.NUMBER, technologyNameAndNumberGenerator.generateNumber(product));
        technology.setField(TechnologyFields.NAME, technologyNameAndNumberGenerator.generateName(product));
        Entity savedTech = technology.getDataDefinition().save(technology);
        if (savedTech.isValid()) {
            return Either.right(new TechnologyId(savedTech.getId()));
        } else {
            return Either.left("Cannot setup technology name and number due to validation errors");
        }
    }

    private Either<String, TechnologyId> updateNode(final Entity node, final TechnologyId technologyId,
            final Optional<Entity> product) {
        product.ifPresent(p -> node.setField(GeneratorTreeNodeFields.PRODUCT, p));
        node.setField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, technologyId.get());
        node.setField(GeneratorTreeNodeFields.ENTITY_TYPE, TechnologyStructureNodeType.CUSTOMIZED_COMPONENT.getStringValue());

        Entity savedNode = node.getDataDefinition().save(node);
        if (savedNode.isValid()) {
            return Either.right(technologyId);
        }
        return Either.left("Cannot customize generator node due to validation errors");
    }

    private Entity resolveProduct(final Entity node, final Entity mainProduct, final GeneratorSettings settings) {
        if (node.getBelongsToField(GeneratorTreeNodeFields.PARENT) == null) {
            return mainProduct;
        }
        Entity product = node.getBelongsToField(GeneratorTreeNodeFields.PRODUCT);
        if (ProductFamilyElementType.from(product) == ProductFamilyElementType.PRODUCTS_FAMILY) {
            return productCustomizer.findOrCreate(product, mainProduct, ProductSuffixes.from(mainProduct), settings);
        }
        return product;
    }

}
