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
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.constants.OrderStateChangeFields;
import com.qcadoo.mes.states.StateChangeContext;
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
        return neededWhenChangingEffectiveDateFrom(parameterService.getParameter());
    }

    private boolean neededWhenChangingEffectiveDateFrom(final Entity parameter) {
        return needForDelayedDateFrom(parameter) || needForEarlierDateFrom(parameter);
    }

    private boolean neededWhenChangingEffectiveDateTo(final Entity parameter) {
        return needForDelayedDateTo(parameter) || needForEarlierDateTo(parameter);
    }

    private boolean needForEarlierDateFrom(final Entity parameter) {
        return parameter.getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM);
    }

    private boolean needForDelayedDateFrom(final Entity parameter) {
        return parameter.getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM);
    }

    private boolean needForEarlierDateTo(final Entity parameter) {
        return parameter.getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO);
    }

    private boolean needForDelayedDateTo(final Entity parameter) {
        return parameter.getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO);
    }

    public long getEffectiveDateFromDifference(final Entity parameter, final Entity order) {
        if (!neededWhenChangingEffectiveDateFrom(parameter)) {
            return 0L;
        }
        final long dateFromTimestamp = getTimestampFromOrder(order, DATE_FROM, CORRECTED_DATE_FROM);
        if (dateFromTimestamp == 0L) {
            return 0L;
        }
        final DateTime dateFrom = new DateTime(dateFromTimestamp);

        if (getEffectiveDate(order, EFFECTIVE_DATE_FROM) == null) {
            return 0L;
        }
        final DateTime effectiveDateFrom = new DateTime(getEffectiveDate(order, EFFECTIVE_DATE_FROM));
        final DateTime maxTime = new DateTime(dateFromTimestamp
                + getAllowedDelayFromParametersAsMiliseconds(DELAYED_EFFECTIVE_DATE_FROM_TIME));
        final DateTime minTime = new DateTime(dateFromTimestamp
                - getAllowedDelayFromParametersAsMiliseconds(EARLIER_EFFECTIVE_DATE_FROM_TIME));

        long difference = 0L;
        if ((effectiveDateFrom.isAfter(maxTime) && needForDelayedDateFrom(parameter))
                || (effectiveDateFrom.isBefore(minTime) && needForEarlierDateFrom(parameter))) {
            difference = Seconds.secondsBetween(dateFrom, effectiveDateFrom).getSeconds();
        }
        return difference;
    }

    public long getEffectiveDateToDifference(final Entity parameter, final Entity order) {
        if (!neededWhenChangingEffectiveDateTo(parameter)) {
            return 0L;
        }
        final long dateToTimestamp = getTimestampFromOrder(order, DATE_TO, CORRECTED_DATE_TO);
        if (dateToTimestamp == 0L) {
            return 0L;
        }
        final DateTime dateTo = new DateTime(dateToTimestamp);

        if (getEffectiveDate(order, EFFECTIVE_DATE_TO) == null) {
            return 0L;
        }
        final DateTime effectiveDateTo = new DateTime(getEffectiveDate(order, EFFECTIVE_DATE_TO));
        final DateTime maxTime = new DateTime(dateToTimestamp
                + getAllowedDelayFromParametersAsMiliseconds(DELAYED_EFFECTIVE_DATE_TO_TIME));
        final DateTime minTime = new DateTime(dateToTimestamp
                - getAllowedDelayFromParametersAsMiliseconds(EARLIER_EFFECTIVE_DATE_TO_TIME));

        long difference = 0L;
        if ((effectiveDateTo.isAfter(maxTime) && needForDelayedDateTo(parameter))
                || (effectiveDateTo.isBefore(minTime) && needForEarlierDateTo(parameter))) {
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
        return (Date) order.getField(effectiveField);
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

    public void showReasonForm(final StateChangeContext stateChangeContext, final ViewContextHolder viewContext) {
        final Entity order = stateChangeContext.getOwner();
        final Entity parameter = parameterService.getParameter();
        Long differenceForDateFrom = getEffectiveDateFromDifference(parameter, order);
        Long differenceForDateTo = getEffectiveDateToDifference(parameter, order);
        final Entity stateChangeEntity = stateChangeContext.getStateChangeEntity();
        String additionalInfoKey = null;
        String additionalInfo = null;
        // EFFECTIVE_DATE_FROM
        if (parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM)
                && differenceForDateFrom > 0L) {
            final String differenceAsString = TimeConverterService.convertTimeToString(String.valueOf(Math
                    .abs(differenceForDateFrom)));
            additionalInfoKey = "orders.order.stateChange.additionalInfo.startTooLate";
            additionalInfo = translationService.translate(additionalInfoKey, LocaleContextHolder.getLocale(), differenceAsString);
        }
        if (parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM)
                && differenceForDateFrom < 0L) {
            final String differenceAsString = TimeConverterService.convertTimeToString(String.valueOf(Math
                    .abs(differenceForDateFrom)));
            additionalInfoKey = "orders.order.stateChange.additionalInfo.startTooEarly";
            additionalInfo = translationService.translate(additionalInfoKey, LocaleContextHolder.getLocale(), differenceAsString);

        }
        // EFFECTIVE_DATE_TO
        if (parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO) && differenceForDateTo > 0L) {
            final String differenceAsString = TimeConverterService.convertTimeToString(String.valueOf(Math
                    .abs(differenceForDateTo)));
            additionalInfoKey = "orders.order.stateChange.additionalInfo.endTooLate";
            additionalInfo = translationService.translate(additionalInfoKey, LocaleContextHolder.getLocale(), differenceAsString);

        }
        if (parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO) && differenceForDateTo < 0L) {
            final String differenceAsString = TimeConverterService.convertTimeToString(String.valueOf(Math
                    .abs(differenceForDateTo)));
            additionalInfoKey = "orders.order.stateChange.additionalInfo.endTooEarly";
            additionalInfo = translationService.translate(additionalInfoKey, LocaleContextHolder.getLocale(), differenceAsString);
        }
        if (additionalInfo != null) {
            stateChangeEntity.setField(OrderStateChangeFields.ADDITIONAL_INFO, additionalInfo);
            stateChangeContext.save();
        }
        stateChangeContext.setStatus(StateChangeStatus.PAUSED);
        stateChangeContext.save();
        viewContext.getViewDefinitionState().openModal(
                "../page/orders/orderStateChangeReasonDialog.html?context={\"form.id\": "
                        + stateChangeContext.getStateChangeEntity().getId() + "}");
    }
}
