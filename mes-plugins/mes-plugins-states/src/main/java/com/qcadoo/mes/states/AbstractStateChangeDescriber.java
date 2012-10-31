/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.states;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.model.api.DataDefinition;

/**
 * This abstract class provides some default values to make concrete Describers more compact.
 * 
 * @since 1.1.7
 */
public abstract class AbstractStateChangeDescriber implements StateChangeEntityDescriber {

    @Override
    public String getSourceStateFieldName() {
        return "sourceState";
    }

    @Override
    public String getTargetStateFieldName() {
        return "targetState";
    }

    @Override
    public String getStatusFieldName() {
        return "status";
    }

    @Override
    public String getMessagesFieldName() {
        return "messages";
    }

    @Override
    public String getPhaseFieldName() {
        return "phase";
    }

    @Override
    public String getDateTimeFieldName() {
        return "dateAndTime";
    }

    @Override
    public String getShiftFieldName() {
        return "shift";
    }

    @Override
    public String getWorkerFieldName() {
        return "worker";
    }

    @Override
    public String getOwnerStateFieldName() {
        return "state";
    }

    @Override
    public String getOwnerStateChangesFieldName() {
        return "stateChanges";
    }

    @Override
    public void checkFields() {
        DataDefinition dataDefinition = getDataDefinition();
        List<String> fieldNames = Lists.newArrayList(getOwnerFieldName(), getSourceStateFieldName(), getTargetStateFieldName(),
                getStatusFieldName(), getMessagesFieldName(), getPhaseFieldName(), getDateTimeFieldName(), getShiftFieldName(),
                getWorkerFieldName());
        Set<String> uniqueFieldNames = Sets.newHashSet(fieldNames);
        checkState(fieldNames.size() == uniqueFieldNames.size(), "Describer methods should return unique field names.");

        Set<String> existingFieldNames = dataDefinition.getFields().keySet();
        checkState(existingFieldNames.containsAll(uniqueFieldNames), "DataDefinition for " + dataDefinition.getPluginIdentifier()
                + '.' + dataDefinition.getName() + " does not have all fields with name specified by this desciber.");
    }

}
