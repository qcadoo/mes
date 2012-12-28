package com.qcadoo.mes.orders.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class OrdersPlanningListHooks {

    public final void addDiscriminatorRestrictionToGrid(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        grid.setCustomRestriction(new CustomRestriction() {

            @Override
            public void addRestriction(final SearchCriteriaBuilder searchBuilder) {
                searchBuilder.add(SearchRestrictions.or(
                        SearchRestrictions.eq(OrderFields.STATE, OrderState.PENDING.getStringValue()),
                        SearchRestrictions.eq(OrderFields.STATE, OrderState.IN_PROGRESS.getStringValue()),
                        SearchRestrictions.eq(OrderFields.STATE, OrderState.INTERRUPTED.getStringValue()),
                        SearchRestrictions.eq(OrderFields.STATE, OrderState.ACCEPTED.getStringValue())));
            }

        });
    }
}
