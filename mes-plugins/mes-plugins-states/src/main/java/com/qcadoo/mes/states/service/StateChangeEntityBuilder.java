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
package com.qcadoo.mes.states.service;

import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.Entity;

/**
 * Service for creating state change entities
 * 
 * @since 1.1.7
 */
public interface StateChangeEntityBuilder {

    /**
     * Build state change entity for specified (not initial) state change
     * 
     * @param describer
     *            state change entity describer.
     * @param owner
     *            entity whose state will be changed.
     * @param targetState
     *            new state value.
     * @return new, not persisted state change entity
     */
    Entity build(final StateChangeEntityDescriber describer, final Entity owner, final StateEnum targetState);

    /**
     * Build state change entity for initial state.
     * 
     * @param describer
     *            state change entity describer.
     * @param initialState
     *            state enum value.
     * @return new, not persisted, initial state set entity.
     */
    Entity buildInitial(final StateChangeEntityDescriber describer, final Entity owner, final StateEnum initialState);

}
