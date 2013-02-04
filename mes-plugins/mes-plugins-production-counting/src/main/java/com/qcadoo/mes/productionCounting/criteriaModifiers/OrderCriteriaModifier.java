package com.qcadoo.mes.productionCounting.criteriaModifiers;

import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.states.constants.OrderState.COMPLETED;
import static com.qcadoo.mes.orders.states.constants.OrderState.INTERRUPTED;
import static com.qcadoo.mes.orders.states.constants.OrderState.IN_PROGRESS;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OrderCriteriaModifier {

    public void orderFilter(final SearchCriteriaBuilder scb) {

        final List<String> allowStatesList = Lists.newArrayList(IN_PROGRESS.getStringValue(), INTERRUPTED.getStringValue(),
                COMPLETED.getStringValue());

        scb.add(SearchRestrictions.and(
                SearchRestrictions.ne(TYPE_OF_PRODUCTION_RECORDING, TypeOfProductionRecording.BASIC.getStringValue()),
                SearchRestrictions.in(STATE, allowStatesList)));
    }
}
