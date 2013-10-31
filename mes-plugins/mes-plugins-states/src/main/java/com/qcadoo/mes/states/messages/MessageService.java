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
package com.qcadoo.mes.states.messages;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.Entity;

/**
 * State change messages service
 * 
 * @since 1.1.7
 */
public interface MessageService {

    /**
     * Create new message entity
     * 
     * @param type
     *            type of message
     * @param correspondField
     * @param translationKey
     * @param translationArgs
     * @return newly created message entity (not persisted yet!)
     */
    Entity createMessage(final String translationKey, final StateMessageType type, final boolean autoClose,
            final String correspondField, final String... translationArgs);

    /**
     * Check if given message entity already exists
     * 
     * @param message
     *            entity you are looking for.
     * @return true if at least one equal entity was found.
     */
    boolean messageAlreadyExists(final Entity message);

    /**
     * Add message to state change entity
     * 
     * @param stateChangeEntity
     * @param message
     */
    void addMessage(final StateChangeContext stateChangeContext, final Entity message);

    /**
     * Create & add message to state change entity
     * 
     * @param stateChangeEntity
     * @param describer
     * @param type
     * @param correspondFieldName
     * @param translationKey
     * @param translationArgs
     */
    void addMessage(final StateChangeContext stateChangeContext, final StateMessageType type, final boolean autoClose,
            final String correspondFieldName, final String translationKey, final String... translationArgs);

    /**
     * Create & add validation error message to state change entity
     * 
     * @param stateChangeEntity
     * @param describer
     * @param correspondField
     * @param translationKey
     * @param translationArgs
     */
    void addValidationError(final StateChangeContext stateChangeContext, final String correspondField,
            final String translationKey, final String... translationArgs);

    /**
     * Create & add validation error message to state change entity
     * 
     * @param stateChangeEntity
     * @param describer
     * @param correspondField
     * @param translationKey
     * @param autoClose
     * @param translationArgs
     */
    void addValidationError(final StateChangeContext stateChangeContext, final String correspondField,
            final String translationKey, final boolean autoClose, final String... translationArgs);
}
