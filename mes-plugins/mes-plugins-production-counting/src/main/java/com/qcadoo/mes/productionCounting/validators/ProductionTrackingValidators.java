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
package com.qcadoo.mes.productionCounting.validators;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import java.util.*;

import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductionTrackingValidators {

    private static final Set<String> L_ORDER_STARTED_STATES_SET = Sets.newHashSet(OrderStateStringValues.IN_PROGRESS,
            OrderStateStringValues.INTERRUPTED, OrderStateStringValues.COMPLETED);

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private ParameterService parameterService;

    public boolean validatesWith(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        boolean isValid = true;

        isValid = isValid && checkTypeOfProductionRecording(productionTrackingDD, productionTracking, order);
        //isValid = isValid && willOrderAcceptOneMore(productionTrackingDD, productionTracking, order);
        isValid = isValid && checkIfOrderIsStarted(productionTrackingDD, productionTracking, order);
        isValid = isValid && checkTimeRange(productionTrackingDD, productionTracking);
        isValid = isValid && checkIfOperationIsSet(productionTrackingDD, productionTracking);
        isValid = isValid && checkIfExpirationDateTheSameForOrder(productionTrackingDD, productionTracking);
        isValid = isValid && checkIfIsOne(productionTrackingDD, productionTracking);
        isValid = isValid && checkIfIsLastTracking(productionTrackingDD, productionTracking);
        return isValid;
    }

    private boolean checkIfIsLastTracking(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        if(!productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING) || productionTracking.getBooleanField(ProductionTrackingFields.ON_UNCORRECTION_PROCESS) ) {
            return true;
        }

        SearchCriteriaBuilder scb = productionTrackingDD.find()
                .createAlias(ProductionTrackingFields.ORDER, "o", JoinType.LEFT)
                .add(SearchRestrictions.eq(ProductionTrackingFields.LAST_TRACKING, Boolean.TRUE))
                .add(SearchRestrictions.eq("o.id", productionTracking.getBelongsToField(ProductionTrackingFields.ORDER).getId()))
                .add(SearchRestrictions.in(ProductionTrackingFields.STATE,
                        Lists.newArrayList(ProductionTrackingStateStringValues.DRAFT, ProductionTrackingStateStringValues.ACCEPTED,
                                ProductionTrackingStateStringValues.PENDING)));

        Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        if (Objects.nonNull(toc)) {
            scb.createAlias(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, "toc", JoinType.LEFT)
                    .add(SearchRestrictions.eq("toc.id", toc.getId()));
        }
        if (Objects.nonNull(productionTracking.getId())) {
            scb.add(SearchRestrictions.idNe(productionTracking.getId()));
        }

        List<Entity> entities = scb.list().getEntities();
        if (!entities.isEmpty()) {
            productionTracking.addGlobalError("productionCounting.productionTracking.messages.error.canExistOnlyOneFinalTracking", false);
            return false;
        }
        return true;
    }

    private boolean checkIfIsOne(DataDefinition productionTrackingDD, Entity productionTracking) {

        if (Objects.nonNull(productionTracking.getBelongsToField(ProductionTrackingFields.CORRECTION))
                || productionTracking.getBooleanField(ProductionTrackingFields.IS_CORRECTION)
                || productionTracking.getBooleanField(ProductionTrackingFields.IS_CORRECTED)) {
            return true;
        }

        boolean justOne = parameterService.getParameter().getBooleanField(ParameterFieldsPC.JUST_ONE);

        if (!justOne) {
            return true;
        }

        SearchCriteriaBuilder scb = productionTrackingDD.find()
                .createAlias(ProductionTrackingFields.ORDER, "o", JoinType.LEFT)
                .add(SearchRestrictions.eq("o.id", productionTracking.getBelongsToField(ProductionTrackingFields.ORDER).getId()))
                .add(SearchRestrictions.in(ProductionTrackingFields.STATE,
                        Lists.newArrayList(ProductionTrackingStateStringValues.DRAFT, ProductionTrackingStateStringValues.ACCEPTED,
                                ProductionTrackingStateStringValues.PENDING)));

        Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        if (Objects.nonNull(toc)) {
            scb.createAlias(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, "toc", JoinType.LEFT)
                    .add(SearchRestrictions.eq("toc.id", toc.getId()));
        }
        if (Objects.nonNull(productionTracking.getId())) {
            scb.add(SearchRestrictions.idNe(productionTracking.getId()));
        }

        List<Entity> entities = scb.list().getEntities();
        if (!entities.isEmpty()) {
            productionTracking.addGlobalError("productionCounting.productionTracking.messages.error.canExistOnlyOneProductionTrackingRecord", false);
            return false;
        }
        return true;
    }

    private boolean checkIfExpirationDateTheSameForOrder(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity batch = productionTracking.getBelongsToField(ProductionTrackingFields.BATCH);
        Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        Either<Boolean, Optional<Date>> maybeExpirationDayFilled = productionTrackingService.findExpirationDate(productionTracking, order, toc, batch);
        if (Objects.nonNull(maybeExpirationDayFilled)) {
            Date expirationDate = productionTracking.getDateField(ProductionTrackingFields.EXPIRATION_DATE);

            if (maybeExpirationDayFilled.isLeft() && Objects.nonNull(expirationDate)) {
                productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.EXPIRATION_DATE),
                        "productionCounting.productionTracking.messages.error.expirationDateNotFilled");
            } else if (maybeExpirationDayFilled.isRight() && maybeExpirationDayFilled.getRight().isPresent()) {
                Date alreadyDefinedExpirationDate = maybeExpirationDayFilled.getRight().get();

                if (Objects.isNull(expirationDate) || !alreadyDefinedExpirationDate.equals(expirationDate)) {
                    productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.EXPIRATION_DATE),
                            "productionCounting.productionTracking.messages.error.expirationDate", DateUtils.toDateString(alreadyDefinedExpirationDate));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkIfOperationIsSet(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        String recordingMode = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER).getStringField(
                OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        Object orderOperation = productionTracking.getField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(recordingMode) && orderOperation == null) {
            productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT),
                    "productionCounting.productionTracking.messages.error.operationIsNotSet");
            return false;
        }
        return true;

    }

    private boolean checkTimeRange(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        Date timeRangeFrom = productionTracking.getDateField(ProductionTrackingFields.TIME_RANGE_FROM);
        Date timeRangeTo = productionTracking.getDateField(ProductionTrackingFields.TIME_RANGE_TO);

        if (timeRangeFrom == null || timeRangeTo == null || timeRangeTo.after(timeRangeFrom)) {
            return true;
        }
        productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.TIME_RANGE_TO),
                "productionCounting.productionTracking.productionTrackingError.timeRangeToBeforetumeRangeFrom");
        return false;
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


    private boolean checkIfOrderIsStarted(final DataDefinition productionTrackingDD, final Entity productionTracking,
                                          final Entity order) {
        boolean isStarted = true;

        String state = productionTracking.getStringField(ProductionTrackingFields.STATE);
        String orderState = order.getStringField(OrderFields.STATE);

        if (isProductionTrackingDraft(state) && !isOrderStateStarted(orderState)) {
            productionTracking.addError(productionTrackingDD.getField(ProductionTrackingFields.ORDER),
                    "productionCounting.productionTracking.messages.error.orderIsNotStarted");

            isStarted = false;
        }

        return isStarted;
    }

    private boolean isProductionTrackingDraft(final String state) {
        return ProductionTrackingStateStringValues.DRAFT.equals(state);
    }

    private boolean isOrderStateStarted(final String orderState) {
        return L_ORDER_STARTED_STATES_SET.contains(orderState);
    }

}
