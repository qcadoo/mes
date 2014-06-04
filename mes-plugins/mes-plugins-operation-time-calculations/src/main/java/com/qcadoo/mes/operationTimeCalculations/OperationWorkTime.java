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
package com.qcadoo.mes.operationTimeCalculations;

public class OperationWorkTime {

    private Integer laborWorkTime;

    private Integer machineWorkTime;

    private Integer duration;

    public final Integer getLaborWorkTime() {
        return laborWorkTime;
    }

    public final void setLaborWorkTime(final Integer laborWorkTime) {
        this.laborWorkTime = laborWorkTime;
    }

    public final Integer getMachineWorkTime() {
        return machineWorkTime;
    }

    public final void setMachineWorkTime(final Integer machineWorkTime) {
        this.machineWorkTime = machineWorkTime;
    }

    public final Integer getDuration() {
        return duration;
    }

    public final void setDuration(final Integer duration) {
        this.duration = duration;
    }

}
