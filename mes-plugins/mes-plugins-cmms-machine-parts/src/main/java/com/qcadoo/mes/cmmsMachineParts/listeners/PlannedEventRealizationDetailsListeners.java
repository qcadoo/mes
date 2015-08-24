package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventRealizationFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class PlannedEventRealizationDetailsListeners {

    public void calculateDuration(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent startDateFieldComponent = (FieldComponent) view
                .getComponentByReference(PlannedEventRealizationFields.START_DATE);
        FieldComponent finishDateFieldComponent = (FieldComponent) view
                .getComponentByReference(PlannedEventRealizationFields.FINISH_DATE);
        FieldComponent durationFieldComponent = (FieldComponent) view
                .getComponentByReference(PlannedEventRealizationFields.DURATION);

        if (startDateFieldComponent.getFieldValue() == null || finishDateFieldComponent.getFieldValue() == null) {
            return;
        }

        Date start = DateUtils.parseDate(startDateFieldComponent.getFieldValue());
        Date end = DateUtils.parseDate(finishDateFieldComponent.getFieldValue());

        if (start.before(end)) {
            Seconds seconds = Seconds.secondsBetween(new DateTime(start), new DateTime(end));
            durationFieldComponent.setFieldValue(Integer.valueOf(seconds.getSeconds()));
        }
        durationFieldComponent.requestComponentUpdateState();
    }
}
