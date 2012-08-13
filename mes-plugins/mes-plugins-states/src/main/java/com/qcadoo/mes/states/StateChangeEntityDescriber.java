/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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

import com.qcadoo.model.api.DataDefinition;

/**
 * State change entity describer is a kind of adapter for different models describing specific state change entities.
 * 
 * @since 1.1.7
 */
public interface StateChangeEntityDescriber {

    /**
     * @return data definition for state change entity.
     */
    DataDefinition getDataDefinition();

    /**
     * Parse string value into proper enum object.
     * 
     * @param stringValue
     * @return
     */
    StateEnum parseStateEnum(final String stringValue);

    /**
     * @return name of belongsTo field pointing to owner entity.
     */
    String getOwnerFieldName();

    /**
     * @return name of string/enum field containing source (old) state value.
     */
    String getSourceStateFieldName();

    /**
     * @return name of string/enum field containing target (new) state value.
     */
    String getTargetStateFieldName();

    /**
     * @return name of boolean field containing finished flag.
     */
    String getStatusFieldName();

    /**
     * @return name of hasMany field containing collection of related messages.
     */
    String getMessagesFieldName();

    /**
     * @return name of Integer field containing number of phase.
     */
    String getPhaseFieldName();

    /**
     * @return name of DateTime field containing date & time of state change.
     */
    String getDateTimeFieldName();

    /**
     * @return name of belongsTo field pointing to shift entity.
     */
    String getShiftFieldName();

    /**
     * @return name of String field containing name of worker whose perform state change.
     */
    String getWorkerFieldName();

    /**
     * @return name of String field containing owner entity state.
     */
    String getOwnerStateFieldName();

    /**
     * @return data definition of owner entity.
     */
    DataDefinition getOwnerDataDefinition();

    /**
     * @return name of hasMany field containing state change entities.
     */
    String getOwnerStateChangesFieldName();

    /**
     * Check if any field using in this describer is missing.
     * 
     * @throws IllegalStateException
     *             if at least one field is missing.
     */
    void checkFields();
}
