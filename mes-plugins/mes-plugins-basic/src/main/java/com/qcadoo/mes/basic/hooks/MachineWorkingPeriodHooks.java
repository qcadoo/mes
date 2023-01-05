/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.hooks;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.MachineWorkingPeriodFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MachineWorkingPeriodHooks {

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        Date launch = entity.getDateField(MachineWorkingPeriodFields.LAUNCH_DATE);
        Date stop = entity.getDateField(MachineWorkingPeriodFields.STOP_DATE);

        if (stop != null) {
            Seconds seconds = Seconds.secondsBetween(new DateTime(launch), new DateTime(stop));
            entity.setField(MachineWorkingPeriodFields.WORKING_TIME, seconds.getSeconds());
        } else {
            entity.setField(MachineWorkingPeriodFields.WORKING_TIME, 0);
        }
    }

}
