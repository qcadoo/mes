package com.qcadoo.mes.orders.listeners;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.constants.WorkstationChangeoverForOperationalTaskFields;
import com.qcadoo.mes.orders.hooks.WorkstationChangeoverForOperationalTaskDetailsHooks;
import com.qcadoo.model.api.IntegerUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class WorkstationChangeoverForOperationalTaskDetailsListeners {

    @Autowired
    private WorkstationChangeoverForOperationalTaskDetailsHooks workstationChangeoverForOperationalTaskDetailsHooks;

    public void clearOperationalTasksFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        workstationChangeoverForOperationalTaskDetailsHooks.setCurrentOperationalTask(view, null, true);
        workstationChangeoverForOperationalTaskDetailsHooks.setPreviousOperationalTask(view, null, true);
    }

    public void clearAttributeValueLookups(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent fromAttributeValueLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.FROM_ATTRIBUTE_VALUE);
        LookupComponent toAttributeValueLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.TO_ATTRIBUTE_VALUE);

        fromAttributeValueLookup.setFieldValue(null);
        fromAttributeValueLookup.requestComponentUpdateState();
        toAttributeValueLookup.setFieldValue(null);
        toAttributeValueLookup.requestComponentUpdateState();
    }

    public void onDatesChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent startDateField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.START_DATE);
        FieldComponent finishDateField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE);
        FieldComponent durationField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.DURATION);

        Date startDate = DateUtils.parseDate(startDateField.getFieldValue());
        Date finishDate = DateUtils.parseDate(finishDateField.getFieldValue());

        if (Objects.nonNull(startDate) && Objects.nonNull(finishDate) && startDate.before(finishDate)) {
            Integer duration = Seconds.secondsBetween(new DateTime(startDate), new DateTime(finishDate)).getSeconds();

            durationField.setFieldValue(duration);
        }

        durationField.requestComponentUpdateState();
    }

    public void onDurationChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent startDateField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.START_DATE);
        FieldComponent finishDateField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE);
        FieldComponent durationField = (FieldComponent) view.getComponentByReference(WorkstationChangeoverForOperationalTaskFields.DURATION);

        Date startDate = DateUtils.parseDate(startDateField.getFieldValue());
        Integer duration = IntegerUtils.parse((String) durationField.getFieldValue());

        if (Objects.nonNull(startDate) && Objects.nonNull(duration)) {
            Date finishDate = new DateTime(startDate).plusSeconds(duration).toDate();

            finishDateField.setFieldValue(DateUtils.toDateTimeString(finishDate));
        }

        finishDateField.requestComponentUpdateState();
    }

}
