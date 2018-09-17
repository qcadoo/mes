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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.qcadoo.mes.basic.product.domain.ProductId;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.domain.TechnologyId;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchQueryBuilder;

@Service
public class DefaultTechnologiesProvider {

    private static final String PARENT_PROD_MASTER_TECHS_QUERY = "select  \n"
            + "p.id as prodId,                                            \n"
            + "pt.id as techId                                            \n"
            + "from #technologies_operationProductInComponent opic        \n"
            + "inner join opic.operationComponent toc                     \n"
            + "inner join toc.technology tech                             \n"
            + "inner join opic.product p                                  \n"
            + "inner join p.parent as pp                                  \n"
            + "inner join pp.technologies as pt                           \n"
            + "where                                                      \n"
            + "tech.id = :technologyId                                    \n"
            + "and                                                        \n"
            + "p.id not in :excludedProductIds                            \n"
            + "and                                                        \n"
            + "pt.master = true                                           \n";

    private static final String PROD_TECHS_QUERY = "select                \n"
            + "p.id as prodId,                                            \n"
            + "t.id as techId                                             \n"
            + "from #technologies_operationProductInComponent opic        \n"
            + "inner join opic.operationComponent toc                     \n"
            + "inner join toc.technology tech                             \n"
            + "inner join opic.product p                                  \n"
            + "inner join p.technologies t                                \n"
            + "where                                                      \n"
            + "tech.id = :technologyId                                    \n"
            + "and                                                        \n"
            + "t.master = true                                            \n";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public ImmutableMap<ProductId, TechnologyId> forAllInputProductsFrom(final TechnologyId technologyId) {
        ImmutableMap.Builder<ProductId, TechnologyId> builder = ImmutableMap.builder();
        ImmutableMap<ProductId, TechnologyId> masterTechnologies = findProdMasterTechnologies(technologyId);
        builder.putAll(masterTechnologies);
        builder.putAll(findParentProdMasterTechnologies(technologyId, masterTechnologies.keySet()));
        return builder.build();
    }

    private ImmutableMap<ProductId, TechnologyId> findParentProdMasterTechnologies(final TechnologyId technologyId,
            final Set<ProductId> excludedProducts) {
        SearchQueryBuilder sqb = getInputProductComponentDD().find(PARENT_PROD_MASTER_TECHS_QUERY);
        sqb.setLong("technologyId", technologyId.get());
        Set<Long> excludedProductIds = excludedProducts.stream().map(ProductId::get).collect(Collectors.toSet());
        excludedProductIds.add(-1L);
        sqb.setParameterList("excludedProductIds", excludedProductIds);
        List<Entity> projections = sqb.list().getEntities();
        return technologiesMapFrom(projections);
    }

    private ImmutableMap<ProductId, TechnologyId> findProdMasterTechnologies(final TechnologyId technologyId) {
        SearchQueryBuilder sqb = getInputProductComponentDD().find(PROD_TECHS_QUERY);
        sqb.setLong("technologyId", technologyId.get());
        List<Entity> projections = sqb.list().getEntities();
        return technologiesMapFrom(projections);
    }

    private ImmutableMap<ProductId, TechnologyId> technologiesMapFrom(final List<Entity> projections) {
        Multimap<ProductId, Entity> projectionsByProduct = Multimaps.index(projections,
                p -> new ProductId((Long) p.getField("prodId")));
        Map<ProductId, Optional<TechnologyId>> technologiesByProduct = Maps.transformValues(projectionsByProduct.asMap(),
                this::flattenProjectionsToSingleTechId);
        Map<ProductId, Optional<TechnologyId>> nonEmptyTechs = Maps.filterValues(technologiesByProduct, Optional::isPresent);
        return ImmutableMap.copyOf(Maps.transformValues(nonEmptyTechs, Optional::get));
    }

    private Optional<TechnologyId> flattenProjectionsToSingleTechId(final Collection<Entity> projections) {
        return Optional.ofNullable(Iterables.getFirst(projections, null)).map(p -> new TechnologyId((Long) p.getField("techId")));
    }

    private DataDefinition getInputProductComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

}
