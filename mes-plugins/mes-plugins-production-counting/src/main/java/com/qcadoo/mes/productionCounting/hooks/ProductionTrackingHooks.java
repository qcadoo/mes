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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.hooks.helpers.OperationProductsExtractor;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductionTrackingHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private ProductionTrackingStateChangeDescriber describer;

    @Autowired
    private OperationProductsExtractor operationProductsExtractor;

    public void onCreate(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        setInitialState(productionTracking);
    }

    public void onCopy(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        setInitialState(productionTracking);
        clearLaborAndMachineTime(productionTracking);
    }

    public void onSave(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        generateNumberIfNeeded(productionTracking);
        copyProducts(productionTracking);
    }

    private void copyProducts(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        final boolean registerQuantityInProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT);
        final boolean registerQuantityOutProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT);

        if (!(registerQuantityInProduct || registerQuantityOutProduct)
                || StringUtils.isEmpty(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))
                || !shouldCopyProducts(productionTracking)) {
            return;
        }

        OperationProductsExtractor.TrackingOperationProducts operationProducts = operationProductsExtractor
                .getProductsByModelName(productionTracking);

        List<Entity> inputs = Collections.emptyList();
        if (registerQuantityInProduct) {
            inputs = operationProducts.getInputComponents();
        }

        List<Entity> outputs = Collections.emptyList();
        if (registerQuantityOutProduct) {
            outputs = operationProducts.getOutputComponents();
        }

        productionTracking.setField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS, inputs);
        productionTracking.setField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS, outputs);
    }

    private boolean shouldCopyProducts(final Entity productionTracking) {
        if (productionTracking.getId() == null) {
            return true;
        }

        Entity existingProductionTracking = productionTracking.getDataDefinition().get(productionTracking.getId());

        Object oldTocValue = existingProductionTracking.getField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        Object newTocValue = productionTracking.getField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        Object oldOrderValue = existingProductionTracking.getField(ProductionTrackingFields.ORDER);
        Object newOrderValue = productionTracking.getField(ProductionTrackingFields.ORDER);

        return !ObjectUtils.equals(oldOrderValue, newOrderValue) || !ObjectUtils.equals(oldTocValue, newTocValue);
    }

    private void setInitialState(final Entity productionTracking) {
        stateChangeEntityBuilder.buildInitial(describer, productionTracking, ProductionTrackingState.DRAFT);
    }

    private void generateNumberIfNeeded(final Entity productionTracking) {
        if (productionTracking.getField(ProductionTrackingFields.NUMBER) == null) {
            productionTracking.setField(ProductionTrackingFields.NUMBER, numberGeneratorService.generateNumber(
                    ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING));
        }
    }

    private void clearLaborAndMachineTime(final Entity productionTracking) {
        productionTracking.setField(ProductionTrackingFields.LABOR_TIME, 0);
        productionTracking.setField(ProductionTrackingFields.MACHINE_TIME, 0);
    }
}
