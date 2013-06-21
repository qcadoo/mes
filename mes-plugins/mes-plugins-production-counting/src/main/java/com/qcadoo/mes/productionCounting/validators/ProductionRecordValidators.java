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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductionRecordValidators {

    private static final List<String> L_ORDER_STARTED_STATES = Lists.newArrayList(OrderStateStringValues.IN_PROGRESS,
            OrderStateStringValues.COMPLETED, OrderStateStringValues.INTERRUPTED);

    @Autowired
    private ProductionCountingService productionCountingService;

    public boolean validatesWith(final DataDefinition productionRecordDD, final Entity productionRecord) {
        boolean isValid = true;

        isValid = isValid && checkTypeOfProductionRecording(productionRecordDD, productionRecord);
        isValid = isValid && willOrderAcceptOneMore(productionRecordDD, productionRecord);
        isValid = isValid && checkIfOrderIsStarted(productionRecordDD, productionRecord);
        isValid = isValid && checkIfOperationIsSet(productionRecordDD, productionRecord);

        return isValid;
    }

    private boolean checkTypeOfProductionRecording(final DataDefinition productionRecordDD, final Entity productionRecord) {
        final Entity order = productionRecord.getBelongsToField(ProductionRecordFields.ORDER);
        if (order == null) {
            return true;
        }
        final String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        return isValidTypeOfProductionRecording(productionRecord, typeOfProductionRecording, productionRecordDD);
    }

    private boolean isValidTypeOfProductionRecording(final Entity productionRecord, final String typeOfProductionRecording,
            final DataDefinition productionRecordDD) {
        boolean isValid = true;

        if (productionCountingService.checkIfTypeOfProductionRecordingIsEmptyOrBasic(typeOfProductionRecording)) {
            productionRecord.addError(productionRecordDD.getField(ProductionRecordFields.ORDER),
                    "productionCounting.validate.global.error.productionRecord.orderError");
            isValid = false;
        }
        if (productionCountingService.isTypeOfProductionRecordingBasic(typeOfProductionRecording)) {
            productionRecord.addError(productionRecordDD.getField(ProductionRecordFields.ORDER),
                    "productionRecord.productionRecord.report.error.orderWithBasicProductionCounting");
            isValid = false;
        }

        return isValid;
    }

    private boolean willOrderAcceptOneMore(final DataDefinition productionRecordDD, final Entity productionRecord) {
        Entity order = productionRecord.getBelongsToField(ProductionRecordFields.ORDER);
        Entity technologyOperationComponent = productionRecord
                .getBelongsToField(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT);

        final List<Entity> productionRecords = productionRecordDD
                .find()
                .add(SearchRestrictions.eq(ProductionRecordFields.STATE, ProductionRecordStateStringValues.ACCEPTED))
                .add(SearchRestrictions.belongsTo(ProductionRecordFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent)).list().getEntities();

        return willOrderAcceptOneMoreValidator(productionRecords, productionRecord, productionRecordDD);
    }

    private boolean willOrderAcceptOneMoreValidator(final List<Entity> productionRecords, final Entity productionRecord,
            final DataDefinition productionRecordDD) {
        for (Entity record : productionRecords) {
            if (record.getBooleanField(ProductionRecordFields.LAST_RECORD)) {
                if (productionRecord.getBelongsToField(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT) == null) {
                    productionRecord.addError(productionRecordDD.getField(ProductionRecordFields.ORDER),
                            "productionCounting.record.messages.error.final");
                } else {
                    productionRecord.addError(productionRecordDD.getField(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT),
                            "productionCounting.record.messages.error.operationFinal");
                }

                return false;
            }
        }

        return true;
    }

    private boolean checkIfOrderIsStarted(final DataDefinition productionRecordDD, final Entity productionRecord) {
        boolean isStarted = true;

        Entity order = productionRecord.getBelongsToField(ProductionRecordFields.ORDER);
        String state = order.getStringField(OrderFields.STATE);

        if (!isOrderStarted(state)) {
            productionRecord.addError(productionRecordDD.getField(ProductionRecordFields.ORDER),
                    "productionCounting.record.messages.error.orderIsNotStarted");

            isStarted = false;
        }

        return isStarted;
    }

    private boolean isOrderStarted(final String state) {
        return L_ORDER_STARTED_STATES.contains(state);
    }

    private boolean checkIfOperationIsSet(final DataDefinition productionRecordDD, final Entity productionRecord) {
        Entity order = productionRecord.getBelongsToField(ProductionRecordFields.ORDER);
        Entity technologyOperationComponent = productionRecord
                .getBelongsToField(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT);

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)
                && (technologyOperationComponent == null)) {
            productionRecord.addError(productionRecordDD.getField(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT),
                    "productionCounting.record.messages.error.operationIsNotSet");

            return false;
        }

        return true;
    }

}
