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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderStateChangeFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
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

    public boolean isReasonNeededWhenCorrectingDateFrom() {
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_CORRECTING_DATE_FROM);
    }

    public boolean isReasonNeededWhenCorrectingDateTo() {
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
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM)
                || parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM);
    }

    public boolean neededWhenChangingEffectiveDateTo() {
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO)
                || parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO);
    }

    public long getEffectiveDateFromDifference(final Entity order) {
        if (!neededWhenChangingEffectiveDateFrom()) {
            return 0L;
        }
        final long dateFrom = getTimestampFromOrder(order, DATE_FROM, CORRECTED_DATE_FROM, EFFECTIVE_DATE_FROM);
        if (dateFrom == 0L) {
            return 0L;
        }

        final Date effectiveDateFrom = getEffectiveDate(order, EFFECTIVE_DATE_FROM);
        final long maxTime = effectiveDateFrom.getTime()
                + ((Integer) parameterService.getParameter().getField(DELAYED_EFFECTIVE_DATE_FROM_TIME)).longValue();
        final long minTime = effectiveDateFrom.getTime()
                - ((Integer) parameterService.getParameter().getField(EARLIER_EFFECTIVE_DATE_FROM_TIME)).longValue();

        long difference = 0L;
        if (dateFrom > maxTime && parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM)) {
            difference = dateFrom - maxTime;
        } else if (dateFrom < minTime
                && parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM)) {
            difference = dateFrom - maxTime;
        }
        return difference;
    }

    public long getEffectiveDateToDifference(final Entity order) {
        if (!neededWhenChangingEffectiveDateTo()) {
            return 0L;
        }
        final long dateTime = getTimestampFromOrder(order, DATE_TO, CORRECTED_DATE_TO, EFFECTIVE_DATE_TO);
        if (dateTime == 0) {
            return 0L;
        }

        final Date effectiveDateTo = getEffectiveDate(order, EFFECTIVE_DATE_TO);
        final long maxTime = effectiveDateTo.getTime()
                + ((Integer) parameterService.getParameter().getField(DELAYED_EFFECTIVE_DATE_TO_TIME)).longValue();
        final long minTime = effectiveDateTo.getTime()
                - ((Integer) parameterService.getParameter().getField(EARLIER_EFFECTIVE_DATE_TO_TIME)).longValue();

        long difference = 0L;
        if (dateTime > maxTime && parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO)) {
            difference = maxTime - dateTime;
        } else if (dateTime < minTime
                && parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO)) {
            difference = minTime - dateTime;
        }
        return difference;
    }

    private Date getEffectiveDate(final Entity order, final String effectiveField) {
        Date effectiveDate = (Date) order.getField(effectiveField);
        if (effectiveDate == null) {
            effectiveDate = new Date();
        }
        return effectiveDate;
    }

    private long getTimestampFromOrder(final Entity order, final String dateField, final String correctedField,
            final String effectiveField) {
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
        final long difference = getEffectiveDateToDifference(order);
        if (difference == 0L) {
            return;
        }
        setAdditionalInfo(stateChangeContext, difference);
        showReasonForm(stateChangeContext, viewContext);
    }

    public void onStart(final StateChangeContext stateChangeContext, final ViewContextHolder viewContext) {
        final Entity order = stateChangeContext.getOwner();
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

        final String differenceAsString = TimeConverterService.convertTimeToString(String.valueOf(difference));
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
