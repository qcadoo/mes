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
package com.qcadoo.mes.technologiesGenerator.dataProvider;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static com.qcadoo.model.api.search.SearchRestrictions.idEq;
import static com.qcadoo.model.api.search.SearchRestrictions.isNotNull;
import static com.qcadoo.model.api.search.SearchRestrictions.isNull;
import static com.qcadoo.model.api.search.SearchRestrictions.neField;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.qcadoo.mes.basic.product.domain.ProductId;
import com.qcadoo.mes.technologies.domain.OperationId;
import com.qcadoo.mes.technologies.domain.OperationProductInComponentId;
import com.qcadoo.mes.technologies.domain.SizeGroupId;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.mes.technologies.domain.TechnologyInputProductTypeId;
import com.qcadoo.mes.technologies.tree.domain.TechnologyOperationId;
import com.qcadoo.mes.technologiesGenerator.constants.GeneratorTreeNodeFields;
import com.qcadoo.mes.technologiesGenerator.constants.TechnologiesGeneratorConstants;
import com.qcadoo.mes.technologiesGenerator.domain.ContextId;
import com.qcadoo.mes.technologiesGenerator.domain.ProductInfo;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;

@Service
public class TechnologyStructureTreeDataProvider {

    private static final String OUTPUT_PRODUCTS_HQL = "SELECT                                     \n"
            + "toc.id AS tocId,                                                                   \n"
            + "parent.id AS parentId,                                                             \n"
            + "prod.id AS prodId,                                                                 \n"
            + "coalesce(opic.quantity, opoc.quantity) AS quantity,                                \n"
            + "operation.id AS operationId,                                                       \n"
            + "'1' AS isIntermediate                                                              \n"
            + "FROM                                                                               \n"
            + "#technologies_operationProductOutComponent opoc                                    \n"
            + "LEFT JOIN opoc.product prod                                                        \n"
            + "LEFT JOIN opoc.operationComponent toc                                              \n"
            + "LEFT JOIN toc.technology tech                                                      \n"
            + "LEFT JOIN toc.operation operation                                                  \n"
            + "LEFT JOIN toc.parent parent                                                        \n"
            + "LEFT JOIN parent.operationProductInComponents opic                                 \n"
            + "LEFT JOIN opic.product parentInputProd                                             \n"
            + "WHERE                                                                              \n"
            + "(                                                                                  \n"
            + "    (toc.parent IS null AND tech.product.id = prod.id)                             \n"
            + "    OR                                                                             \n"
            + "    (toc.parent IS NOT null AND parentInputProd.id = prod.id)                      \n"
            + ")                                                                                  \n"
            + "AND                                                                                \n"
            + "tech.id = :techId                                                                  \n"
            + "ORDER BY prodId ASC                                                                \n";

    private static final String INPUT_COMPONENTS_HQL = "SELECT DISTINCT                           \n"
            + "opic.id AS opicId,                                                                 \n"
            + "toc.id AS tocId,                                                                   \n"
            + "parent.id AS parentId,                                                             \n"
            + "inputProd.id AS prodId,                                                            \n"
            + "opic.quantity AS quantity,                                                         \n"
            + "opic.givenUnit AS givenUnit,                                                       \n"
            + "technologyInputProductType.id AS technologyInputProductTypeId,                     \n"
            + "opic.differentProductsInDifferentSizes AS differentProductsInDifferentSizes,       \n"
            + "opic.variousQuantitiesInProductsBySize AS variousQuantitiesInProductsBySize,       \n"
            + "operation.id AS operationId,                                                       \n"
            + "(SELECT count(*) FROM children c WHERE c.parent = toc.id) AS childrenCnt,          \n"
            + "(                                                                                  \n"
            + "    SELECT count(*) FROM                                                           \n"
            + "    #technologies_operationProductOutComponent opoc                                \n"
            + "    LEFT JOIN opoc.operationComponent oc                                           \n"
            + "    LEFT JOIN oc.technology tech                                                   \n"
            + "    LEFT JOIN oc.parent par                                                        \n"
            + "    WHERE                                                                          \n"
            + "    opoc.product = inputProd AND par.id = toc.id                                   \n"
            + ") AS isIntermediate                                                                \n"
            + "FROM                                                                               \n"
            + "#technologies_operationProductInComponent opic                                     \n"
            + "LEFT JOIN opic.product inputProd                                                   \n"
            + "LEFT JOIN opic.operationComponent toc                                              \n"
            + "LEFT JOIN opic.technologyInputProductType technologyInputProductType               \n"
            + "LEFT JOIN toc.operation operation                                                  \n"
            + "LEFT JOIN toc.operationProductOutComponents opoc                                   \n"
            + "LEFT JOIN opoc.product outputProd                                                  \n"
            + "LEFT JOIN toc.technology tech                                                      \n"
            + "LEFT JOIN toc.parent parent                                                        \n"
            + "LEFT JOIN toc.children children                                                    \n"
            + "WHERE                                                                              \n"
            + "tech.id = :techId ORDER BY prodId ASC                                              \n";

