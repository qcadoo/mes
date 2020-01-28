package com.qcadoo.mes.stoppage.criteriaModifiers;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Service;

@Service
public class StoppageOrdersCriteriaModifiers {

    public void showNotRejected(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.ne(OrderFields.STATE,
                OrderState.DECLINED.getStringValue()));
    }
}
