package com.qcadoo.mes.stoppage.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.stoppage.constants.StoppageFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ProductionTrackingsCriteriaModifiers {

    public void showForOrder(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(StoppageFields.ORDER)) {
            scb.add(SearchRestrictions.belongsTo(StoppageFields.ORDER, OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_ORDER, filterValue.getLong(StoppageFields.ORDER)));
        } else {
            scb.add(SearchRestrictions.belongsTo(StoppageFields.ORDER, OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_ORDER, 0L));
        }
    }
}
