package com.qcadoo.mes.orders.criteriaModifires;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ScheduleOrderCriteriaModifier {

    public static final String SCHEDULE_PARAMETER = "scheduleId";

    public void filterByState(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.in(OrderFields.STATE,
                Lists.newArrayList(OrderState.ACCEPTED.getStringValue(), OrderState.PENDING.getStringValue())));
        if (filterValueHolder.has(SCHEDULE_PARAMETER)) {
            scb.createAlias(OrderFields.SCHEDULES, OrderFields.SCHEDULES, JoinType.LEFT);
            scb.add(SearchRestrictions.or(SearchRestrictions.ne(OrderFields.SCHEDULES + ".id",
                    filterValueHolder.getLong(SCHEDULE_PARAMETER)), SearchRestrictions.isNull(OrderFields.SCHEDULES + ".id")));
        }
    }
}
