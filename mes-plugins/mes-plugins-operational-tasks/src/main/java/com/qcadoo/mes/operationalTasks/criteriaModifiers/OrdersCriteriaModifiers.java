package com.qcadoo.mes.operationalTasks.criteriaModifiers;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

import org.springframework.stereotype.Service;

@Service
public class OrdersCriteriaModifiers {

    public void showWithProductionLines(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNotNull(OrderFields.PRODUCTION_LINE));
        scb.add(SearchRestrictions.in(OrderFields.STATE, Lists.newArrayList(OrderState.PENDING.getStringValue(),
                OrderState.IN_PROGRESS.getStringValue())));
    }

}
