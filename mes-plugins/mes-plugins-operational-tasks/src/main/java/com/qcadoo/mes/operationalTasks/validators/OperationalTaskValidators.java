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
package com.qcadoo.mes.operationalTasks.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OperationalTaskValidators {

    public boolean compareDate(final DataDefinition dataDefinition, final Entity entity) {
        // Date startDate = (Date) entity.getField(OperationalTasksFields.START_DATE);
        // Date finishDate = (Date) entity.getField(OperationalTasksFields.FINISH_DATE);
        // if (startDate.compareTo(finishDate) == 1) {
        // entity.addError(dataDefinition.getField(OperationalTasksFields.START_DATE),
        // "operationalTasks.operationalTask.finishDateIsEarlier");
        // entity.addError(dataDefinition.getField(OperationalTasksFields.FINISH_DATE),
        // "operationalTasks.operationalTask.finishDateIsEarlier");
        // return false;
        // }
        return true;
    }
}
