package com.qcadoo.mes.orders.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OrdersCriteriaModifiers {

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    private static final String L_FOR_EACH = "03forEach";

    public void showWithProductionLines(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNotNull(OrderFields.PRODUCTION_LINE));
        scb.add(SearchRestrictions.in(OrderFields.STATE,
                Lists.newArrayList(OrderState.PENDING.getStringValue(), OrderState.ACCEPTED.getStringValue())));
        scb.add(SearchRestrictions.eq(L_TYPE_OF_PRODUCTION_RECORDING, L_FOR_EACH));
    }

    public void showWithAppropriateStateForOrderPack(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.in(OrderFields.STATE, Lists.newArrayList(OrderState.ACCEPTED.getStringValue(),
                OrderState.IN_PROGRESS.getStringValue(), OrderState.INTERRUPTED.getStringValue())));
    }

    public void showNotCompletedDeclinedAndAbandoned(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.not(SearchRestrictions.in(OrderFields.STATE, Lists.newArrayList(OrderState.COMPLETED.getStringValue(),
                OrderState.DECLINED.getStringValue(), OrderState.ABANDONED.getStringValue()))
        ));
    }

}
