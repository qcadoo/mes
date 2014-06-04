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
package com.qcadoo.mes.states.service.client;

import com.qcadoo.model.api.Entity;

/**
 * State change client for Samples plugin.
 * 
 * @since marcinkubala
 */
public interface StateChangeSamplesClient {

    /**
     * @param entity
     *            an entity whose state will be changed
     * @param targetState
     *            target (new) state
     * @return entity whose state was changed
     */
    Entity changeState(final Entity entity, final String targetState);

    /**
     * Resume paused state change
     * 
     * @param entity
     *            an entity whose state change will be resumed
     * 
     * @param stateChangeEntity
     *            state change entity representing paused state change
     */
    void resumeStateChange(final Entity entity, final Entity stateChangeEntity);
}
