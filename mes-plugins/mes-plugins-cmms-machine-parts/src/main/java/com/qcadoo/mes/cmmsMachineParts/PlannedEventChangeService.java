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
package com.qcadoo.mes.cmmsMachineParts;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.cmmsMachineParts.constants.ParameterFieldsCMP;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.model.api.Entity;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

@Service
public class PlannedEventChangeService {

    @Autowired
    private ParameterService parameterService;

    public void showReasonForm(final StateChangeContext stateChangeContext, final ViewContextHolder viewContext) {
        stateChangeContext.setStatus(StateChangeStatus.PAUSED);
        stateChangeContext.save();

        viewContext.getViewDefinitionState().openModal(
                "../page/cmmsMachineParts/plannedEventStateChangeReasonDialog.html?context={\"form.id\": "
                        + stateChangeContext.getStateChangeEntity().getId() + "}");
    }

    public void updatePlannedEventFinishDate(final StateChangeContext stateChangeContext) {
        Entity plannedEvent = stateChangeContext.getOwner();

        Entity parameter = parameterService.getParameter();

        if (parameter.getBooleanField(ParameterFieldsCMP.UPDATE_PLANNED_EVENT_FINISH_DATE)) {
            plannedEvent.setField(PlannedEventFields.FINISH_DATE, DateTime.now().toDate());
        }
    }

}
