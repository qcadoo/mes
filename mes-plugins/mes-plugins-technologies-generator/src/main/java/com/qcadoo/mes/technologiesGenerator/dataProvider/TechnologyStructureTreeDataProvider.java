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

    private static final String INPUT_PRODUCTS_HQL = "select                                      \n"
            + "toc.id as tocId,                                                                   \n"
            + "prod.id as prodId,                                                                 \n"
            + "opic.quantity as quantity,                                                         \n"
            + "operation.id as operationId,                                                       \n"
            + "'0' as isIntermediate                                                              \n"
            + "from                                                                               \n"
            + "#technologies_operationProductInComponent opic                                     \n"
            + "left join opic.product as prod                                                     \n"
            + "left join opic.operationComponent toc                                              \n"
            + "left join toc.technology as tech                                                   \n"
            + "left join toc.operation operation                                                  \n"
            + "left join toc.children as child                                                    \n"
            + "left join child.operationProductOutComponents as childOpoc                         \n"
            + "left join childOpoc.product as childProd                                           \n"
            + "where                                                                              \n"
            + "(toc.children is empty)                                                            \n"
            + "and                                                                                \n"
            + "tech.id = :techId                                                                  \n"
            + "order by prodId asc                                                                \n";

    private static final String OUTPUT_PRODUCTS_HQL = "select                                     \n"
            + "toc.id as tocId,                                                                   \n"
            + "parent.id as parentId,                                                             \n"
            + "prod.id as prodId,                                                                 \n"
            + "coalesce(opic.quantity, opoc.quantity) as quantity,                                \n"
            + "operation.id as operationId,                                                       \n"
            + "'1' as isIntermediate                                                              \n"
            + "from                                                                               \n"
            + "#technologies_operationProductOutComponent opoc                                    \n"
            + "left join opoc.product as prod                                                     \n"
            + "left join opoc.operationComponent as toc                                           \n"
            + "left join toc.technology as tech                                                   \n"
            + "left join toc.operation operation                                                  \n"
            + "left join toc.parent as parent                                                     \n"
            + "left join parent.operationProductInComponents as opic                              \n"
            + "left join opic.product as parentInputProd                                          \n"
            + "where                                                                              \n"
            + "(                                                                                  \n"
            + "    (toc.parent is null and tech.product.id = prod.id)                             \n"
            + "     or                                                                            \n"
            + "    (toc.parent is not null and parentInputProd.id = prod.id)                      \n"
            + ")                                                                                  \n"
            + "and                                                                                \n"
            + "tech.id = :techId                                                                  \n"
            + "order by prodId asc                                                                \n";

    private static final String inComponentHQL = "select DISTINCT                                 \n"
            + "opic.id as opicId,                                                                 \n"
            + "toc.id as tocId,                                                                   \n"
            + "parentToc.id as parentId,                                                          \n"
            + "inputProd.id as prodId,                                                            \n"
            + "opic.quantity as quantity,                                                         \n"
            + "opic.givenUnit as givenUnit,                                                       \n"
            + "technologyInputProductType.id as technologyInputProductTypeId,                     \n"
            + "operation.id as operationId,                                                       \n"
            + "(select count(*) from children c where c.parent = toc.id) as childrenCnt,          \n"
            + "(                                                                                  \n"
            + "    select count(*) from                                                           \n"
            + "    #technologies_operationProductOutComponent opoc                                \n"
            + "    left join opoc.operationComponent oc                                           \n"
            + "    left join oc.technology as tech                                                \n"
            + "    left join oc.parent par                                                        \n"
            + "    where                                                                          \n"
            + "    opoc.product = inputProd and par.id = toc.id                                   \n"
            + ") as isIntermediate                                                                \n"
            + "from                                                                               \n"
            + "#technologies_operationProductInComponent opic                                     \n"
            + "left join opic.product as inputProd                                                \n"
            + "left join opic.operationComponent toc                                              \n"
            + "left join opic.technologyInputProductType technologyInputProductType               \n"
            + "left join toc.operation operation                                                  \n"
            + "left join toc.operationProductOutComponents opoc                                   \n"
            + "left join opoc.product outputProd                                                  \n"
            + "left join toc.technology tech                                                      \n"
            + "left join toc.parent as parentToc                                                  \n"
            + "left join toc.children as children                                                 \n"
            + "where                                                                              \n"
            + "tech.id = :techId order by prodId asc                                              \n";

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

        return findAndGroup(inComponentHQL, technologyId, "tocId", defaultTechnologies, true);
    }

    private Multimap<Optional<TechnologyOperationId>, ProductInfo> findAndGroup(final String hqlQuery, final TechnologyId techId,
            final String groupBy, final Map<ProductId, TechnologyId> defaultTechnologies, boolean filterIntermediate) {
        List<Entity> entities = getGeneratorTreeNodeDD().find(hqlQuery).setLong("techId", techId.get()).list().getEntities();

        if (filterIntermediate) {
            entities = entities.stream().filter(p -> (Long) p.getField("isIntermediate") == 0l).collect(Collectors.toList());
        }

        Multimap<Optional<TechnologyOperationId>, Entity> groupedProjections = Multimaps.index(entities,
                (e) -> Optional.ofNullable((Long) e.getField(groupBy)).map(TechnologyOperationId::new));

        return ImmutableMultimap
                .copyOf(Multimaps.transformValues(groupedProjections, p -> buildProductInfo(p, defaultTechnologies)));
    }

    private ProductInfo buildProductInfo(final Entity projection, final Map<ProductId, TechnologyId> defaultTechnologies) {
        TechnologyOperationId tocId = new TechnologyOperationId((Long) projection.getField("tocId"));
        Optional<TechnologyOperationId> parentId = Optional.ofNullable((Long) projection.getField("parentId"))
                .map(TechnologyOperationId::new);
        ProductId productId = buildProduct(projection);
        Optional<TechnologyId> prodTechnology = Optional.ofNullable(defaultTechnologies.get(productId));
        Optional<TechnologyInputProductTypeId> technologyInputProductType = Optional
                .ofNullable((Long) projection.getField("technologyInputProductTypeId")).map(TechnologyInputProductTypeId::new);
        OperationId operation = buildOperation(projection);
        BigDecimal quantity = projection.getDecimalField("quantity");
        boolean isIntermediate = projection.getBooleanField("isIntermediate");
        String givenUnit = projection.getStringField("givenUnit");

        return new ProductInfo(tocId, parentId, productId, quantity, prodTechnology, prodTechnology, technologyInputProductType,
                operation, isIntermediate, givenUnit);
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
