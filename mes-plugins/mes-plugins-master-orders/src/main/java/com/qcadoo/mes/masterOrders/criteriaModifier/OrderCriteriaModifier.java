package com.qcadoo.mes.masterOrders.criteriaModifier;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

@Service
public class OrderCriteriaModifier {

    private static String MASTER_ORDER_NUMBER_FILTER_VALUE = "masterOrderNumber";

    public void filterByMasterOrderNumber(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {

        if(filterValueHolder.has(MASTER_ORDER_NUMBER_FILTER_VALUE)){
            String masterOrderNumber = filterValueHolder.getString(MASTER_ORDER_NUMBER_FILTER_VALUE);
            scb.add(SearchRestrictions.like(OrderFields.NUMBER, masterOrderNumber + "%"));
        }
    }

    public void putMasterOrderNumberFilter(final LookupComponent lookupComponent, final String masterOrderNumber){
        FilterValueHolder valueHolder = lookupComponent.getFilterValue();
        valueHolder.put(MASTER_ORDER_NUMBER_FILTER_VALUE, masterOrderNumber);
        lookupComponent.setFilterValue(valueHolder);
    }

    public void clearMasterOrderNumberFilter(LookupComponent lookupComponent) {
        FilterValueHolder valueHolder = lookupComponent.getFilterValue();
        valueHolder.remove(MASTER_ORDER_NUMBER_FILTER_VALUE);
        lookupComponent.setFilterValue(valueHolder);

    }
}
