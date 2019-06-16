/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.productionPerShift.dates;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.dataProvider.ProgressForDayDataProvider;
import com.qcadoo.model.api.Entity;

@Service
public class ProgressDatesService {

    @Autowired
    private ProgressForDayDataProvider progressForDayDataProvider;

    @Transactional
    public void setUpDatesFor(final Entity order) {
        List<Entity> progressesForDays = progressForDayDataProvider.findForOrder(order,
                ProgressForDayDataProvider.DEFAULT_SEARCH_ORDER);
        for (Entity progressForDay : progressesForDays) {
            if (progressForDay.getDateField(ProgressForDayFields.ACTUAL_DATE_OF_DAY) == null) {
                Date dateOfDay = progressForDay.getDateField(ProgressForDayFields.DATE_OF_DAY);
                progressForDay.setField(ProgressForDayFields.ACTUAL_DATE_OF_DAY, dateOfDay);
                progressForDay.getDataDefinition().save(progressForDay);
            }
        }
    }
}
