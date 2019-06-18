package com.qcadoo.mes.ordersForSubproductsGeneration.criteriaModifiers;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class OrdersForSPCriteriaModifiers {

    public static final String ORDERS_PARAMETER = "ordersID";

    public void showEntryData(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (!filterValue.has(ORDERS_PARAMETER)) {
            scb.add(SearchRestrictions.idEq(0L));
        } else if (filterValue.has(ORDERS_PARAMETER)) {
            String ids = filterValue.getString(ORDERS_PARAMETER);
            Iterable<Long> longIds = Longs.stringConverter()
                    .convertAll(Splitter.on(',').trimResults().omitEmptyStrings().splitToList(ids));
            List<Long> id = Lists.newArrayList(longIds);
            scb.add(SearchRestrictions.in("id", id));
        }
    }
}
