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
package com.qcadoo.mes.productionPerShift.hooks;

import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.idEq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.dates.ProgressDatesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;

@Service
public class OrderHooksPPS {

    @Autowired
    private ProgressDatesService progressDatesService;

    public void onUpdate(final DataDefinition orderDD, final Entity order) {
        setUpPpsDaysAndDatesFor(order);
    }

    void setUpPpsDaysAndDatesFor(final Entity order) {
        if (startDatesHasBeenChanged(order)) {
            progressDatesService.setUpDatesFor(order);
        }
    }

    private boolean startDatesHasBeenChanged(final Entity order) {
        SearchCriteriaBuilder scb = order.getDataDefinition().find();
        scb.setProjection(id());
        scb.add(idEq(order.getId()));
        for (String dateFieldName : Sets.newHashSet(OrderFields.DATE_FROM, OrderFields.CORRECTED_DATE_FROM,
                OrderFields.EFFECTIVE_DATE_FROM)) {
            scb.add(eq(dateFieldName, order.getDateField(dateFieldName)));
        }
        return scb.setMaxResults(1).uniqueResult() == null;
    }

}
