/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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
