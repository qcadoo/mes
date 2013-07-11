/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.productionCounting.hooks;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductionTrackingHooks {

    private static final String L_PRODUCT = "product";

    private static final String L_OPERATION_COMPONENT = "operationComponent";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private ProductionTrackingStateChangeDescriber describer;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public void onCreate(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        setInitialState(productionTrackingDD, productionTracking);
    }

    public void onSave(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        generateData(productionTrackingDD, productionTracking);
        copyProductsFromOrderOperation(productionTrackingDD, productionTracking);
    }

    public void onCopy(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        setInitialState(productionTrackingDD, productionTracking);
        clearLaborAndMachineTime(productionTrackingDD, productionTracking);
    }

    private void setInitialState(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        stateChangeEntityBuilder.buildInitial(describer, productionTracking, ProductionTrackingState.DRAFT);
    }

    private void generateData(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        if (productionTracking.getField(ProductionTrackingFields.NUMBER) == null) {
            productionTracking.setField(ProductionTrackingFields.NUMBER, numberGeneratorService.generateNumber(
                    ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING));
        }
    }

    private void copyProductsFromOrderOperation(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (typeOfProductionRecording == null) {
            return;
        }

        boolean registerQuantityInProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT);
        boolean registerQuantityOutProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT);

        if (!registerQuantityInProduct && !registerQuantityOutProduct) {
            return;
        }

        if (shouldCopy(productionTracking, order, technologyOperationComponent)) {
            if (registerQuantityInProduct) {
                copyOperationProductComponents(productionTracking, order, technologyOperationComponent,
                        ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT);
            }
            if (registerQuantityOutProduct) {
                copyOperationProductComponents(productionTracking, order, technologyOperationComponent,
                        ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT);
            }
        }
    }

    private void copyOperationProductComponents(final Entity productionTracking, final Entity order,
            final Entity technologyOperationComponent, final String trackingOperationProductModelName) {
        List<Entity> trackingOperationProductComponents = Lists.newArrayList();

        String operationProductModel = null;
        String trackingOperationProductsFieldName = null;

        if (ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT.equals(trackingOperationProductModelName)) {
            operationProductModel = "operationProductInComponent";
            trackingOperationProductsFieldName = ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS;
        } else if (ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT
                .equals(trackingOperationProductModelName)) {
            operationProductModel = "operationProductOutComponent";
            trackingOperationProductsFieldName = ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS;
        }

        OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                .getProductComponentQuantities(asList(order));

        Set<Long> alreadyAddedProducts = Sets.newHashSet();

        for (Entry<OperationProductComponentHolder, BigDecimal> productComponentQuantity : productComponentQuantities.asMap()
                .entrySet()) {
            Entity operationProductComponent = productComponentQuantity.getKey().getEntity();

            if (technologyOperationComponent != null) {
                Entity operationComponent = operationProductComponent.getBelongsToField(L_OPERATION_COMPONENT);

                if (!technologyOperationComponent.getId().equals(operationComponent.getId())) {
                    continue;
                }
            }

            if (operationProductModel.equals(operationProductComponent.getDataDefinition().getName())) {
                Entity product = operationProductComponent.getBelongsToField(L_PRODUCT);

                if (!alreadyAddedProducts.contains(product.getId())) {
                    createTrackingOperationProduct(trackingOperationProductComponents, trackingOperationProductModelName, product);

                    alreadyAddedProducts.add(product.getId());
                }
            }
        }

        productionTracking.setField(trackingOperationProductsFieldName, trackingOperationProductComponents);
    }

    private void createTrackingOperationProduct(final List<Entity> trackingOperationProductComponents,
            final String trackingOperationProductModelName, final Entity product) {
        Entity trackingOperationProductComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                trackingOperationProductModelName).create();

        trackingOperationProductComponent.setField(L_PRODUCT, product);

        trackingOperationProductComponents.add(trackingOperationProductComponent);
    }

    private boolean shouldCopy(final Entity productionTracking, final Entity order, final Entity technologyOperationComponent) {
        return (hasValueChanged(productionTracking, order, ProductionTrackingFields.ORDER)
                || (technologyOperationComponent != null && hasValueChanged(productionTracking, technologyOperationComponent,
                        ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT)) || !hasTrackingOperationProductComponents(productionTracking));
    }

    private boolean hasTrackingOperationProductComponents(final Entity productionTracking) {
        return ((productionTracking.getField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS) != null) && (productionTracking
                .getField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS) != null));
    }

    private boolean hasValueChanged(final Entity productionTracking, final Entity value, final String model) {
        Entity existingProductionTracking = getExistingProductionTracking(productionTracking);
        if (existingProductionTracking == null) {
            return false;
        }
        Entity existingProductionTrackingValue = existingProductionTracking.getBelongsToField(model);
        if (existingProductionTrackingValue == null) {
            return true;
        }
        return !existingProductionTrackingValue.equals(value);
    }

    private Entity getExistingProductionTracking(final Entity productionTracking) {
        if (productionTracking.getId() == null) {
            return null;
        }
        return productionTracking.getDataDefinition().get(productionTracking.getId());
    }

    private void clearLaborAndMachineTime(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        productionTracking.setField(ProductionTrackingFields.LABOR_TIME, 0);
        productionTracking.setField(ProductionTrackingFields.MACHINE_TIME, 0);
    }

}
