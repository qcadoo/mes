package com.qcadoo.mes.operationalTasksForOrders.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OrdersCriteriaModifiersOTFO {

    public void showWithProductionLines(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNotNull(OrderFields.PRODUCTION_LINE));
    }

}
