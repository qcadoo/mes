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
package com.qcadoo.mes.orders;

import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.REASON_TYPES_CORRECTION_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.REASON_TYPES_CORRECTION_DATE_TO;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DELAYED_EFFECTIVE_DATE_FROM_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DELAYED_EFFECTIVE_DATE_TO_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.EARLIER_EFFECTIVE_DATE_FROM_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.EARLIER_EFFECTIVE_DATE_TO_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CHANGING_STATE_TO_ABANDONED;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CHANGING_STATE_TO_DECLINED;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CHANGING_STATE_TO_INTERRUPTED;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_DATE_TO;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.states.constants.OrderStateChangeFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class OrderStateChangeReasonService {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TranslationService translationService;

    public boolean neededWhenCorrectingDateFrom() {
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_CORRECTING_DATE_FROM);
    }

    public boolean neededWhenCorrectingDateTo() {
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_CORRECTING_DATE_TO);
    }

    public boolean neededForDecline() {
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_CHANGING_STATE_TO_DECLINED);
    }

    public boolean neededForInterrupt() {
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_CHANGING_STATE_TO_INTERRUPTED);
    }

    public boolean neededForAbandon() {
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_CHANGING_STATE_TO_ABANDONED);
    }

    public boolean neededWhenChangingEffectiveDateFrom() {
        return needForDelayedDateFrom() || needForEarlierDateFrom();
    }

    public boolean neededWhenChangingEffectiveDateTo() {
        return needForDelayedDateTo() || needForEarlierDateTo();
    }

    private boolean needForEarlierDateFrom() {
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM);
    }

    private boolean needForDelayedDateFrom() {
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM);
    }

    private boolean needForEarlierDateTo() {
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO);
    }

    private boolean needForDelayedDateTo() {
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO);
    }

    public long getEffectiveDateFromDifference(final Entity order) {
        if (!neededWhenChangingEffectiveDateFrom()) {
            return 0L;
        }
        final long dateFromTimestamp = getTimestampFromOrder(order, DATE_FROM, CORRECTED_DATE_FROM);
        if (dateFromTimestamp == 0L) {
            return 0L;
        }
        final DateTime dateFrom = new DateTime(dateFromTimestamp);
        final DateTime effectiveDateFrom = new DateTime(getEffectiveDate(order, EFFECTIVE_DATE_FROM));
        final DateTime maxTime = new DateTime(dateFromTimestamp
                + getAllowedDelayFromParametersAsMiliseconds(DELAYED_EFFECTIVE_DATE_FROM_TIME));
        final DateTime minTime = new DateTime(dateFromTimestamp
                - getAllowedDelayFromParametersAsMiliseconds(EARLIER_EFFECTIVE_DATE_FROM_TIME));

        long difference = 0L;
        if ((effectiveDateFrom.isAfter(maxTime) && needForDelayedDateFrom())
                || (effectiveDateFrom.isBefore(minTime) && needForEarlierDateFrom())) {
            difference = Seconds.secondsBetween(dateFrom, effectiveDateFrom).getSeconds();
        }
        return difference;
    }

    public long getEffectiveDateToDifference(final Entity order) {
        if (!neededWhenChangingEffectiveDateTo()) {
            return 0L;
        }
        final long dateToTimestamp = getTimestampFromOrder(order, DATE_TO, CORRECTED_DATE_TO);
        if (dateToTimestamp == 0L) {
            return 0L;
        }
        final DateTime dateTo = new DateTime(dateToTimestamp);
        final DateTime effectiveDateTo = new DateTime(getEffectiveDate(order, EFFECTIVE_DATE_TO));
        final DateTime maxTime = new DateTime(dateToTimestamp
                + getAllowedDelayFromParametersAsMiliseconds(DELAYED_EFFECTIVE_DATE_TO_TIME));
        final DateTime minTime = new DateTime(dateToTimestamp
                - getAllowedDelayFromParametersAsMiliseconds(EARLIER_EFFECTIVE_DATE_TO_TIME));

        long difference = 0L;
        if ((effectiveDateTo.isAfter(maxTime) && needForDelayedDateTo())
                || (effectiveDateTo.isBefore(minTime) && needForEarlierDateTo())) {
            difference = Seconds.secondsBetween(dateTo, effectiveDateTo).getSeconds();
        }
        return difference;
    }

    private int getAllowedDelayFromParametersAsMiliseconds(final String parameterName) {
        final Integer parameterValue = (Integer) parameterService.getParameter().getField(parameterName);
        int miliseconds = 0;
        if (parameterValue != null) {
            miliseconds = parameterValue.intValue() * 1000;
        }
        return miliseconds;
    }

    private Date getEffectiveDate(final Entity order, final String effectiveField) {
        Date effectiveDate = (Date) order.getField(effectiveField);
        if (effectiveDate == null) {
            effectiveDate = new Date();
        }
        return effectiveDate;
    }

    private long getTimestampFromOrder(final Entity order, final String dateField, final String correctedField) {
        final Date date = (Date) order.getField(dateField);
        final Date correctedDate = (Date) order.getField(correctedField);

        if (date == null) {
            return 0L;
        }

        if (correctedDate == null) {
            return date.getTime();
        } else {
            return correctedDate.getTime();
        }
    }

    public void onComplete(final StateChangeContext stateChangeContext, final ViewContextHolder viewContext) {
        final Entity order = stateChangeContext.getOwner();
        if (neededWhenCorrectingDateTo() && !hasRequiredCorrectionDateToReasonField(order)) {
            stateChangeContext.addFieldValidationError(REASON_TYPES_CORRECTION_DATE_TO,
                    "orders.order.stateChange.missingEndCorrectionReason");
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
            stateChangeContext.save();
            return;
        }
        final long difference = getEffectiveDateToDifference(order);
        if (difference == 0L) {
            return;
        }
        setAdditionalInfo(stateChangeContext, difference);
        showReasonForm(stateChangeContext, viewContext);
    }

    private boolean hasRequiredCorrectionDateFromReasonField(final Entity order) {
        return order.getField(CORRECTED_DATE_FROM) == null
                || (order.getField(REASON_TYPES_CORRECTION_DATE_FROM) != null && order.getField(CORRECTED_DATE_FROM) != null);
    }

    private boolean hasRequiredCorrectionDateToReasonField(final Entity order) {
        return order.getField(CORRECTED_DATE_TO) == null
                || (order.getField(REASON_TYPES_CORRECTION_DATE_TO) != null && order.getField(CORRECTED_DATE_TO) != null);
    }

    public void onStart(final StateChangeContext stateChangeContext, final ViewContextHolder viewContext) {
        final Entity order = stateChangeContext.getOwner();
        if (neededWhenCorrectingDateFrom() && !hasRequiredCorrectionDateFromReasonField(order)) {
            stateChangeContext.addFieldValidationError(REASON_TYPES_CORRECTION_DATE_FROM,
                    "orders.order.stateChange.missingStartCorrectionReason");
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
            stateChangeContext.save();
            return;
        }
        final long difference = getEffectiveDateFromDifference(order);
        if (difference == 0L) {
            return;
        }
        setAdditionalInfo(stateChangeContext, difference);
        showReasonForm(stateChangeContext, viewContext);
    }

    private void setAdditionalInfo(final StateChangeContext stateChangeContext, final long difference) {
        if (difference == 0L) {
            return;
        }
        final Entity stateChangeEntity = stateChangeContext.getStateChangeEntity();
        final StateChangeEntityDescriber describer = stateChangeContext.getDescriber();
        final OrderState orderState = (OrderState) stateChangeContext.getStateEnumValue(describer.getTargetStateFieldName());

        String additionalInfoKey = null;
        if (OrderState.IN_PROGRESS.equals(orderState)) {
            if (difference < 0L) {
                additionalInfoKey = "orders.order.stateChange.additionalInfo.startTooEarly";
            } else {
                additionalInfoKey = "orders.order.stateChange.additionalInfo.startTooLate";
            }
        } else if (OrderState.COMPLETED.equals(orderState)) {
            if (difference < 0L) {
                additionalInfoKey = "orders.order.stateChange.additionalInfo.endTooEarly";
            } else {
                additionalInfoKey = "orders.order.stateChange.additionalInfo.endTooLate";
            }
        } else {
            return;
        }

        final String differenceAsString = TimeConverterService.convertTimeToString(String.valueOf(Math.abs(difference)));
        final String additionalInfo = translationService.translate(additionalInfoKey, LocaleContextHolder.getLocale(),
                differenceAsString);
        stateChangeEntity.setField(OrderStateChangeFields.ADDITIONAL_INFO, additionalInfo);
        stateChangeContext.save();
    }

    public void showReasonForm(final StateChangeContext stateChangeContext, final ViewContextHolder viewContext) {
        stateChangeContext.setStatus(StateChangeStatus.PAUSED);
        stateChangeContext.save();
        viewContext.getViewDefinitionState().openModal(
                "../page/orders/orderStateChangeReasonDialog.html?context={\"form.id\": "
                        + stateChangeContext.getStateChangeEntity().getId() + "}");
    }
}
