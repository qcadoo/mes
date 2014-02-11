/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProgressForDayHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void saveDateOfDay(final DataDefinition progressForDayDD, final Entity progressForDay) {
        Entity technology = progressForDay.getBelongsToField(ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT)
                .getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFields.TECHNOLOGY, technology)).setMaxResults(1).uniqueResult();

        Integer day = (Integer) progressForDay.getField(ProgressForDayFields.DAY);
        DateTime orderStartDate;

        if (progressForDay.getBooleanField(ProgressForDayFields.CORRECTED)) {
            if (order.getField(OrderFields.CORRECTED_DATE_FROM) == null) {
                orderStartDate = new DateTime(order.getDateField(OrderFields.DATE_FROM));
            } else {
                orderStartDate = new DateTime(order.getDateField(OrderFields.CORRECTED_DATE_FROM));
            }
        } else {
            orderStartDate = new DateTime(order.getDateField(OrderFields.DATE_FROM));
        }

        Date dayOfDay = orderStartDate.plusDays(day - 1).toDate();
        progressForDay.setField(ProgressForDayFields.DATE_OF_DAY, dayOfDay);
    }

}
