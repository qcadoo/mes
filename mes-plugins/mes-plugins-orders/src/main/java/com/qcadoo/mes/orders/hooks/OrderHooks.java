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
package com.qcadoo.mes.orders.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.ProductService;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.ExpiryDateValidityUnit;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OrderPackService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.OrderStateChangeReasonService;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.states.constants.OrderStateChangeDescriber;
import com.qcadoo.mes.orders.states.constants.OrderStateChangeFields;
import com.qcadoo.mes.orders.util.AdditionalUnitService;
import com.qcadoo.mes.orders.util.OrderDatesService;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyProductionLineFields;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.security.api.UserService;
import com.qcadoo.security.constants.UserFields;
import com.qcadoo.view.api.utils.TimeConverterService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OrderHooks {

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    private static final List<String> sourceDateFields = Lists.newArrayList("sourceCorrectedDateFrom", "sourceCorrectedDateTo",
            "sourceStartDate", "sourceFinishDate");

    public static final String ORDER_PACKS_VALIDATE_GLOBAL_ERROR_QUANTITY_ERROR = "orderPacks.validate.global.error.quantityError";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDatesService orderDatesService;

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private OrderStateChangeDescriber orderStateChangeDescriber;

    @Autowired
    private OrderStateChangeReasonService orderStateChangeReasonService;

    @Autowired
    private UserService userService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private OrderPackService orderPackService;

    @Autowired
    private AdditionalUnitService additionalUnitService;

    @Autowired
    private FileService fileService;

    public boolean validatesWith(final DataDefinition orderDD, final Entity order) {
        Entity parameter = parameterService.getParameter();

        boolean isValid = checkOrderDates(orderDD, order);
        isValid = isValid && checkOrderPlannedQuantity(orderDD, order);
        isValid = isValid && checkProductQuantities(orderDD, order);
        isValid = isValid && productService.checkIfProductIsNotRemoved(orderDD, order);
        isValid = isValid && checkReasonOfStartDateCorrection(parameter, order);
        isValid = isValid && checkReasonOfEndDateCorrection(parameter, order);
        isValid = isValid && checkEffectiveDeviation(parameter, order);
        isValid = isValid && checkOperationalTasks(orderDD, order);
        isValid = isValid && checkOrderPacksQuantity(orderDD, order);
        isValid = isValid && checkOrderTechnologicalProcessesQuantity(orderDD, order);
        isValid = isValid && checkOrderProduct(orderDD, order);

        return isValid;
    }

    private boolean checkOrderProduct(final DataDefinition orderDD, final Entity order) {
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);
        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(product.getStringField(ProductFields.ENTITY_TYPE))) {
            order.addGlobalError("orders.validate.global.error.product.differentProductsInDifferentSizes");
            return false;
        }
        return true;
    }

    public void onCreate(final DataDefinition orderDD, final Entity order) {
        setInitialState(orderDD, order);
        setCommissionedPlannedQuantity(orderDD, order);

        if (Objects.isNull(order.getField(OrderFields.EXTERNAL_SYNCHRONIZED))) {
            order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
        }

        if (Objects.isNull(order.getField(OrderFields.PRIORITY))) {
            order.setField(OrderFields.PRIORITY, 100);
        }

        fillStartDateFromParameters(order);
    }

    private void fillStartDateFromParameters(final Entity order) {
        if (Objects.isNull(order.getId())
                && parameterService.getParameter().getBooleanField(ParameterFieldsO.ADVISE_START_DATE_OF_THE_ORDER)
                && Objects.isNull(order.getDateField(OrderFields.START_DATE))) {
            String basedOn = parameterService.getParameter().getStringField(ParameterFieldsO.ORDER_START_DATE_BASED_ON);

            if (OrderStartDateBasedOn.CURRENT_DATE.getStringValue().equals(basedOn)) {
                order.setField(OrderFields.START_DATE, new Date());
            } else if (OrderStartDateBasedOn.BEGINNING_FIRST_SHIFT_NEXT_DAY.getStringValue().equals(basedOn)) {
                if (Objects.nonNull(order.getBelongsToField(OrderFields.PRODUCTION_LINE))) {
                    DateTime nextDate = DateTime.now().plusDays(1);

                    Optional<DateTime> maybeDate = shiftsService.getNearestWorkingDate(nextDate.withTimeAtStartOfDay(),
                            order.getBelongsToField(OrderFields.PRODUCTION_LINE));

                    maybeDate.ifPresent(dateTime -> order.setField(OrderFields.START_DATE, dateTime.toDate()));
                }
            } else if (OrderStartDateBasedOn.END_DATE_LAST_ORDER_ON_THE_LINE.getStringValue().equals(basedOn)) {
                if (Objects.nonNull(order.getBelongsToField(OrderFields.PRODUCTION_LINE))) {
                    Optional<Entity> maybeLastOrder = orderService.findLastOrder(order);

                    if (maybeLastOrder.isPresent()) {
                        Entity lastOrder = maybeLastOrder.get();

                        Optional<DateTime> maybeDate = shiftsService.getNearestWorkingDate(
                                new DateTime(lastOrder.getDateField(OrderFields.FINISH_DATE)),
                                order.getBelongsToField(OrderFields.PRODUCTION_LINE));

                        maybeDate.ifPresent(dateTime -> order.setField(OrderFields.START_DATE, dateTime.toDate()));
                    }
                }
            }
        }
    }

    public void onSave(final DataDefinition orderDD, final Entity order) {
        copyStartDate(orderDD, order);
        copyEndDate(orderDD, order);
        copyProductQuantity(orderDD, order);
        onCorrectingTheRequestedVolume(orderDD, order);
        auditDatesChanges(order);
        setRemainingQuantity(order);
        setAdditionalFields(order);
        fillExpirationDate(order);
        checkMinimalQuantity(order);
    }

    private void fillExpirationDate(final Entity order) {
        if (Objects.isNull(order.getDateField(OrderFields.EXPIRATION_DATE))) {
            setExpirationDate(order, false);
        } else {
            if (Objects.nonNull(order.getId()) && Objects.nonNull(order.getDateField(OrderFields.START_DATE))) {
                Entity orderDb = order.getDataDefinition().get(order.getId());
                if (!order.getDateField(OrderFields.START_DATE).equals(orderDb.getDateField(OrderFields.START_DATE)) || Objects.isNull(orderDb.getDateField(OrderFields.START_DATE))) {
                    setExpirationDate(order, false);

                }

                Entity product = order.getBelongsToField(OrderFields.PRODUCT);
                Entity productDb = orderDb.getBelongsToField(OrderFields.PRODUCT);

                if (!product.getId().equals(productDb.getId())) {
                    setExpirationDate(order, true);
                }
            }
        }

    }

    private void setExpirationDate(Entity order, boolean clearIfEmptyProductExpiryDateValidity) {
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);
        if (Objects.isNull(product)) {
            return;
        }
        Integer expiryDateValidity = product.getIntegerField(ProductFields.EXPIRY_DATE_VALIDITY);
        String expiryDateValidityUnit = product.getStringField(ProductFields.EXPIRY_DATE_VALIDITY_UNIT);

        if (Objects.nonNull(expiryDateValidity) && Objects.nonNull(order.getDateField(OrderFields.START_DATE))) {
            Date expirationDate;

            if (ExpiryDateValidityUnit.DAYS.getStringValue().equals(expiryDateValidityUnit)) {
                expirationDate = new DateTime(order.getDateField(OrderFields.START_DATE)).plusDays(expiryDateValidity).toDate();
            } else {
                expirationDate = new DateTime(order.getDateField(OrderFields.START_DATE)).plusMonths(expiryDateValidity).toDate();
            }

            order.setField(OrderFields.EXPIRATION_DATE, expirationDate);
        } else if (clearIfEmptyProductExpiryDateValidity) {
            order.setField(OrderFields.EXPIRATION_DATE, null);

        }

    }

    private void setAdditionalFields(final Entity order) {
        if (Objects.isNull(order.getId())) {
            order.setField("includeTpz", parameterService.getParameter().getBooleanField("includeTpzPS"));
            order.setField("includeAdditionalTime", parameterService.getParameter().getBooleanField("includeAdditionalTimePS"));
        }
    }

    public void onCopy(final DataDefinition orderDD, final Entity order) {
        setInitialState(orderDD, order);
        clearOrSetSpecyfiedValueOrderFieldsOnCopy(orderDD, order);
        setProductQuantity(orderDD, order);
        copyAttachments(order);
        setProductionLine(order);
    }

    private void setProductionLine(Entity order) {

        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        if (Objects.isNull(productionLine)) {
            return;
        }
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.isNull(technology)) {
            order.setField(OrderFields.PRODUCTION_LINE, null);
            return;
        }
        List<Entity> lines = technology.getHasManyField(TechnologyFields.PRODUCTION_LINES);
        if (lines.isEmpty()) {
            return;
        }

        boolean anyMatch = lines.stream().anyMatch(l -> l.getBelongsToField(TechnologyProductionLineFields.PRODUCTION_LINE).getId().equals(productionLine.getId()));

        if (anyMatch) {
            return;
        }

        Entity newProductionLine = orderService.getProductionLine(technology);
        order.setField(OrderFields.PRODUCTION_LINE, newProductionLine);

    }


    public void setRemainingQuantity(final Entity order) {
        BigDecimal remainingAmountOfProductToProduce = BigDecimalUtils
                .convertNullToZero(order.getDecimalField(OrderFields.PLANNED_QUANTITY))
                .subtract(BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED)),
                        numberService.getMathContext());

        order.setField(OrderFields.REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE, remainingAmountOfProductToProduce);
    }

    public boolean setDateChanged(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Entity order,
                                  final Object fieldOldValue, final Object fieldNewValue) {
        OrderState orderState = OrderState.of(order);

        if (Objects.nonNull(fieldOldValue) && Objects.nonNull(fieldNewValue) && !orderState.equals(OrderState.PENDING)) {
            Date oldDate = DateUtils.parseDate(fieldOldValue);
            Date newDate = DateUtils.parseDate(fieldNewValue);

            if (!oldDate.equals(newDate)) {
                order.setField(OrderFields.DATES_CHANGED, true);
                order.setField(getSourceFieldName(fieldDefinition), fieldOldValue);
            }
        }

        return true;
    }

    private void auditDatesChanges(final Entity order) {
        boolean datesChanged = order.getBooleanField(OrderFields.DATES_CHANGED);

        OrderState orderState = OrderState.of(order);

        if (datesChanged && !orderState.equals(OrderState.PENDING)) {
            order.setField(OrderFields.DATES_CHANGED, false);

            DataDefinition orderStateChangeDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_ORDER_STATE_CHANGE);

            Entity orderStateChange = orderStateChangeDD.create();

            orderStateChange.setField(OrderStateChangeFields.DATES_CHANGED, true);
            orderStateChange.setField(OrderStateChangeFields.ORDER, order);
            orderStateChange.setField(OrderStateChangeFields.SOURCE_CORRECTED_DATE_FROM,
                    order.getField(OrderFields.SOURCE_CORRECTED_DATE_FROM));
            orderStateChange.setField(OrderStateChangeFields.SOURCE_CORRECTED_DATE_TO,
                    order.getField(OrderFields.SOURCE_CORRECTED_DATE_TO));
            orderStateChange.setField(OrderStateChangeFields.SOURCE_FINISH_DATE, order.getField(OrderFields.SOURCE_FINISH_DATE));
            orderStateChange.setField(OrderStateChangeFields.SOURCE_START_DATE, order.getField(OrderFields.SOURCE_START_DATE));
            orderStateChange.setField(OrderStateChangeFields.TARGET_CORRECTED_DATE_FROM,
                    order.getField(OrderFields.CORRECTED_DATE_FROM));
            orderStateChange.setField(OrderStateChangeFields.TARGET_CORRECTED_DATE_TO,
                    order.getField(OrderFields.CORRECTED_DATE_TO));
            orderStateChange.setField(OrderStateChangeFields.TARGET_FINISH_DATE, order.getField(OrderFields.FINISH_DATE));
            orderStateChange.setField(OrderStateChangeFields.TARGET_START_DATE, order.getField(OrderFields.START_DATE));
            orderStateChange.setField(OrderStateChangeFields.SOURCE_STATE, order.getField(OrderFields.STATE));
            orderStateChange.setField(OrderStateChangeFields.TARGET_STATE, order.getField(OrderFields.STATE));

            String workerToChange = order.getStringField(OrderFields.WORKER_TO_CHANGE);

            if (StringUtils.isEmpty(workerToChange)) {
                orderStateChange.setField(OrderStateChangeFields.WORKER,
                        userService.getCurrentUserEntity().getField(UserFields.USER_NAME));
            } else {
                orderStateChange.setField(OrderStateChangeFields.WORKER, workerToChange);
                order.setField(OrderFields.WORKER_TO_CHANGE, null);
            }

            orderStateChange.setField("dateAndTime", setDateToField(new Date()));
            orderStateChange.setField(OrderStateChangeFields.STATUS, "03successful");

            orderStateChangeDD.save(orderStateChange);
        }
    }

    private Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }

    private String getSourceFieldName(final FieldDefinition fieldDefinition) {
        String targetName = fieldDefinition.getName();

        for (String fieldName : sourceDateFields) {
            if (fieldName.toLowerCase().contains(targetName.toLowerCase())) {
                return fieldName;
            }
        }

        return null;
    }

    public void setInitialState(final DataDefinition orderDD, final Entity order) {
        stateChangeEntityBuilder.buildInitial(orderStateChangeDescriber, order, OrderState.PENDING);
    }

    private void checkMinimalQuantity(final Entity order) {
        BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);

        if (Objects.isNull(plannedQuantity)) {
            return;
        }
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.isNull(technology)) {
            return;
        }
        BigDecimal minimalQuantity = technology.getDecimalField(TechnologyFields.MINIMAL_QUANTITY);
        if (!Objects.isNull(minimalQuantity)) {
            String unit = technology.getBelongsToField(TechnologyFields.PRODUCT).getStringField(ProductFields.UNIT);

            if (plannedQuantity.compareTo(minimalQuantity) < 0) {
                order.addGlobalMessage("orders.order.minimalQuantity.info", false, false,
                        order.getStringField(OrderFields.NUMBER), BigDecimalUtils.toString(minimalQuantity, 5), unit);
            }
        }
    }

    private boolean checkOperationalTasks(final DataDefinition orderDD, final Entity order) {
        if (!order.getHasManyField(OrderFields.OPERATIONAL_TASKS).isEmpty()) {
            Entity orderFromDB = orderDD.get(order.getId());

            if (!order.getBelongsToField(OrderFields.PRODUCT).equals(orderFromDB.getBelongsToField(OrderFields.PRODUCT))) {
                order.addError(orderDD.getField(OrderFields.PRODUCT),
                        "orders.validate.global.error.operationalTasks.productChange");

                return false;
            }
            if (!order.getBooleanField(OrderFields.NEW_VERSION_TECHNOLOGY_SET) && !order.getBelongsToField(OrderFields.TECHNOLOGY)
                    .equals(orderFromDB.getBelongsToField(OrderFields.TECHNOLOGY))) {
                order.addError(orderDD.getField(OrderFields.TECHNOLOGY),
                        "orders.validate.global.error.operationalTasks.technologyChange");

                return false;
            }
        }

        return true;
    }

    public boolean checkOrderDates(final DataDefinition orderDD, final Entity order) {
        DateRange orderDateRange = orderDatesService.getCalculatedDates(order);
        Date dateFrom = orderDateRange.getFrom();
        Date dateTo = orderDateRange.getTo();

        if (Objects.isNull(dateFrom) || Objects.isNull(dateTo) || dateTo.after(dateFrom)) {
            return true;
        }

        order.addError(orderDD.getField(OrderFields.FINISH_DATE), "orders.validate.global.error.datesOrder");

        return false;
    }

    public boolean checkOrderPlannedQuantity(final DataDefinition orderDD, final Entity order) {
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);

        if (Objects.isNull(product)) {
            return true;
        }

        BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);

        if (Objects.isNull(plannedQuantity)) {
            order.addError(orderDD.getField(OrderFields.PLANNED_QUANTITY), "orders.validate.global.error.plannedQuantityError");

            return false;
        } else {
            return true;
        }
    }

    private boolean checkProductQuantities(final DataDefinition orderDD, final Entity order) {
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);

        if (Objects.isNull(product)) {
            return true;
        }

        BigDecimal commissionedPlannedQuantity = order.getDecimalField(OrderFields.COMMISSIONED_PLANNED_QUANTITY);

        if (Objects.isNull(commissionedPlannedQuantity)) {
            order.addError(orderDD.getField(OrderFields.COMMISSIONED_PLANNED_QUANTITY),
                    "qcadooView.validate.field.error.missing");

            return false;
        }

        Long orderId = order.getId();

        if (Objects.nonNull(orderId)) {
            Entity orderFromDB = orderDD.get(orderId);

            BigDecimal commissionedCorrectedQuantity = order.getDecimalField(OrderFields.COMMISSIONED_CORRECTED_QUANTITY);
            BigDecimal commissionedCorrectedQuantityFromDB = orderFromDB
                    .getDecimalField(OrderFields.COMMISSIONED_CORRECTED_QUANTITY);
            String state = order.getStringField(OrderFields.STATE);

            if ((OrderState.ACCEPTED.getStringValue().equals(state) || OrderState.IN_PROGRESS.getStringValue().equals(state)
                    || OrderState.INTERRUPTED.getStringValue().equals(state))
                    && Objects.nonNull(commissionedCorrectedQuantityFromDB) && Objects.isNull(commissionedCorrectedQuantity)) {
                order.addError(orderDD.getField(OrderFields.COMMISSIONED_CORRECTED_QUANTITY),
                        "qcadooView.validate.field.error.missing");

                return false;
            }
        }

        return true;
    }

    private boolean checkOrderPacksQuantity(final DataDefinition orderDD, final Entity order) {
        BigDecimal sumQuantityOrderPacks = orderPackService.getSumQuantityOrderPacksForOrder(order);

        if (order.getDecimalField(OrderFields.PLANNED_QUANTITY).compareTo(sumQuantityOrderPacks) < 0) {
            order.addError(orderDD.getField(OrderFields.PLANNED_QUANTITY), ORDER_PACKS_VALIDATE_GLOBAL_ERROR_QUANTITY_ERROR);

            return false;
        }

        return checkProductQuantitiesForOrderPacks(orderDD, order, sumQuantityOrderPacks);
    }

    private boolean checkProductQuantitiesForOrderPacks(final DataDefinition orderDD, final Entity order,
                                                        final BigDecimal sumQuantityOrderPacks) {
        Long orderId = order.getId();

        if (Objects.nonNull(orderId)) {
            Entity orderFromDB = orderDD.get(orderId);

            if (orderFromDB.getDecimalField(OrderFields.PLANNED_QUANTITY)
                    .compareTo(order.getDecimalField(OrderFields.PLANNED_QUANTITY)) == 0) {
                BigDecimal commissionedCorrectedQuantity = order.getDecimalField(OrderFields.COMMISSIONED_CORRECTED_QUANTITY);

                if (Objects.nonNull(commissionedCorrectedQuantity)) {
                    if (commissionedCorrectedQuantity.compareTo(sumQuantityOrderPacks) < 0) {
                        order.addError(orderDD.getField(OrderFields.COMMISSIONED_CORRECTED_QUANTITY),
                                ORDER_PACKS_VALIDATE_GLOBAL_ERROR_QUANTITY_ERROR);

                        return false;
                    }
                } else {
                    BigDecimal commissionedPlannedQuantity = order.getDecimalField(OrderFields.COMMISSIONED_PLANNED_QUANTITY);

                    if (Objects.nonNull(commissionedPlannedQuantity)
                            && commissionedPlannedQuantity.compareTo(sumQuantityOrderPacks) < 0) {
                        order.addError(orderDD.getField(OrderFields.COMMISSIONED_PLANNED_QUANTITY),
                                ORDER_PACKS_VALIDATE_GLOBAL_ERROR_QUANTITY_ERROR);

                        return false;
                    }
                }
            } else if (orderFromDB.getDecimalField(OrderFields.PLANNED_QUANTITY)
                    .compareTo(order.getDecimalField(OrderFields.PLANNED_QUANTITY)) < 0) {
                order.addGlobalMessage("orderPacks.validate.global.message.quantityMessage");
            }
        }

        return true;
    }

    private boolean checkOrderTechnologicalProcessesQuantity(final DataDefinition orderDD, final Entity order) {
        Long orderId = order.getId();

        if (Objects.nonNull(orderId)) {
            Entity orderFromDB = orderDD.get(orderId);

            BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
            BigDecimal plannedQuantityFromDB = orderFromDB.getDecimalField(OrderFields.PLANNED_QUANTITY);

            List<Entity> orderTechnologicalProcesses = order.getHasManyField(OrderFields.ORDER_TECHNOLOGICAL_PROCESSES);

            if ((plannedQuantity.compareTo(plannedQuantityFromDB) != 0) && !orderTechnologicalProcesses.isEmpty()) {
                order.addGlobalMessage("orderTechnologicalProcesses.validate.global.message.quantityMessage");
            }
        }

        return true;
    }

    public void copyStartDate(final DataDefinition orderDD, final Entity order) {
        setStartDate(order);
        fillStartDate(order);
    }

    public void copyEndDate(final DataDefinition orderDD, final Entity order) {
        setEndDate(order);
        fillEndDate(order);
    }

    protected boolean checkReasonOfStartDateCorrection(final Entity parameter, final Entity order) {
        String state = order.getStringField(OrderFields.STATE);

        if (OrderState.ACCEPTED.getStringValue().equals(state)) {
            return !parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_DATE_FROM)
                    || checkReasonNeeded(order, OrderFields.CORRECTED_DATE_FROM, OrderFields.REASON_TYPES_CORRECTION_DATE_FROM,
                    "orders.order.commentReasonTypeCorrectionDateFrom.isRequired");
        }

        return true;
    }

    protected boolean checkReasonOfEndDateCorrection(final Entity parameter, final Entity order) {
        String orderState = order.getStringField(OrderFields.STATE);

        if (OrderState.ACCEPTED.getStringValue().equals(orderState) || OrderState.IN_PROGRESS.getStringValue().equals(orderState)
                || OrderState.INTERRUPTED.getStringValue().equals(orderState)) {
            return !parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_DATE_TO)
                    || checkReasonNeeded(order, OrderFields.CORRECTED_DATE_TO, OrderFields.REASON_TYPES_CORRECTION_DATE_TO,
                    "orders.order.commentReasonTypeCorrectionDateTo.isRequired");
        }

        return true;
    }

    private boolean checkReasonNeeded(final Entity order, final String dateFieldName, final String reasonTypeFieldName,
                                      final String messageTranslationKey) {
        if (Objects.nonNull(order.getField(dateFieldName)) && order.getHasManyField(reasonTypeFieldName).isEmpty()) {
            order.addError(order.getDataDefinition().getField(reasonTypeFieldName), messageTranslationKey);

            return false;
        }

        return true;
    }

    private boolean checkEffectiveDeviation(final Entity parameter, final Entity order) {
        long differenceForDateFrom = orderStateChangeReasonService.getEffectiveDateFromDifference(parameter, order);
        long differenceForDateTo = orderStateChangeReasonService.getEffectiveDateToDifference(parameter, order);

        String orderState = order.getStringField(OrderFields.STATE);

        // EFFECTIVE_DATE_FROM
        if (OrderState.COMPLETED.getStringValue().equals(orderState) || OrderState.ABANDONED.getStringValue().equals(orderState)
                || OrderState.IN_PROGRESS.getStringValue().equals(orderState)
                || OrderState.INTERRUPTED.getStringValue().equals(orderState)) {
            if (parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM)
                    && differenceForDateFrom > 0L) {
                final String differenceAsString = TimeConverterService
                        .convertTimeToString(String.valueOf(Math.abs(differenceForDateFrom)));

                checkEffectiveDeviationNeeded(order, OrderFields.EFFECTIVE_DATE_FROM,
                        OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_START,
                        "orders.order.reasonNeededWhenDelayedEffectiveDateFrom.isRequired", differenceAsString);
            }
            if (parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM)
                    && differenceForDateFrom < 0L) {
                final String differenceAsString = TimeConverterService
                        .convertTimeToString(String.valueOf(Math.abs(differenceForDateFrom)));

                checkEffectiveDeviationNeeded(order, OrderFields.EFFECTIVE_DATE_FROM,
                        OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_START,
                        "orders.order.reasonNeededWhenEarlierEffectiveDateFrom.isRequired", differenceAsString);
            }
        }

        // EFFECTIVE_DATE_TO
        if (OrderState.COMPLETED.getStringValue().equals(orderState)
                || OrderState.ABANDONED.getStringValue().equals(orderState)) {
            if (parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO)
                    && differenceForDateTo > 0L) {
                final String differenceAsString = TimeConverterService
                        .convertTimeToString(String.valueOf(Math.abs(differenceForDateTo)));

                checkEffectiveDeviationNeeded(order, OrderFields.EFFECTIVE_DATE_TO,
                        OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_END,
                        "orders.order.reasonNeededWhenDelayedEffectiveDateTo.isRequired", differenceAsString);
            }
            if (parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO)
                    && differenceForDateTo < 0L) {
                final String differenceAsString = TimeConverterService
                        .convertTimeToString(String.valueOf(Math.abs(differenceForDateTo)));
                checkEffectiveDeviationNeeded(order, OrderFields.EFFECTIVE_DATE_TO,
                        OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_END,
                        "orders.order.reasonNeededWhenEarlierEffectiveDateTo.isRequired", differenceAsString);
            }
        }

        return true;
    }

    private boolean checkEffectiveDeviationNeeded(final Entity order, final String dateFieldName,
                                                  final String reasonTypeFieldName, final String messageTranslationKey, final String differenceAsString) {
        if (Objects.nonNull(order.getField(dateFieldName)) && order.getHasManyField(reasonTypeFieldName).isEmpty()) {
            order.addError(order.getDataDefinition().getField(reasonTypeFieldName), messageTranslationKey, differenceAsString);

            return false;
        }

        return true;
    }

    private void setStartDate(final Entity order) {
        Long orderId = order.getId();

        if (Objects.isNull(orderId)) {
            return;
        }

        Date startDate = order.getDateField(OrderFields.START_DATE);

        if (Objects.isNull(startDate)) {
            return;
        }

        Entity orderFromDB = orderService.getOrder(orderId);

        String state = order.getStringField(OrderFields.STATE);

        Date startDateDB = new Date();

        if (Objects.nonNull(orderFromDB.getDateField(OrderFields.START_DATE))) {
            startDateDB = orderFromDB.getDateField(OrderFields.START_DATE);
        }
        if (OrderState.PENDING.getStringValue().equals(state) && !startDate.equals(startDateDB)) {
            order.setField(OrderFields.DATE_FROM, startDate);
        }
        if ((OrderState.IN_PROGRESS.getStringValue().equals(state) || OrderState.COMPLETED.getStringValue().equals(state)
                || OrderState.ABANDONED.getStringValue().equals(state)) && !startDate.equals(startDateDB)) {
            order.setField(OrderFields.EFFECTIVE_DATE_FROM, startDate);
        }
        if (OrderState.ACCEPTED.getStringValue().equals(state) && !startDateDB.equals(startDate)) {
            order.setField(OrderFields.CORRECTED_DATE_FROM, startDate);
        }
    }

    private void setEndDate(final Entity order) {
        Long orderId = order.getId();

        if (Objects.isNull(orderId)) {
            return;
        }

        Date finishDate = order.getDateField(OrderFields.FINISH_DATE);

        if (Objects.isNull(finishDate)) {
            return;
        }

        Entity orderFromDB = orderService.getOrder(orderId);

        String state = order.getStringField(OrderFields.STATE);

        Date finishDateDB = new Date();

        if (Objects.nonNull(orderFromDB.getDateField(OrderFields.FINISH_DATE))) {
            finishDateDB = orderFromDB.getDateField(OrderFields.FINISH_DATE);
        }
        if (OrderState.PENDING.getStringValue().equals(state) && !finishDateDB.equals(finishDate)) {
            order.setField(OrderFields.DATE_TO, finishDate);
        }
        if ((OrderState.COMPLETED.getStringValue().equals(state) || OrderState.ABANDONED.getStringValue().equals(state))
                && !finishDateDB.equals(finishDate)) {
            order.setField(OrderFields.EFFECTIVE_DATE_TO, finishDate);
        }
        if ((OrderState.ACCEPTED.getStringValue().equals(state) || OrderState.IN_PROGRESS.getStringValue().equals(state))
                && !finishDateDB.equals(finishDate)) {
            order.setField(OrderFields.CORRECTED_DATE_TO, finishDate);
        }
    }

    private void fillStartDate(final Entity order) {
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        if (Objects.isNull(dateRange.getFrom())) {
            order.setField(OrderFields.DATE_FROM, order.getField(OrderFields.START_DATE));
        } else {
            order.setField(OrderFields.START_DATE, dateRange.getFrom());
        }
    }

    private void fillEndDate(final Entity order) {
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        if (Objects.isNull(dateRange.getTo())) {
            order.setField(OrderFields.DATE_TO, order.getField(OrderFields.FINISH_DATE));
        } else {
            order.setField(OrderFields.FINISH_DATE, dateRange.getTo());
        }
    }

    public boolean validateDates(final DataDefinition orderDD, final Entity order) {
        Date effectiveDateFrom = order.getDateField(OrderFields.EFFECTIVE_DATE_FROM);
        Date effectiveDateTo = order.getDateField(OrderFields.EFFECTIVE_DATE_TO);

        if (Objects.nonNull(effectiveDateFrom) && Objects.nonNull(effectiveDateTo) && effectiveDateTo.before(effectiveDateFrom)) {
            order.addError(orderDD.getField(OrderFields.EFFECTIVE_DATE_TO), "orders.validate.global.error.effectiveDateTo");

            return false;
        }

        return true;
    }

    public void copyProductQuantity(final DataDefinition orderDD, final Entity order) {
        Long orderId = order.getId();

        if (Objects.isNull(orderId)) {
            return;
        }

        Entity orderFromDB = orderService.getOrder(orderId);

        BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
        BigDecimal commissionedPlannedQuantity = order.getDecimalField(OrderFields.COMMISSIONED_PLANNED_QUANTITY);
        BigDecimal commissionedCorrectedQuantity = order.getDecimalField(OrderFields.COMMISSIONED_CORRECTED_QUANTITY);
        BigDecimal plannedQuantityFromDB = orderFromDB.getDecimalField(OrderFields.PLANNED_QUANTITY);

        if (plannedQuantityFromDB.compareTo(plannedQuantity) != 0) {
            String state = order.getStringField(OrderFields.STATE);

            if (OrderState.PENDING.getStringValue().equals(state)) {
                order.setField(OrderFields.COMMISSIONED_PLANNED_QUANTITY,
                        numberService.setScaleWithDefaultMathContext(plannedQuantity));
            }
            if (OrderState.ACCEPTED.getStringValue().equals(state) || OrderState.IN_PROGRESS.getStringValue().equals(state)
                    || OrderState.INTERRUPTED.getStringValue().equals(state)) {
                order.setField(OrderFields.COMMISSIONED_CORRECTED_QUANTITY,
                        numberService.setScaleWithDefaultMathContext(plannedQuantity));
            }
        } else {
            Entity product = order.getBelongsToField(OrderFields.PRODUCT);

            if (Objects.nonNull(commissionedCorrectedQuantity)) {
                if (commissionedCorrectedQuantity.compareTo(BigDecimal.ZERO) == 0) {
                    order.addError(orderDD.getField(OrderFields.COMMISSIONED_CORRECTED_QUANTITY), "qcadooView.validate.field.error.outOfRange.toSmall");
                }
                order.setField(OrderFields.PLANNED_QUANTITY,
                        numberService.setScaleWithDefaultMathContext(commissionedCorrectedQuantity));
                order.setField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT,
                        numberService.setScaleWithDefaultMathContext(additionalUnitService.getQuantityAfterConversion(order,
                                additionalUnitService.getAdditionalUnit(product),
                                numberService.setScaleWithDefaultMathContext(commissionedCorrectedQuantity),
                                product.getStringField(ProductFields.UNIT))));
            } else if (Objects.nonNull(commissionedPlannedQuantity)) {
                if (commissionedPlannedQuantity.compareTo(BigDecimal.ZERO) == 0) {
                    order.addError(orderDD.getField(OrderFields.COMMISSIONED_PLANNED_QUANTITY), "qcadooView.validate.field.error.outOfRange.toSmall");
                }
                order.setField(OrderFields.PLANNED_QUANTITY,
                        numberService.setScaleWithDefaultMathContext(commissionedPlannedQuantity));
                order.setField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT,
                        numberService.setScaleWithDefaultMathContext(additionalUnitService.getQuantityAfterConversion(order,
                                additionalUnitService.getAdditionalUnit(product),
                                numberService.setScaleWithDefaultMathContext(commissionedPlannedQuantity),
                                product.getStringField(ProductFields.UNIT))));
            }
        }

        BigDecimal doneQuantityFromDB = orderFromDB.getDecimalField(OrderFields.DONE_QUANTITY);
        BigDecimal doneQuantity = order.getDecimalField(OrderFields.DONE_QUANTITY);
        BigDecimal amountOfProductProducedFromDB = orderFromDB.getDecimalField(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED);
        BigDecimal amountOfProductProduced = order.getDecimalField(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED);

        String typeOfProductionRecording = order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING);

        if (StringUtils.isEmpty(typeOfProductionRecording)) {
            if (BigDecimalUtils.convertNullToZero(doneQuantity)
                    .compareTo(BigDecimalUtils.convertNullToZero(doneQuantityFromDB)) != 0) {
                order.setField(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED,
                        numberService.setScaleWithDefaultMathContext(doneQuantity));
            } else if (BigDecimalUtils.convertNullToZero(amountOfProductProduced)
                    .compareTo(BigDecimalUtils.convertNullToZero(amountOfProductProducedFromDB)) != 0) {
                order.setField(OrderFields.DONE_QUANTITY, numberService.setScaleWithDefaultMathContext(amountOfProductProduced));
            }
        } else {
            order.setField(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED, numberService.setScaleWithDefaultMathContext(doneQuantity));
        }
    }

    public void onCorrectingTheRequestedVolume(final DataDefinition orderDD, final Entity order) {
        if (!neededWhenCorrectingTheRequestedVolume()) {
            return;
        }

        Long orderId = order.getId();

        if (Objects.isNull(orderId)) {
            return;
        }

        String state = order.getStringField(OrderFields.STATE);

        if (OrderState.ACCEPTED.getStringValue().equals(state) || OrderState.IN_PROGRESS.getStringValue().equals(state)
                || OrderState.INTERRUPTED.getStringValue().equals(state)) {

            Entity orderFromDB = orderService.getOrder(orderId);

            BigDecimal commissionedCorrectedQuantity = order.getDecimalField(OrderFields.COMMISSIONED_CORRECTED_QUANTITY);
            BigDecimal commissionedCorrectedQuantityFromDB = orderFromDB
                    .getDecimalField(OrderFields.COMMISSIONED_CORRECTED_QUANTITY);

            if ((BigDecimalUtils.convertNullToZero(commissionedCorrectedQuantity)
                    .compareTo(BigDecimalUtils.convertNullToZero(commissionedCorrectedQuantityFromDB)) != 0)
                    && order.getHasManyField(OrderFields.TYPE_OF_CORRECTION_CAUSES).isEmpty()) {
                order.addGlobalError("orders.order.correctingQuantity.missingTypeOfCorrectionCauses");
            }
        }
    }

    public boolean neededWhenCorrectingTheRequestedVolume() {
        return parameterService.getParameter()
                .getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_THE_REQUESTED_VOLUME);
    }

    public void setCommissionedPlannedQuantity(final DataDefinition orderDD, final Entity order) {
        if (Objects.isNull(order)) {
            return;
        }

        Object quantity = order.getField(OrderFields.PLANNED_QUANTITY);

        if (Objects.nonNull(quantity) && BigDecimalUtils.checkIfCorrectDecimalValue(order, OrderFields.PLANNED_QUANTITY)) {
            order.setField(OrderFields.COMMISSIONED_PLANNED_QUANTITY,
                    numberService.setScaleWithDefaultMathContext(order.getDecimalField(OrderFields.PLANNED_QUANTITY)));
        }
    }

    private void copyAttachments(Entity order) {
        List<Entity> orderAttachments = order.getHasManyField("orderAttachments");

        for (Entity attachment : orderAttachments) {
            File file = fileService.getFileFromFilenameWithRandomDirectory(attachment
                    .getStringField(OrderAttachmentFields.NAME));
            try (InputStream is = fileService.getInputStream(attachment
                    .getStringField(OrderAttachmentFields.ATTACHMENT));
                 OutputStream output = Files.newOutputStream(file.toPath())) {
                IOUtils.copy(is, output);
            } catch (IOException e) {
                throw new IllegalStateException("Problem with order attachments");
            }
            attachment.setField(OrderAttachmentFields.ATTACHMENT, file);
            attachment.getDataDefinition().save(attachment);

        }
    }


    public void setProductQuantity(final DataDefinition orderDD, final Entity order) {
        if (Objects.isNull(order)) {
            return;
        }

        order.setField(OrderFields.COMMISSIONED_PLANNED_QUANTITY, order.getDecimalField(OrderFields.PLANNED_QUANTITY));
        order.setField(OrderFields.COMMISSIONED_CORRECTED_QUANTITY, null);
        order.setField(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED, null);
        order.setField(OrderFields.REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE, null);
    }

    public void clearOrSetSpecyfiedValueOrderFieldsOnCopy(final DataDefinition orderDD, final Entity order) {
        order.setField(OrderFields.STATE, OrderState.PENDING.getStringValue());
        order.setField(OrderFields.EFFECTIVE_DATE_TO, null);
        order.setField(OrderFields.EFFECTIVE_DATE_FROM, null);
        order.setField(OrderFields.CORRECTED_DATE_FROM, null);
        order.setField(OrderFields.CORRECTED_DATE_TO, null);
        order.setField(OrderFields.DATE_FROM, order.getDateField(OrderFields.START_DATE));
        order.setField(OrderFields.DATE_TO, order.getDateField(OrderFields.FINISH_DATE));
        order.setField(OrderFields.DONE_QUANTITY, null);
        order.setField(OrderFields.WASTES_QUANTITY, null);
        order.setField(OrderFields.EXTERNAL_NUMBER, null);
        order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
        order.setField(OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_FROM, null);
        order.setField(OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO, null);
        order.setField(OrderFields.COMMENT_REASON_DEVIATION_EFFECTIVE_END, null);
        order.setField(OrderFields.COMMENT_REASON_DEVIATION_EFFECTIVE_START, null);
        order.setField(OrderFields.COMMENT_REASON_TYPE_DEVIATIONS_QUANTITY, null);
    }

    public boolean onDelete(final DataDefinition orderDD, final Entity order) {
        List<Entity> orderTechnologicalProcesses = order.getHasManyField(OrderFields.ORDER_TECHNOLOGICAL_PROCESSES);

        if (!orderTechnologicalProcesses.isEmpty()) {
            order.addGlobalError("orders.validate.global.error.orderTechnologicalProcessesExists");

            return false;
        }

        return true;
    }

}
