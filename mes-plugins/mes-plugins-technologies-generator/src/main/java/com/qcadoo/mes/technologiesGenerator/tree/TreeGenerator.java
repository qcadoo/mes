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
package com.qcadoo.mes.technologiesGenerator.tree;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.domain.OperationId;
import com.qcadoo.mes.technologies.domain.SizeGroupId;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.mes.technologies.domain.TechnologyInputProductTypeId;
import com.qcadoo.mes.technologies.tree.domain.TechnologyOperationId;
import com.qcadoo.mes.technologiesGenerator.GeneratorSettings;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorContextFields;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorTreeNodeFields;
import com.qcadoo.mes.technologiesGenerator.constants.TechnologiesGeneratorConstants;
import com.qcadoo.mes.technologiesGenerator.dataProvider.GeneratorContextDataProvider;
import com.qcadoo.mes.technologiesGenerator.dataProvider.TechnologyStructureTreeDataProvider;
import com.qcadoo.mes.technologiesGenerator.domain.ContextId;
import com.qcadoo.mes.technologiesGenerator.domain.OperationProductKey;
import com.qcadoo.mes.technologiesGenerator.domain.ProductInfo;
import com.qcadoo.mes.technologiesGenerator.domain.TechnologyStructureNode;
import com.qcadoo.mes.technologiesGenerator.domain.TechnologyStructureNodeType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class TreeGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(TreeGenerator.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyStructureTreeDataProvider technologyStructureTreeDataProvider;

    @Autowired
    private TechnologyStructureTreeBuilder technologyStructureTreeBuilder;

    @Autowired
    private GeneratorContextDataProvider generatorContextDataProvider;

    @Autowired
    private TechnologyService technologyService;

    @Transactional
    public Either<String, ContextId> generate(final Entity context, final GeneratorSettings settings, boolean applyCustomized) {
        try {
            return performGeneration(context, settings, applyCustomized);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("A terrible error occurred during technologies structure generation", e);
            }

            return Either.left("An unexpected error occurred");
        }
    }

    private Either<String, ContextId> performGeneration(final Entity context, final GeneratorSettings settings,
            boolean applyCustomized) {
        Entity technology = context.getBelongsToField(GeneratorContextFields.TECHNOLOGY);

        TechnologyId technologyId = new TechnologyId(technology.getId());
        ContextId contextId = new ContextId(context.getId());

        Map<OperationProductKey, Long> customizedOperationProductTechnologies = getCustomizedTechnologies(context,
                applyCustomized);

        Either<String, TechnologyStructureNode> mRoot = tryBuildStructure(settings, technologyId, contextId);
        Either<String, Entity> generationResults = mRoot
                .flatMap(root -> regenerateNodes(context, root).flatMap(x -> markContextAsGenerated(contextId)));

        if (applyCustomized) {
            updateNodesToCustomized(context, customizedOperationProductTechnologies);
        }

        logResults(generationResults);

        return generationResults.map(Entity::getId).map(ContextId::new);
    }

    private void updateNodesToCustomized(Entity context, Map<OperationProductKey, Long> customizedOperationProductTechnologies) {
        customizedOperationProductTechnologies.forEach((opk, tech) -> {
            Entity node = getGeneratorTreeNodeDD().find()
                    .createAlias(GeneratorTreeNodeFields.PRODUCT, GeneratorTreeNodeFields.PRODUCT, JoinType.LEFT)
                    .add(SearchRestrictions.belongsTo(GeneratorTreeNodeFields.GENERATOR_CONTEXT, context))
                    .add(SearchRestrictions.eq(GeneratorTreeNodeFields.PRODUCT + ".id", opk.getProductId()))
                    .add(SearchRestrictions.eq(GeneratorTreeNodeFields.OPERATION + ".id", opk.getOperationId())).setMaxResults(1)
                    .uniqueResult();

            if (Objects.nonNull(node)) {
                node.setField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, tech);
                node.setField(GeneratorTreeNodeFields.ENTITY_TYPE,
                        TechnologyStructureNodeType.CUSTOMIZED_COMPONENT.getStringValue());
                node.getDataDefinition().save(node);
            }
        });
    }

    private Map<OperationProductKey, Long> getCustomizedTechnologies(final Entity context, boolean applyCustomized) {
        Map<OperationProductKey, Long> map = Maps.newHashMap();

        if (!applyCustomized) {
            return map;
        }

        List<Entity> nodes = getGeneratorTreeNodeDD().find()
                .add(SearchRestrictions.belongsTo(GeneratorTreeNodeFields.GENERATOR_CONTEXT, context))
                .add(SearchRestrictions.eq(GeneratorTreeNodeFields.ENTITY_TYPE,
                        TechnologyStructureNodeType.CUSTOMIZED_COMPONENT.getStringValue()))
                .list().getEntities();

        nodes.forEach(node -> {
            OperationProductKey operationProductKey = new OperationProductKey(
                    node.getBelongsToField(GeneratorTreeNodeFields.OPERATION).getId(),
                    node.getBelongsToField(GeneratorTreeNodeFields.PRODUCT).getId());
            map.put(operationProductKey, node.getBelongsToField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY).getId());
        });

        return map;
    }

    private Either<String, TechnologyStructureNode> tryBuildStructure(final GeneratorSettings settings,
            final TechnologyId technologyId, final ContextId contextId) {
        return technologyStructureTreeBuilder.forTechnology(technologyId, Optional.empty(), contextId, settings)
                .<Either<String, TechnologyStructureNode>> map(v -> Either.right(v))
                .orElse(Either.left("There was a problem with generating technologies structure tree"));
    }

    private void logResults(final Either<String, Entity> generationResults) {
        if (generationResults.isLeft() && LOG.isWarnEnabled()) {
            LOG.warn(generationResults.getLeft());
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("Technologies tree generated successfully");
        }
    }

    private Either<String, Entity> regenerateNodes(final Entity context, final TechnologyStructureNode root) {
        technologyStructureTreeDataProvider.deleteExistingNodes(context);

        Entity rootEntity = generate(root, context);

        return trySave(rootEntity);
    }

    private Either<String, Entity> markContextAsGenerated(final ContextId contextId) {
        return generatorContextDataProvider.find(contextId).map(context -> {
            context.setField(GeneratorContextFields.GENERATED, true);

            return trySave(context);
        }).orElseGet(() -> Either.left("Cannot find context entity, perhaps it was deleted during structure generation.."));
    }

    private Entity generate(final TechnologyStructureNode rootNode, final Entity generatorContext) {
        return buildEntity(rootNode, generatorContext, getGeneratorTreeNodeDD());
    }

    private Entity buildEntity(final TechnologyStructureNode node, final Entity generatorContext,
            final DataDefinition dataDefinition) {
        ProductInfo productInfo = node.getProductInfo();

        Entity entity = dataDefinition.create();

        setUpChildrenField(node, generatorContext, dataDefinition, entity);
        setBelongsToField(entity, GeneratorTreeNodeFields.GENERATOR_CONTEXT, generatorContext);
        setBelongsToField(entity, GeneratorTreeNodeFields.PRODUCT, productInfo.getProduct().get());
        setUpProductTechnologyFields(entity, productInfo);
        setUpTechnologyInputProductTypeField(entity, productInfo);
        entity.setField(GeneratorTreeNodeFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES,
                productInfo.getDifferentProductsInDifferentSizes());
        entity.setField(GeneratorTreeNodeFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE,
                productInfo.getVariousQuantitiesInProductsBySize());
        setUpOperationField(entity, productInfo);
        setUpDivisionField(entity, productInfo);
        setUpTechnologyGeneratorAndPerformance(entity, productInfo);
        entity.setField(GeneratorTreeNodeFields.QUANTITY, productInfo.getQuantity());
        entity.setField(GeneratorTreeNodeFields.ENTITY_TYPE, node.getType().getStringValue());
        entity.setField(GeneratorTreeNodeFields.UNIT, productInfo.getUnit());
        setUpSizeGroupField(entity, productInfo);

        return entity.getDataDefinition().save(entity);
    }

    private void setUpProductTechnologyFields(final Entity entity, final ProductInfo productInfo) {
        Long prodTechnologyId = productInfo.getProductTechnology().map(TechnologyId::get).orElse(null);

        setBelongsToField(entity, GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, prodTechnologyId);
        setBelongsToField(entity, GeneratorTreeNodeFields.ORIGINAL_TECHNOLOGY,
                productInfo.getOriginalTechnology().map(TechnologyId::get).orElse(prodTechnologyId));
    }

    private void setUpTechnologyInputProductTypeField(final Entity entity, final ProductInfo productInfo) {
        Long technologyInputProductTypeId = productInfo.getTechnologyInputProductType().map(TechnologyInputProductTypeId::get)
                .orElse(null);

        setBelongsToField(entity, GeneratorTreeNodeFields.TECHNOLOGY_INPUT_PRODUCT_TYPE, technologyInputProductTypeId);
    }

    private void setUpOperationField(final Entity entity, final ProductInfo productInfo) {
        OperationId operationId = productInfo.getOperation();

        setBelongsToField(entity, GeneratorTreeNodeFields.OPERATION, operationId.get());
    }

    private void setUpDivisionField(final Entity entity, final ProductInfo productInfo) {
        TechnologyOperationId tocId = productInfo.getTocId();

        if (Objects.nonNull(tocId)) {
            Entity toc = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                    .get(tocId.get());

            if (Objects.nonNull(toc)) {
                setBelongsToField(entity, GeneratorTreeNodeFields.DIVISION,
                        toc.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
            }
        }
    }

    private void setUpSizeGroupField(final Entity entity, final ProductInfo productInfo) {
        Long sizeGroupId = productInfo.getSizeGroup().map(SizeGroupId::get).orElse(null);

        setBelongsToField(entity, GeneratorTreeNodeFields.SIZE_GROUP, sizeGroupId);
    }

    private void setUpChildrenField(final TechnologyStructureNode node, final Entity generatorContext,
            final DataDefinition dataDefinition, final Entity entity) {
        List<Entity> children = node.getChildren().stream().map((n) -> buildEntity(n, generatorContext, dataDefinition))
                .collect(Collectors.toList());

        entity.setField(GeneratorTreeNodeFields.CHILDREN, children);
    }

    // These two methods below are aimed to enforce program's correctness by compiler. It prevents us from making such mistakes
    // like passing id/entity POJO wrappers instead of it's underlying values, and so on. Type safety rocks!
    // TODO dev_team: consider adding such strongly typed API to Entity. There are only pros..
    private void setBelongsToField(final Entity entity, final String fieldName, final Long id) {
        entity.setField(fieldName, id);
    }

    private void setBelongsToField(final Entity entity, final String fieldName, final Entity btFieldValue) {
        entity.setField(fieldName, btFieldValue);
    }

    private Either<String, Entity> trySave(final Entity entity) {
        DataDefinition dataDefinition = entity.getDataDefinition();

        Entity savedEntity = dataDefinition.save(entity);

        if (savedEntity.isValid()) {
            return Either.right(savedEntity);
        }

        return Either.left(String.format("Cannot save %s.%s because of validation errors", dataDefinition.getPluginIdentifier(),
                dataDefinition.getName()));
    }

    private void setUpTechnologyGeneratorAndPerformance(final Entity entity, final ProductInfo productInfo) {
        TechnologyOperationId tocId = productInfo.getTocId();

        if (Objects.nonNull(tocId)) {
            Entity tech = dataDefinitionService
                    .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                    .get(tocId.get()).getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);

            if (Objects.nonNull(tech)) {
                setBelongsToField(entity, GeneratorTreeNodeFields.TECHNOLOGY_GROUP,
                        tech.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP));
                entity.setField(GeneratorTreeNodeFields.STANDARD_PERFORMANCE,
                        technologyService.getStandardPerformance(tech).orElse(null));
            }
        }
    }

    private DataDefinition getGeneratorTreeNodeDD() {
        return dataDefinitionService.get(TechnologiesGeneratorConstants.PLUGIN_IDENTIFIER,
                TechnologiesGeneratorConstants.MODEL_GENERATOR_TREE_NODE);
    }

}
