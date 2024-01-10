/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCounting.hooks.helpers;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.dto.OperationProductComponentEntityType;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationProductsExtractor {

    public static final String L_PRODUCT = "product";

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private TrackingOperationComponentBuilder trackingOperationComponentBuilder;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PluginManager pluginManager;

    /**
     * This method takes production tracking entity and returns all matching products wrapped in tracking operation components.
     * Results will be grouped by their model name, so you can easily distinct inputs products from output ones.
     *
     * @param productionTracking production tracking for which you want to extract products.
     * @return object representing tracking operation components grouped by their model name.
     */
    public TrackingOperationProducts getProductsByModelName(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        Iterable<Entity> allProducts = getProductsFromOrderOperation(productionTracking, order);

        return new TrackingOperationProducts(Multimaps.index(allProducts, EXTRACT_MODEL_NAME));
    }

    private Iterable<Entity> getProductsFromOrderOperation(final Entity productionTracking, final Entity order) {
        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        return getOperationProductComponents(order, technologyOperationComponent);
    }

    private List<Entity> getOperationProductComponents(final Entity order, final Entity technologyOperationComponent) {
        List<Entity> trackingOperationProductComponents = Lists.newArrayList();
        Map<OperationProductComponentEntityType, Set<Entity>> entityTypeWithAlreadyAddedProducts = Maps.newHashMap();

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                .getProductComponentQuantities(order);

        for (Entry<OperationProductComponentHolder, BigDecimal> productComponentQuantity : productComponentQuantities.asMap()
                .entrySet()) {
            OperationProductComponentHolder operationProductComponentHolder = productComponentQuantity.getKey();

            if (forEach(typeOfProductionRecording)) {
                Entity operationComponent = operationProductComponentHolder.getTechnologyOperationComponent();

                if (technologyOperationComponent == null) {
                    if (operationComponent != null) {
                        continue;
                    }
                } else {
                    if ((operationComponent == null) || !technologyOperationComponent.getId().equals(operationComponent.getId())) {
                        continue;
                    }
                }

            } else if (cumulated(typeOfProductionRecording)) {
                OperationProductComponentEntityType entityType = operationProductComponentHolder.getEntityType();
                Entity product = operationProductComponentHolder.getProduct();

                if ((product != null) && (entityType != null)) {
                    if (shouldSkipAddingProduct(operationProductComponentHolder, entityTypeWithAlreadyAddedProducts,
                            typeOfProductionRecording)) {

                        if (entityTypeWithAlreadyAddedProducts.containsKey(entityType)) {
                            continue;
                        } else {
                            entityTypeWithAlreadyAddedProducts.put(entityType, Sets.newHashSet());
                            continue;
                        }
                    } else {
                        if (entityTypeWithAlreadyAddedProducts.containsKey(entityType)) {
                            Set<Entity> alreadAddedProducts = entityTypeWithAlreadyAddedProducts.get(entityType);

                            alreadAddedProducts.add(product);

                            entityTypeWithAlreadyAddedProducts.put(entityType, alreadAddedProducts);
                        } else {
                            entityTypeWithAlreadyAddedProducts.put(entityType, Sets.newHashSet(product));
                        }

                    }

                }

            }

            Entity trackingOperationProductComponent = trackingOperationComponentBuilder
                    .fromOperationProductComponentHolder(operationProductComponentHolder);

            if (forEach(typeOfProductionRecording)) {
                Optional<Entity> mabyExist = trackingOperationProductComponents
                        .stream()
                        .filter(toc -> toc.getBelongsToField(L_PRODUCT).getId()
                                .equals(trackingOperationProductComponent.getBelongsToField(L_PRODUCT).getId())).findAny();
                if (!mabyExist.isPresent()) {
                    trackingOperationProductComponents.add(trackingOperationProductComponent);
                }
            } else {
                trackingOperationProductComponents.add(trackingOperationProductComponent);

            }
        }

        if (pluginManager.isPluginEnabled("productFlowThruDivision")) {
            for (Entity trackingOperationProductComponent : trackingOperationProductComponents) {
                if (ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT.equals(trackingOperationProductComponent.getDataDefinition().getName())) {

                    SearchCriteriaBuilder scb = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER, BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                            .find()
                            .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                            .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, trackingOperationProductComponent.getBelongsToField(L_PRODUCT)))
                            .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE, ProductionCountingQuantityRole.USED.getStringValue()))
                            .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL, ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()));

                    if (Objects.nonNull(technologyOperationComponent)) {
                        scb.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent));
                    }

                    List<Entity> pcqs = scb.list().getEntities();

                    List<Entity> reservations = Lists.newArrayList();
                    for (Entity productionCountingQuantity : pcqs) {
                        List<Entity> orderProductResourceReservations = productionCountingQuantity.getHasManyField("orderProductResourceReservations");
                        for (Entity orderProductResourceReservation : orderProductResourceReservations) {
                            Entity reservation = dataDefinitionService.get("productFlowThruDivision", "trackingProductResourceReservation").create();
                            reservation.setField("trackingOperationProductInComponent", trackingOperationProductComponent);
                            reservation.setField("orderProductResourceReservation", orderProductResourceReservation);
                            reservation.setField("priority", orderProductResourceReservation.getIntegerField("priority"));
                            reservations.add(reservation);
                        }
                    }

                    trackingOperationProductComponent.setField("resourceReservations", reservations);
                }
            }
        }

        return trackingOperationProductComponents;
    }

    private boolean shouldSkipAddingProduct(OperationProductComponentHolder operationProductComponentHolder,
                                            Map<OperationProductComponentEntityType, Set<Entity>> entityTypeWithAlreadyAddedProducts,
                                            String typeOfProductionRecording) {

        if (cumulated(typeOfProductionRecording)
                && operationProductComponentHolder.getProductMaterialType().getStringValue()
                .equals(TechnologyService.L_02_INTERMEDIATE)) {
            return true;
        }

        OperationProductComponentEntityType entityType = operationProductComponentHolder.getEntityType();
        Entity product = operationProductComponentHolder.getProduct();

        return entityTypeWithAlreadyAddedProducts.containsKey(entityType)
                && entityTypeWithAlreadyAddedProducts.get(entityType).contains(product);
    }

    private void add(List<Entity> trackingOperationProductComponents, Entity operationComponent) {
        Entity trackingOperationProductComponent = trackingOperationComponentBuilder
                .fromOperationProductComponent(operationComponent);
        trackingOperationProductComponents.add(trackingOperationProductComponent);
    }

    private boolean cumulated(String typeOfProductionRecording) {
        return TypeOfProductionRecording.CUMULATED.getStringValue().equals(typeOfProductionRecording);
    }

    private boolean forEach(String typeOfProductionRecording) {
        return TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording);
    }

    public static class TrackingOperationProducts {

        private final Multimap<String, Entity> operationProductsByModelName;

        protected TrackingOperationProducts(final Multimap<String, Entity> operationProductsByModelName) {
            this.operationProductsByModelName = operationProductsByModelName;
        }

        public List<Entity> getInputComponents() {
            return copyOf(ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT);
        }

        public List<Entity> getOutputComponents() {
            return copyOf(ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT);
        }

        private List<Entity> copyOf(final String key) {
            return Lists.newArrayList(operationProductsByModelName.get(key));
        }
    }

    private static final Function<Entity, String> EXTRACT_MODEL_NAME = from -> from.getDataDefinition().getName();

}