    private static final String PRODUCT_BY_SIZES_HQL = "SELECT                                    \n"
            + "opic.id AS opicId,                                                                 \n"
            + "opic.variousQuantitiesInProductsBySize AS variousQuantitiesInProductsBySize,       \n"
            + "toc.id AS tocId,                                                                   \n"
            + "parent.id AS parentId,                                                             \n"
            + "operation.id AS operationId,                                                       \n"
            + "prod.id AS prodId,                                                                 \n"
            + "sizeGroup.id AS sizeGroupId,                                                       \n"
            + "pbsg.quantity AS quantity,                                                         \n"
            + "prod.unit AS givenUnit                                                             \n"
            + "FROM                                                                               \n"
            + "#technologies_productBySizeGroup pbsg                                              \n"
            + "LEFT JOIN pbsg.operationProductInComponent opic                                    \n"
            + "LEFT JOIN pbsg.sizeGroup sizeGroup                                                 \n"
            + "LEFT JOIN pbsg.product prod                                                        \n"
            + "LEFT JOIN opic.operationComponent toc                                              \n"
            + "LEFT JOIN toc.operation operation                                                  \n"
            + "LEFT JOIN toc.technology tech                                                      \n"
            + "LEFT JOIN toc.parent parent                                                        \n"
            + "WHERE                                                                              \n"
            + "tech.id = :techId ORDER BY prodId ASC                                              \n";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DefaultTechnologiesProvider defaultTechnologiesProvider;

    public Multimap<Optional<TechnologyOperationId>, ProductInfo> findIntermediates(final TechnologyId technologyId) {
        return findAndGroup(OUTPUT_PRODUCTS_HQL, technologyId, "parentId", ImmutableMap.of(), false);
    }

    public Multimap<Optional<TechnologyOperationId>, ProductInfo> findMaterialsAndComponents(final TechnologyId technologyId) {
        ImmutableMap<ProductId, TechnologyId> defaultTechnologies = defaultTechnologiesProvider
                .forAllInputProductsFrom(technologyId);

        return findAndGroup(INPUT_COMPONENTS_HQL, technologyId, "tocId", defaultTechnologies, true);
    }

    public Multimap<Optional<TechnologyOperationId>, ProductInfo> findProductBySizeGroups(final TechnologyId technologyId) {
        ImmutableMap<ProductId, TechnologyId> defaultTechnologies = defaultTechnologiesProvider
                .forAllInputProductsFrom(technologyId);

        return findAndGroup(PRODUCT_BY_SIZES_HQL, technologyId, "tocId", defaultTechnologies, false);
    }

    private Multimap<Optional<TechnologyOperationId>, ProductInfo> findAndGroup(final String hqlQuery, final TechnologyId techId,
            final String groupBy, final Map<ProductId, TechnologyId> defaultTechnologies, boolean filterIntermediate) {
        List<Entity> entities = getGeneratorTreeNodeDD().find(hqlQuery).setLong("techId", techId.get()).list().getEntities();

        if (filterIntermediate) {
            entities = entities.stream().filter(p -> (Long) p.getField("isIntermediate") == 0L).collect(Collectors.toList());
        }

        Multimap<Optional<TechnologyOperationId>, Entity> groupedProjections = Multimaps.index(entities,
                (e) -> Optional.ofNullable((Long) e.getField(groupBy)).map(TechnologyOperationId::new));

        return ImmutableMultimap
                .copyOf(Multimaps.transformValues(groupedProjections, p -> buildProductInfo(p, defaultTechnologies)));
    }

