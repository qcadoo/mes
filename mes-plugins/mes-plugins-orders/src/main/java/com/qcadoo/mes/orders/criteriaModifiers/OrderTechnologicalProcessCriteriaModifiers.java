package com.qcadoo.mes.orders.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OrderTechnologicalProcessCriteriaModifiers {

    public void filterByOrderState(final SearchCriteriaBuilder scb) {
        scb.createAlias(OrderTechnologicalProcessFields.ORDER, OrderTechnologicalProcessFields.ORDER, JoinType.LEFT);
        scb.add(SearchRestrictions
                .not(SearchRestrictions.in(OrderTechnologicalProcessFields.ORDER + "." + OrderFields.STATE, Lists.newArrayList(
                        OrderStateStringValues.DECLINED, OrderStateStringValues.COMPLETED, OrderStateStringValues.ABANDONED))));
    }

}
