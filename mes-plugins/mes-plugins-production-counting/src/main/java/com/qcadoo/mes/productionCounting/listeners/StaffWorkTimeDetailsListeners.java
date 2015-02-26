package com.qcadoo.mes.productionCounting.listeners;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.productionCounting.constants.StaffWorkTimeFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class StaffWorkTimeDetailsListeners {

    public void calculateLaborTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent startDateFieldComponent = (FieldComponent) view
                .getComponentByReference(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_START);
        FieldComponent endDateFieldComponent = (FieldComponent) view
                .getComponentByReference(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_END);
        FieldComponent laborTimeFieldComponent = (FieldComponent) view
                .getComponentByReference(StaffWorkTimeFields.LABOR_TIME);

        Date start = DateUtils.parseDate(startDateFieldComponent.getFieldValue());
        Date end = DateUtils.parseDate(endDateFieldComponent.getFieldValue());

        if (start.before(end)) {
            Seconds seconds = Seconds.secondsBetween(new DateTime(start), new DateTime(end));
            laborTimeFieldComponent.setFieldValue(Integer.valueOf(seconds.getSeconds()));
        }
        laborTimeFieldComponent.requestComponentUpdateState();
    }
}