    private ProductInfo buildProductInfo(final Entity projection, final Map<ProductId, TechnologyId> defaultTechnologies) {
        Optional<OperationProductInComponentId> opicId = Optional.ofNullable((Long) projection.getField("opicId"))
                .map(OperationProductInComponentId::new);
        TechnologyOperationId tocId = new TechnologyOperationId((Long) projection.getField("tocId"));
        Optional<TechnologyOperationId> parentId = Optional.ofNullable((Long) projection.getField("parentId"))
                .map(TechnologyOperationId::new);
        ProductId productId = buildProduct(projection);
        Optional<TechnologyId> prodTechnology = Optional.ofNullable(defaultTechnologies.get(productId));
        Optional<TechnologyInputProductTypeId> technologyInputProductType = Optional
                .ofNullable((Long) projection.getField("technologyInputProductTypeId")).map(TechnologyInputProductTypeId::new);
        boolean differentProductsInDifferentSizes = projection.getBooleanField("differentProductsInDifferentSizes");
        boolean variousQuantitiesInProductsBySize = projection.getBooleanField("variousQuantitiesInProductsBySize");
        OperationId operation = buildOperation(projection);
        BigDecimal quantity = projection.getDecimalField("quantity");
        boolean isIntermediate = projection.getBooleanField("isIntermediate");
        String givenUnit = projection.getStringField("givenUnit");
        Optional<SizeGroupId> sizeGroup = Optional.ofNullable((Long) projection.getField("sizeGroupId")).map(SizeGroupId::new);

        return new ProductInfo(opicId, tocId, parentId, productId, quantity, prodTechnology, prodTechnology, technologyInputProductType,
                differentProductsInDifferentSizes, variousQuantitiesInProductsBySize, operation, isIntermediate, givenUnit, sizeGroup);
    }

    private OperationId buildOperation(final Entity projection) {
        Long operationId = (Long) projection.getField("operationId");

        return new OperationId(operationId);
    }

    private ProductId buildProduct(final Entity projection) {
        Long productId = (Long) projection.getField("prodId");

        return new ProductId(productId);
    }

    public Map<TechnologyId, Entity> findExistingCustomizedNodes(final ContextId contextId) {
        if (Objects.isNull(contextId)) {
            return ImmutableMap.of();
        }

        SearchCriteriaBuilder scb = prepareExistingNodeSearchCriteria(contextId);

        List<Entity> existingNodes = scb.list().getEntities();

        return ImmutableMap.copyOf(Maps.uniqueIndex(existingNodes,
                n -> new TechnologyId(n.getBelongsToField(GeneratorTreeNodeFields.ORIGINAL_TECHNOLOGY).getId())));
    }

    private SearchCriteriaBuilder prepareExistingNodeSearchCriteria(final ContextId contextId) {
        SearchCriteriaBuilder scb = getGeneratorTreeNodeDD().find();

        scb.add(isNotNull(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY));
        scb.add(isNotNull(GeneratorTreeNodeFields.ORIGINAL_TECHNOLOGY));
        scb.createCriteria(GeneratorTreeNodeFields.GENERATOR_CONTEXT, "generatorContext_alias", JoinType.INNER)
                .add(idEq(contextId.get()));
        scb.add(neField(GeneratorTreeNodeFields.PRODUCT_TECHNOLOGY, GeneratorTreeNodeFields.ORIGINAL_TECHNOLOGY));

        return scb;
    }

    public void deleteExistingNodes(final Entity savedContext) {
        DataDefinition nodesDD = getGeneratorTreeNodeDD();

        Entity rootNodeToBeDeleted = nodesDD.find().add(belongsTo(GeneratorTreeNodeFields.GENERATOR_CONTEXT, savedContext))
                .add(isNull(GeneratorTreeNodeFields.PARENT)).setProjection(alias(id(), "id")).setMaxResults(1).uniqueResult();

        if (Objects.nonNull(rootNodeToBeDeleted)) {
            nodesDD.delete((Long) rootNodeToBeDeleted.getField("id"));
        }
    }

    public Optional<Entity> tryFind(final Long nodeId) {
        return Optional.of(getGeneratorTreeNodeDD().get(nodeId));
    }

    private DataDefinition getGeneratorTreeNodeDD() {
        return dataDefinitionService.get(TechnologiesGeneratorConstants.PLUGIN_IDENTIFIER,
                TechnologiesGeneratorConstants.MODEL_GENERATOR_TREE_NODE);
    }

}
