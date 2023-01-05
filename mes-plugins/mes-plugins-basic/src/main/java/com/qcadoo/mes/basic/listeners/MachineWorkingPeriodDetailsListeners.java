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
package com.qcadoo.mes.basic.listeners;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.MachineWorkingPeriodFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class MachineWorkingPeriodDetailsListeners {

    public void calculateWorkingTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent launchDateFieldComponent = (FieldComponent) view
                .getComponentByReference(MachineWorkingPeriodFields.LAUNCH_DATE);
        FieldComponent stopDateFieldComponent = (FieldComponent) view
                .getComponentByReference(MachineWorkingPeriodFields.STOP_DATE);
        FieldComponent workingTimeFieldComponent = (FieldComponent) view
                .getComponentByReference(MachineWorkingPeriodFields.WORKING_TIME);

        Date launch = DateUtils.parseDate(launchDateFieldComponent.getFieldValue());
        Date stop = DateUtils.parseDate(stopDateFieldComponent.getFieldValue());

        if (launch != null && stop != null) {
            if (launch.before(stop)) {
                Seconds seconds = Seconds.secondsBetween(new DateTime(launch), new DateTime(stop));
                workingTimeFieldComponent.setFieldValue(seconds.getSeconds());
            }
            workingTimeFieldComponent.requestComponentUpdateState();
        } else {
            workingTimeFieldComponent.setFieldValue(null);
            workingTimeFieldComponent.requestComponentUpdateState();
        }
    }
}
