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
package com.qcadoo.mes.states;

import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.messages.MessagesHolder;
import com.qcadoo.model.api.Entity;

/**
 * State change context holder object
 * 
 * @since 1.1.7
 */
public interface StateChangeContext extends MessagesHolder {

    /**
     * Save inner state change entity
     * 
     * @return false if occur any validation errors, true if valid.
     */
    boolean save();

    /**
     * Set inner state change entity's field
     * 
     * @param fieldName
     * @param fieldValue
     */
    void setField(final String fieldName, final Object fieldValue);

    /**
     * Returns state enum value from field with given name
     * 
     * @param fieldName
     * @return
     */
    StateEnum getStateEnumValue(final String fieldName);

    /**
     * Returns current phase number
     * 
     * @return current phase number
     */
    int getPhase();

    /**
     * Set current phase number
     * 
     * @param phase
     */
    void setPhase(final int phase);

    /**
     * Returns state change entity's status
     * 
     * @return state change entity's status
     */
    StateChangeStatus getStatus();

    /**
     * Set state change entity's status
     * 
     * @param status
     *            state change entity's status
     */
    void setStatus(final StateChangeStatus status);

    /**
     * Returns state change entity's describer
     * 
     * @return state change entity's describer
     */
    StateChangeEntityDescriber getDescriber();

    /**
     * Returns inner state change entity
     * 
     * @return inner state change entity
     */
    Entity getStateChangeEntity();

    /**
     * Save in database and set (if there is no validation errors) inner state change's owner entity
     * 
     * @param owner
     *            state change's owner entity
     * @return false if occur any validation errors, true if valid.
     */
    boolean setOwner(final Entity owner);

    /**
     * Returns entity which state is changing
     * 
     * @return entity which state is changing
     */
    Entity getOwner();

    /**
     * Checks if owner entity (entity which state is changing) has any validation results.
     * 
     * @return false if owner entity (entity which state is changing) has any validation results.
     */
    boolean isOwnerValid();

}
