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
package com.qcadoo.mes.productionCounting.validators;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductionTrackingValidators {

    private static final Set<String> ORDER_STARTED_STATES_SET = Sets.newHashSet(OrderStateStringValues.IN_PROGRESS,
            OrderStateStringValues.COMPLETED, OrderStateStringValues.INTERRUPTED);

    @Autowired
    private ProductionCountingService productionCountingService;

    public boolean validatesWith(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        boolean isValid = true;

        isValid = isValid && checkTypeOfProductionRecording(productionTrackingDD, productionTracking, order);
        isValid = isValid && willOrderAcceptOneMore(productionTrackingDD, productionTracking, order);
        isValid = isValid && checkIfOrderIsStarted(productionTrackingDD, productionTracking, order);

        return isValid;
    }

    private boolean checkTypeOfProductionRecording(final DataDefinition productionTrackingDD, final Entity productionTracking,
            final Entity order) {
        if (order == null) {
            return true;
        }
        final String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        return isValidTypeOfProductionRecording(productionTrackingDD, productionTracking, typeOfProductionRecording);
    }

    private boolean isValidTypeOfProductionRecording(final DataDefinition productionTrackingDD, final Entity productionTracking,
            final String typeOfProductionRecording) {
        boolean isValid = true;

        if (productionCountingService.checkIfTypeOfProductionRecordingIsEmptyOrBasic(typeOfProductionRecording)) {
            productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.ORDER),
                    "productionCounting.validate.global.error.productionTracking.orderError");
            isValid = false;
        }
        if (productionCountingService.isTypeOfProductionRecordingBasic(typeOfProductionRecording)) {
            productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.ORDER),
                    "productionCounting.productionTracking.report.error.orderWithBasicProductionCounting");
            isValid = false;
        }

        return isValid;
    }

    private boolean willOrderAcceptOneMore(final DataDefinition productionTrackingDD, final Entity productionTracking,
            final Entity order) {

        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        final List<Entity> productionTrackings = productionTrackingDD
                .find()
                .add(SearchRestrictions.eq(ProductionTrackingFields.STATE, ProductionTrackingStateStringValues.ACCEPTED))
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent)).list().getEntities();

        return willOrderAcceptOneMoreValidator(productionTrackingDD, productionTracking, productionTrackings);
    }

    private boolean willOrderAcceptOneMoreValidator(final DataDefinition productionTrackingDD, final Entity productionTracking,
            final List<Entity> productionTrackings) {
        for (Entity tracking : productionTrackings) {
            if (tracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING)) {
                if (productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT) == null) {
                    productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.ORDER),
                            "productionCounting.record.messages.error.final");
                } else {
                    productionTracking.addError(
                            productionTrackingDD.getField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT),
                            "productionCounting.record.messages.error.operationFinal");
                }

                return false;
            }
        }

        return true;
    }

    private boolean checkIfOrderIsStarted(final DataDefinition productionTrackingDD, final Entity productionTracking,
            final Entity order) {
        boolean isStarted = true;

        String state = order.getStringField(OrderFields.STATE);

        if (!isOrderStarted(state)) {
            productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.ORDER),
                    "productionCounting.record.messages.error.orderIsNotStarted");

            isStarted = false;
        }

        return isStarted;
    }

    private boolean isOrderStarted(final String state) {
        return ORDER_STARTED_STATES_SET.contains(state);
    }

}
