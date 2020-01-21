package com.qcadoo.mes.stoppage.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.stoppage.constants.StoppageFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

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

}
