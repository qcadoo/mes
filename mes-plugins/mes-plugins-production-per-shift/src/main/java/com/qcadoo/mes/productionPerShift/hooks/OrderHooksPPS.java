package com.qcadoo.mes.productionPerShift.hooks;

import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.idEq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.dates.ProgressDatesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;

@Service
public class OrderHooksPPS {

    @Autowired
    private ProgressDatesService progressDatesService;

    public void onUpdate(final DataDefinition orderDD, final Entity order) {
        setUpPpsDaysAndDatesFor(order);
    }

    void setUpPpsDaysAndDatesFor(final Entity order) {
        if (startDatesHasBeenChanged(order)) {
            progressDatesService.setUpDatesFor(order);
        }
    }

    private boolean startDatesHasBeenChanged(final Entity order) {
        SearchCriteriaBuilder scb = order.getDataDefinition().find();
        scb.setProjection(id());
        scb.add(idEq(order.getId()));
        for (String dateFieldName : Sets.newHashSet(OrderFields.DATE_FROM, OrderFields.CORRECTED_DATE_FROM,
                OrderFields.EFFECTIVE_DATE_FROM)) {
            scb.add(eq(dateFieldName, order.getDateField(dateFieldName)));
        }
        return scb.setMaxResults(1).uniqueResult() == null;
    }

}
