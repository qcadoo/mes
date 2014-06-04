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

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.Entity;

public interface StateChangeContextBuilder {

    /**
     * Create new state change context. This method already create and persist state change entity.
     * 
     * @param describer
     * @param ownerEntity
     *            entity which state you want to be changed (state change's owner)
     * @return state change context object
     */
    StateChangeContext build(final StateChangeEntityDescriber describer, final Entity ownerEntity,
            final String targetStateString);

    /**
     * Build state change context from given state change entity.
     * 
     * @param describer
     * @param stateChangeEntity
     * @return state change context object
     */
    StateChangeContext build(final StateChangeEntityDescriber describer, final Entity stateChangeEntity);

}
