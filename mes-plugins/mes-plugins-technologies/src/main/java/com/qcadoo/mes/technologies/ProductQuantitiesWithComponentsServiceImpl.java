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
package com.qcadoo.mes.technologies;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.SizeFields;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ProductQuantitiesWithComponentsServiceImpl implements ProductQuantitiesWithComponentsService {

    @Autowired
    ProductQuantitiesService productQuantitiesService;

    @Autowired
    ProductStructureTreeService productStructureTreeService;

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Override
    public ProductQuantitiesHolder getProductComponentQuantities(final Entity technology, final BigDecimal givenQuantity) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        OperationProductComponentWithQuantityContainer productQuantities = getProductComponentWithQuantitiesForTechnology(
                technology, null, givenQuantity, operationRuns, nonComponents);

        return new ProductQuantitiesHolder(productQuantities, operationRuns);
    }

    @Override
    public OperationProductComponentWithQuantityContainer getProductComponentWithQuantitiesForTechnology(final Entity technology, final Entity product,
                                                                                                         final BigDecimal givenQuantity, final Map<Long, BigDecimal> operationRuns,
                                                                                                         final Set<OperationProductComponentHolder> nonComponents) {
        OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer = new OperationProductComponentWithQuantityContainer();

        operationProductComponentWithQuantityContainer.setOrderedProduct(product);

        if (Objects.nonNull(product)) {
            Entity size = product.getBelongsToField(ProductFields.SIZE);

            if (Objects.nonNull(size)) {
                List<Entity> sizeGroups = size.getHasManyField(SizeFields.SIZE_GROUPS);

                if (!sizeGroups.isEmpty()) {
                    operationProductComponentWithQuantityContainer.setSizeGroups(sizeGroups);
                }
            }
        }

        EntityTree operationComponents = productStructureTreeService.getOperationComponentsFromTechnology(technology);

        technology.setField(TechnologyFields.OPERATION_COMPONENTS,
                EntityTreeUtilsService.getDetachedEntityTree(operationComponents));

        Entity root = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

        Map<Long, Entity> entitiesById = Maps.newLinkedHashMap();

        for (Entity entity : operationComponents) {
            entitiesById.put(entity.getId(), entity);
        }

        if (Objects.nonNull(root)) {
            productQuantitiesService.preloadProductQuantitiesAndOperationRuns(operationComponents,
                    operationProductComponentWithQuantityContainer, operationRuns);
            productQuantitiesService.traverseProductQuantitiesAndOperationRuns(technology, entitiesById, givenQuantity, root,
                    null, operationProductComponentWithQuantityContainer, nonComponents, operationRuns);
        }

        return operationProductComponentWithQuantityContainer;
    }

    @Override
    public Map<OperationProductComponentHolder, BigDecimal> getNeededProductQuantitiesByOPC(final Entity technology,
                                                                                            final BigDecimal givenQuantity, final MrpAlgorithm mrpAlgorithm) {
        return getNeededProductQuantitiesByOPC(technology, null, givenQuantity, mrpAlgorithm);
    }

    @Override
    public Map<OperationProductComponentHolder, BigDecimal> getNeededProductQuantitiesByOPC(final Entity technology,
                                                                                            final Entity product, final BigDecimal givenQuantity, final MrpAlgorithm mrpAlgorithm) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        OperationProductComponentWithQuantityContainer productComponentWithQuantities = getProductComponentWithQuantitiesForTechnology(
                technology, product, givenQuantity, operationRuns, nonComponents);

        OperationProductComponentWithQuantityContainer allWithSameEntityType = productComponentWithQuantities
                .getAllWithSameEntityType(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);

        if (mrpAlgorithm.equals(MrpAlgorithm.ALL_PRODUCTS_IN)) {
            return getOperationProductComponentWithQuantities(allWithSameEntityType, nonComponents, false, false);
        } else if (mrpAlgorithm.equals(MrpAlgorithm.ONLY_COMPONENTS)) {
            return getOperationProductComponentWithQuantities(allWithSameEntityType, nonComponents, true, false);
        } else if (mrpAlgorithm.equals(MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS)) {
            return getOperationProductComponentWithQuantities(allWithSameEntityType, nonComponents, false, false);
        } else {
            return getOperationProductComponentWithQuantities(allWithSameEntityType, nonComponents, true, true);
        }
    }

    private Map<OperationProductComponentHolder, BigDecimal> getOperationProductComponentWithQuantities(
            final OperationProductComponentWithQuantityContainer productComponentWithQuantities,
            final Set<OperationProductComponentHolder> nonComponents, final boolean onlyComponents, final boolean onlyMaterials) {
        Map<OperationProductComponentHolder, BigDecimal> productWithQuantities = Maps.newHashMap();

        for (Map.Entry<OperationProductComponentHolder, BigDecimal> productComponentWithQuantity : productComponentWithQuantities
                .asMap().entrySet()) {
            OperationProductComponentHolder operationProductComponentHolder = productComponentWithQuantity.getKey();

            if (onlyComponents && nonComponents.contains(operationProductComponentHolder)) {
                continue;
            }

            if (onlyMaterials) {
                Entity product = operationProductComponentHolder.getProduct();

                if (hasAcceptedMasterTechnology(product)) {
                    continue;
                }
            }

            addOPCQuantitiesToList(productComponentWithQuantity, productWithQuantities);
        }

        return productWithQuantities;
    }

    private boolean hasAcceptedMasterTechnology(final Entity product) {
        Entity masterTechnology = getTechnologyDD().find()
                .add(SearchRestrictions.and(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product),
                        (SearchRestrictions.eq("state", "02accepted"))))
                .add(SearchRestrictions.eq(TechnologyFields.MASTER, true)).setMaxResults(1).uniqueResult();

        return Objects.nonNull(masterTechnology);
    }

    public void addOPCQuantitiesToList(final Map.Entry<OperationProductComponentHolder, BigDecimal> productComponentWithQuantity,
                                       final Map<OperationProductComponentHolder, BigDecimal> productWithQuantities) {
        OperationProductComponentHolder operationProductComponentHolder = productComponentWithQuantity.getKey();

        BigDecimal quantity = productComponentWithQuantity.getValue();

        productWithQuantities.put(operationProductComponentHolder, quantity);
    }

    @Override
    public Map<OperationProductComponentHolder, BigDecimal> getNeededProductQuantities(final Entity technology,
                                                                                       final Entity product, final BigDecimal plannedQuantity) {
        String entityType = product.getStringField(ProductFields.ENTITY_TYPE);

        if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(entityType)) {
            return getNeededProductQuantitiesByOPC(technology, product, plannedQuantity, MrpAlgorithm.ONLY_COMPONENTS);
        } else {
            return getNeededProductQuantitiesByOPC(technology, plannedQuantity, MrpAlgorithm.ONLY_COMPONENTS);
        }
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
    }

}
