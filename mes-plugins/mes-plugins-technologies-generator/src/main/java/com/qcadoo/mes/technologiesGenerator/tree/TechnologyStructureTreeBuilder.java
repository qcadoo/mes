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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.technologies.domain.OperationProductInComponentId;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.mes.technologies.tree.domain.TechnologyOperationId;
import com.qcadoo.mes.technologiesGenerator.GeneratorSettings;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorTreeNodeFields;
import com.qcadoo.mes.technologiesGenerator.dataProvider.TechnologyStructureTreeDataProvider;
import com.qcadoo.mes.technologiesGenerator.domain.ContextId;
import com.qcadoo.mes.technologiesGenerator.domain.ProductInfo;
import com.qcadoo.mes.technologiesGenerator.domain.TechnologyStructureNode;
import com.qcadoo.mes.technologiesGenerator.domain.TechnologyStructureNodeType;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyStructureTreeBuilder {

    @Autowired
    private TechnologyStructureTreeDataProvider technologyStructureTreeDataProvider;

    public Optional<TechnologyStructureNode> forTechnology(final TechnologyId technologyId,
            final Optional<TechnologyId> defaultTechnologyId, final ContextId contextId, final GeneratorSettings settings) {
        return buildGeneratorFor(technologyId, defaultTechnologyId, contextId, settings).build();
    }

    public Builder buildGeneratorFor(final TechnologyId technologyId, final Optional<TechnologyId> defaultTechnologyId,
            final ContextId contextId, final GeneratorSettings settings) {
        Multimap<Optional<TechnologyOperationId>, ProductInfo> intermediates = technologyStructureTreeDataProvider
                .findIntermediates(technologyId);
        Preconditions.checkState(intermediates.isEmpty() || intermediates.get(Optional.empty()).size() == 1,
                "Tree has to have exactly one root node!");
        Multimap<Optional<TechnologyOperationId>, ProductInfo> materialsAndComponents = technologyStructureTreeDataProvider
                .findMaterialsAndComponents(technologyId);
        Multimap<Optional<TechnologyOperationId>, ProductInfo> productBySizeGroups = technologyStructureTreeDataProvider
                .findProductBySizeGroups(technologyId);
        Map<TechnologyId, Entity> existingCustomizedNodes = technologyStructureTreeDataProvider
                .findExistingCustomizedNodes(contextId);

        return new Builder(contextId, technologyId, defaultTechnologyId, settings, intermediates, materialsAndComponents,
                productBySizeGroups, existingCustomizedNodes);
    }

    private final class Builder {

        private final ContextId contextId;

        private final TechnologyId technologyId;

        private final Optional<TechnologyId> defaultTechnologyId;

        private final GeneratorSettings settings;

        private final Multimap<Optional<TechnologyOperationId>, ProductInfo> intermediates;

        private final Multimap<Optional<TechnologyOperationId>, ProductInfo> materialsAndComponents;

        private final Multimap<Optional<TechnologyOperationId>, ProductInfo> productBySizeGroups;

        private final Map<TechnologyId, Entity> existingCustomizedNodes;

        private Builder(final ContextId contextId, final TechnologyId technologyId,
                final Optional<TechnologyId> defaultTechnologyId, final GeneratorSettings settings,
                final Multimap<Optional<TechnologyOperationId>, ProductInfo> intermediates,
                final Multimap<Optional<TechnologyOperationId>, ProductInfo> materialsAndComponents,
                final Multimap<Optional<TechnologyOperationId>, ProductInfo> productBySizeGroups,
                final Map<TechnologyId, Entity> existingCustomizedNodes) {
            this.contextId = contextId;
            this.technologyId = technologyId;
            this.defaultTechnologyId = defaultTechnologyId;
            this.settings = settings;
            this.intermediates = intermediates;
            this.materialsAndComponents = materialsAndComponents;
            this.productBySizeGroups = productBySizeGroups;
            this.existingCustomizedNodes = existingCustomizedNodes;
        }

        public Optional<TechnologyStructureNode> build() {
            Optional<ProductInfo> defaultRoot = Optional
                    .ofNullable(Iterables.getFirst(intermediates.get(Optional.empty()), null));
            Optional<ProductInfo> root = defaultRoot.map(r -> r.withOriginalProductTechnology(Optional.of(technologyId))
                    .withProductTechnology(Optional.of(technologyId)));

            if (!defaultTechnologyId.isPresent()) {
                Optional<TechnologyId> maybePrevTech = root.flatMap(this::findCustomizedTechFor);

                return forTechnology(maybePrevTech.orElse(technologyId), Optional.of(technologyId), contextId, settings);
            }

            return root.map(
                    n -> n.withProductTechnology(Optional.of(technologyId)).withOriginalProductTechnology(defaultTechnologyId))
                    .flatMap(this::buildNode);
        }

        public Optional<TechnologyStructureNode> buildNode(final ProductInfo productInfo) {
            TechnologyStructureNodeType type = TechnologyStructureNodeType.resolveFor(productInfo);

            if (type == TechnologyStructureNodeType.MATERIAL) {
                if (productInfo.getDifferentProductsInDifferentSizes()) {
                    return Optional.of(new TechnologyStructureNode(productInfo, type, getProductBySizes(productInfo)));
                } else {
                    return Optional.of(new TechnologyStructureNode(productInfo, type, Collections.emptyList()));
                }
            }
            if ((type == TechnologyStructureNodeType.COMPONENT || type == TechnologyStructureNodeType.CUSTOMIZED_COMPONENT)
                    && !productInfo.isIntermediate()) {
                if (settings.shouldFetchTechnologiesForComponents()) {
                    return generateTreeForComponent(productInfo);
                }

                return Optional.of(
                        new TechnologyStructureNode(productInfo, TechnologyStructureNodeType.MATERIAL, Collections.emptyList()));
            }

            List<TechnologyStructureNode> children = getDirectComponentsFor(productInfo).stream()
                    .filter(p -> !p.equals(productInfo)).map(this::buildNode).filter(Optional::isPresent).map(Optional::get)
                    .collect(Collectors.toList());

            TechnologyStructureNode generatedNode = new TechnologyStructureNode(productInfo, type, children);

            return Optional.of(generatedNode);
        }

        private List<TechnologyStructureNode> getProductBySizes(final ProductInfo productInfo) {
            Optional<OperationProductInComponentId> opicId = productInfo.getOpicId();
            Optional<TechnologyOperationId> tocId = Optional.of(productInfo.getTocId());

            if (opicId.isPresent()) {
                List<TechnologyStructureNode> productBySizes = Lists.newArrayList();

                filterProductBySizes(productBySizeGroups.get(tocId), opicId.get())
                        .forEach(p -> productBySizes.add(new TechnologyStructureNode(p,
                                TechnologyStructureNodeType.PRODUCT_BY_SIZE_GROUP, Collections.emptyList())));

                return productBySizes;
            } else {
                return Collections.emptyList();
            }
        }

        private List<ProductInfo> filterProductBySizes(final Collection<ProductInfo> productInfos,
                final OperationProductInComponentId operationProductInComponentId) {
            return productInfos.stream().filter(productInfo -> filterProductBySize(productInfo, operationProductInComponentId))
                    .collect(Collectors.toList());
        }

        private boolean filterProductBySize(final ProductInfo productInfo,
                final OperationProductInComponentId operationProductInComponentId) {
            return productInfo.getOpicId().isPresent()
                    && productInfo.getOpicId().get().get().equals(operationProductInComponentId.get());
        }

        private Optional<TechnologyStructureNode> generateTreeForComponent(final ProductInfo productInfo) {
            TechnologyId prodTech = findCustomizedTechFor(productInfo).orElseGet(() -> productInfo.getProductTechnology()
                    .orElseThrow(() -> new IllegalStateException("Missing technology for component's product")));

            return forTechnology(prodTech, productInfo.getProductTechnology(), contextId, settings).map(n -> n
                    .withProductTechnology(Optional.of(prodTech)).withOriginalTechnology(productInfo.getProductTechnology()));
        }

        private Optional<TechnologyId> findCustomizedTechFor(final ProductInfo productInfo) {
            Optional<Entity> maybeExistingNode = tryFindExistingCustomizedNode(productInfo);

            return maybeExistingNode.map(node -> {
                Entity technology = node.getBelongsToField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY);

                return new TechnologyId(technology.getId());
            });
        }

        private ImmutableList<ProductInfo> getDirectComponentsFor(final ProductInfo productInfo) {
            Optional<TechnologyOperationId> tocId = Optional.of(productInfo.getTocId());

            ImmutableList.Builder<ProductInfo> builder = ImmutableList.builder();

            builder.addAll(materialsAndComponents.get(tocId));
            builder.addAll(intermediates.get(tocId));

            return builder.build();
        }

        private Optional<Entity> tryFindExistingCustomizedNode(final ProductInfo productInfo) {
            return productInfo.getOriginalTechnology()
                    .flatMap(origTech -> Optional.ofNullable(existingCustomizedNodes.get(origTech)));
        }

    }

}
