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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordState;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductionRecordHooks {

    private static final String L_PRODUCT = "product";

    private static final String L_OPERATION_COMPONENT = "operationComponent";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private ProductionRecordStateChangeDescriber describer;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public void onCreate(final DataDefinition productionRecordDD, final Entity productionRecord) {
        setInitialState(productionRecordDD, productionRecord);
    }

    public void onSave(final DataDefinition productionRecordDD, final Entity productionRecord) {
        generateData(productionRecordDD, productionRecord);
        copyProductsFromOrderOperation(productionRecordDD, productionRecord);
    }

    public void onCopy(final DataDefinition productionRecordDD, final Entity productionRecord) {
        setInitialState(productionRecordDD, productionRecord);
        clearLaborAndMachineTime(productionRecordDD, productionRecord);
    }

    private void setInitialState(final DataDefinition productionRecordDD, final Entity productionRecord) {
        stateChangeEntityBuilder.buildInitial(describer, productionRecord, ProductionRecordState.DRAFT);
    }

    private void generateData(final DataDefinition productionRecordDD, final Entity productionRecord) {
        if (productionRecord.getField(ProductionRecordFields.NUMBER) == null) {
            productionRecord.setField(ProductionRecordFields.NUMBER, numberGeneratorService.generateNumber(
                    ProductionCountingConstants.PLUGIN_IDENTIFIER, productionRecord.getDataDefinition().getName()));
        }
    }

    private void copyProductsFromOrderOperation(final DataDefinition productionRecordDD, final Entity productionRecord) {
        Entity order = productionRecord.getBelongsToField(ProductionRecordFields.ORDER);
        Entity technologyOperationComponent = productionRecord
                .getBelongsToField(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT);

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (typeOfProductionRecording == null) {
            return;
        }

        boolean registerInput = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT);
        boolean registerOutput = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT);

        if (!registerInput && !registerOutput) {
            return;
        }

        if (shouldCopy(productionRecord, order, technologyOperationComponent)) {
            if (registerInput) {
                copyOperationProductComponents(productionRecord, order, technologyOperationComponent,
                        ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT);
            }
            if (registerOutput) {
                copyOperationProductComponents(productionRecord, order, technologyOperationComponent,
                        ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT);
            }
        }
    }

    private void copyOperationProductComponents(final Entity productionRecord, final Entity order,
            final Entity technologyOperationComponent, final String recordOperationProductModelName) {
        List<Entity> recordOperationProducts = Lists.newArrayList();

        String operationProductModel = null;
        String recordOperationProductsFieldName = null;

        if (ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT.equals(recordOperationProductModelName)) {
            operationProductModel = "operationProductInComponent";
            recordOperationProductsFieldName = ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS;
        } else if (ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT
                .equals(recordOperationProductModelName)) {
            operationProductModel = "operationProductOutComponent";
            recordOperationProductsFieldName = ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS;
        }

        Map<Long, BigDecimal> productComponentQuantities = productQuantitiesService.getProductComponentQuantities(asList(order));

        Set<Long> alreadyAddedProducts = Sets.newHashSet();

        for (Entry<Long, BigDecimal> productComponentQuantity : productComponentQuantities.entrySet()) {
            Entity operationProductComponent = productQuantitiesService.getOperationProductComponent(productComponentQuantity
                    .getKey());

            if (technologyOperationComponent != null) {
                Entity operationComponent = operationProductComponent.getBelongsToField(L_OPERATION_COMPONENT);

                if (!technologyOperationComponent.getId().equals(operationComponent.getId())) {
                    continue;
                }
            }

            if (operationProductModel.equals(operationProductComponent.getDataDefinition().getName())) {
                Entity product = operationProductComponent.getBelongsToField(L_PRODUCT);

                if (!alreadyAddedProducts.contains(product.getId())) {
                    createRecordOperationProduct(recordOperationProducts, recordOperationProductModelName, product);

                    alreadyAddedProducts.add(product.getId());
                }
            }
        }

        productionRecord.setField(recordOperationProductsFieldName, recordOperationProducts);
    }

    private void createRecordOperationProduct(final List<Entity> recordOperationProducts,
            final String recordOperationProductModelName, final Entity product) {
        Entity recordOperationProduct = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                recordOperationProductModelName).create();

        recordOperationProduct.setField(L_PRODUCT, product);

        recordOperationProducts.add(recordOperationProduct);
    }

    private boolean shouldCopy(final Entity productionRecord, final Entity order, final Entity technologyOperationComponent) {
        return (hasValueChanged(productionRecord, order, ProductionRecordFields.ORDER)
                || (technologyOperationComponent != null && hasValueChanged(productionRecord, technologyOperationComponent,
                        ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT)) || !hasRecordOperationProductComponents(productionRecord));
    }

    private boolean hasRecordOperationProductComponents(final Entity productionRecord) {
        return ((productionRecord.getField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS) != null) && (productionRecord
                .getField(ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS) != null));
    }

    private boolean hasValueChanged(final Entity productionRecord, final Entity value, final String model) {
        Entity existingProductionRecord = getExistingProductionRecord(productionRecord);
        if (existingProductionRecord == null) {
            return false;
        }
        Entity existingProductionRecordValue = existingProductionRecord.getBelongsToField(model);
        if (existingProductionRecordValue == null) {
            return true;
        }
        return !existingProductionRecordValue.equals(value);
    }

    private Entity getExistingProductionRecord(final Entity productionRecord) {
        if (productionRecord.getId() == null) {
            return null;
        }
        return productionRecord.getDataDefinition().get(productionRecord.getId());
    }

    private void clearLaborAndMachineTime(final DataDefinition productionRecordDD, final Entity productionRecord) {
        productionRecord.setField(ProductionRecordFields.LABOR_TIME, 0);
        productionRecord.setField(ProductionRecordFields.MACHINE_TIME, 0);
    }

}
