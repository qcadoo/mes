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
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.domain.OperationId;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.mes.technologies.tree.builder.api.TechnologyOperationComponent;
import com.qcadoo.mes.technologies.tree.domain.TechnologyOperationId;
import com.qcadoo.mes.technologiesGenerator.GeneratorSettings;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorContextFields;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorTreeNodeFields;
import com.qcadoo.mes.technologiesGenerator.constants.TechnologiesGeneratorConstants;
import com.qcadoo.mes.technologiesGenerator.dataProvider.GeneratorContextDataProvider;
import com.qcadoo.mes.technologiesGenerator.dataProvider.TechnologyStructureTreeDataProvider;
import com.qcadoo.mes.technologiesGenerator.domain.ContextId;
import com.qcadoo.mes.technologiesGenerator.domain.ProductInfo;
import com.qcadoo.mes.technologiesGenerator.domain.TechnologyStructureNode;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

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

    @Transactional
    public Either<String, ContextId> generate(final Entity context, final GeneratorSettings settings) {
        try {
            return performGeneration(context, settings);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("A terrible error occurred during technologies structure generation", e);
            }
            return Either.left("An unexpected error occurred");
        }
    }

    private Either<String, ContextId> performGeneration(final Entity context, final GeneratorSettings settings) {
        Entity technology = context.getBelongsToField(GeneratorContextFields.TECHNOLOGY);
        TechnologyId technologyId = new TechnologyId(technology.getId());
        ContextId contextId = new ContextId(context.getId());
        Either<String, TechnologyStructureNode> mRoot = tryBuildStructure(settings, technologyId, contextId);
        Either<String, Entity> generationResults = mRoot.flatMap(root -> regenerateNodes(context, root).flatMap(
                x -> markContextAsGenerated(contextId)));
        logResults(generationResults);
        return generationResults.map(Entity::getId).map(ContextId::new);
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
        Entity entity = dataDefinition.create();
        setUpChildrenField(node, generatorContext, dataDefinition, entity);

        ProductInfo productInfo = node.getProductInfo();
        setBelongsToField(entity, GeneratorTreeNodeFields.PRODUCT, productInfo.getProduct().get());
        setUpProductTechnologyFields(entity, productInfo);
        setBelongsToField(entity, GeneratorTreeNodeFields.GENERATOR_CONTEXT, generatorContext);
        setUpOperationField(entity, productInfo);
        setUpDivisionField(entity, productInfo);
        setUpTechnologyGeneratorAndPerformance(entity,productInfo);

        entity.setField(GeneratorTreeNodeFields.QUANTITY, productInfo.getQuantity());
        entity.setField(GeneratorTreeNodeFields.ENTITY_TYPE, node.getType().getStringValue());
        return entity.getDataDefinition().save(entity);
    }

    private void setUpProductTechnologyFields(final Entity entity, final ProductInfo productInfo) {
        Long prodTechnologyId = productInfo.getProductTechnology().map(TechnologyId::get).orElse(null);
        setBelongsToField(entity, GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, prodTechnologyId);
        setBelongsToField(entity, GeneratorTreeNodeFields.ORIGINAL_TECHNOLOGY,
                productInfo.getOriginalTechnology().map(TechnologyId::get).orElse(prodTechnologyId));
    }

    private void setUpOperationField(final Entity entity, final ProductInfo productInfo) {
        OperationId operationId = productInfo.getOperation();
        setBelongsToField(entity, GeneratorTreeNodeFields.OPERATION, operationId.get());
    }

    private void setUpDivisionField(final Entity entity, final ProductInfo productInfo) {
        TechnologyOperationId tocId = productInfo.getTocId();
        if (tocId != null) {
            Entity toc = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(tocId.get());
            if (toc != null) {
                setBelongsToField(entity, GeneratorTreeNodeFields.DIVISION,
                        toc.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
            }
        }
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

    private DataDefinition getGeneratorTreeNodeDD() {
        return dataDefinitionService.get(TechnologiesGeneratorConstants.PLUGIN_IDENTIFIER,
                TechnologiesGeneratorConstants.MODEL_GENERATOR_TREE_NODE);
    }

    private void setUpTechnologyGeneratorAndPerformance(final Entity entity, final ProductInfo productInfo) {
        TechnologyOperationId tocId = productInfo.getTocId();
        if (tocId != null) {
            Entity tech = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(tocId.get()).getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
            if (tech != null) {
                setBelongsToField(entity, GeneratorTreeNodeFields.TECHNOLOGY_GROUP,
                        tech.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP));
                entity.setField(GeneratorTreeNodeFields.STANDARD_PERFORMANCE_TECHNOLOGY,tech.getDecimalField(TechnologyFields.STANDARD_PERFORMANCE_TECHNOLOGY));
            }
        }
    }

}
