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
package com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;

public class FieldsForMeterReading extends AbstractFieldsForType {

    public FieldsForMeterReading() {
        super(Lists.newArrayList(PlannedEventFields.EFFECTIVE_DURATION, PlannedEventFields.PLANNED_SEPARATELY,
                PlannedEventFields.REQUIRES_SHUTDOWN, PlannedEventFields.COUNTER, PlannedEventFields.DURATION,
                PlannedEventFields.EFFECTIVE_DURATION, PlannedEventFields.COMPANY),
                Lists.newArrayList(PlannedEventFields.MACHINE_PARTS_TAB,
                        PlannedEventFields.SOLUTION_DESCRIPTION_TAB, PlannedEventFields.RELATED_EVENTS_TAB,
                        PlannedEventFields.DOCUMENTS_TAB),
                Lists.newArrayList(PlannedEventFields.ACTIONS, PlannedEventFields.MACHINE_PARTS_FOR_EVENT));
    }

    @Override public boolean shouldLockBasedOn() {
        return true;
    }
}
