package com.qcadoo.mes.orders;

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

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;

@Service
public class OrderStateChangeReasonService {

    @Autowired
    private ParameterService parameterService;

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

    public boolean isReasonNeededWhenChangingEffectiveDateFrom(final Entity order) {
        Date dateFrom = (Date) order.getField(DATE_FROM);
        Date correctedDateFrom = (Date) order.getField(OrderFields.CORRECTED_DATE_FROM);
        Date effectiveDateFrom = (Date) order.getField(EFFECTIVE_DATE_FROM);

        if ((dateFrom == null) || (effectiveDateFrom == null)) {
            return false;
        }

        Long dateFromTime = null;
        if (correctedDateFrom == null) {
            dateFromTime = dateFrom.getTime();
        } else {
            dateFromTime = correctedDateFrom.getTime();
        }

        Long effectiveDateFromTime = effectiveDateFrom.getTime();

        Long delayedEffectiveDateFromTime = (Long) parameterService.getParameter().getField(DELAYED_EFFECTIVE_DATE_FROM_TIME);
        Long earlierEffectiveDateFromTime = (Long) parameterService.getParameter().getField(EARLIER_EFFECTIVE_DATE_FROM_TIME);

        return ((parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM) && (dateFromTime > (effectiveDateFromTime + delayedEffectiveDateFromTime))) || (parameterService
                .getParameter().getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM) && (dateFromTime < (effectiveDateFromTime - earlierEffectiveDateFromTime))));
    }

    public boolean isReasonNeededWhenChangingEffectiveDateTo(final Entity order) {
        Date dateTo = (Date) order.getField(DATE_TO);
        Date correctedDateTo = (Date) order.getField(OrderFields.CORRECTED_DATE_TO);
        Date effectiveDateTo = (Date) order.getField(EFFECTIVE_DATE_TO);

        if ((dateTo == null) || (effectiveDateTo == null)) {
            return false;
        }

        Long dateToTime = null;
        if (correctedDateTo == null) {
            dateToTime = dateTo.getTime();
        } else {
            dateToTime = correctedDateTo.getTime();
        }

        Long effectiveDateToTime = effectiveDateTo.getTime();

        Long delayedEffectiveDateToTime = (Long) parameterService.getParameter().getField(DELAYED_EFFECTIVE_DATE_TO_TIME);
        Long earlierEffectiveDateToTime = (Long) parameterService.getParameter().getField(EARLIER_EFFECTIVE_DATE_TO_TIME);

        return ((parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO) && (dateToTime > (effectiveDateToTime + delayedEffectiveDateToTime))) || (parameterService
                .getParameter().getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO) && (dateToTime < (effectiveDateToTime - earlierEffectiveDateToTime))));
    }

}
