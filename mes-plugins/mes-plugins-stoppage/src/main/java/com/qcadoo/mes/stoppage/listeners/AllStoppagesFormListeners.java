package com.qcadoo.mes.stoppage.listeners;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.view.api.components.FieldComponent;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.stoppage.constants.StoppageFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

import java.util.Date;

@Service
public class AllStoppagesFormListeners {

    public void changeOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(StoppageFields.ORDER);
        LookupComponent productionTrackingLookup = (LookupComponent) view
                .getComponentByReference(StoppageFields.PRODUCTION_TRACKING);

        Entity order = orderLookup.getEntity();

        if (order != null) {
            FilterValueHolder holder = productionTrackingLookup.getFilterValue();

            holder.put(StoppageFields.ORDER, order.getId());

            productionTrackingLookup.setFilterValue(holder);
        }
    }

    public void calculateDuration(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent dateFromFieldComponent = (FieldComponent) view.getComponentByReference(StoppageFields.DATE_FROM);
        FieldComponent dateToFieldComponent = (FieldComponent) view.getComponentByReference(StoppageFields.DATE_TO);
        FieldComponent durationFieldComponent = (FieldComponent) view.getComponentByReference(StoppageFields.DURATION);

        Date dateFrom = DateUtils.parseDate(dateFromFieldComponent.getFieldValue());
        Date dateTo = DateUtils.parseDate(dateToFieldComponent.getFieldValue());

        if(dateFrom != null && dateTo != null) {
            if (dateFrom.before(dateTo)) {
                Seconds seconds = Seconds.secondsBetween(new DateTime(dateFrom), new DateTime(dateTo));
                durationFieldComponent.setFieldValue(seconds.getSeconds());
            }
            durationFieldComponent.requestComponentUpdateState();
        } else {
            durationFieldComponent.setFieldValue(null);
            durationFieldComponent.requestComponentUpdateState();
        }
    }

}
