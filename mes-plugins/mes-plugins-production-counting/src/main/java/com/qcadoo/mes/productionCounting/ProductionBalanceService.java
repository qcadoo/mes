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
package com.qcadoo.mes.productionCounting;

import java.util.List;
import java.util.Map;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

public interface ProductionBalanceService {

    /**
     * Groups production trackings registered times
     * 
     * @param productionBalance
     *            production balance
     * @param productionTrackings
     *            production trackings
     * 
     * @return grouped production trackings registered times
     */
    Map<Long, Entity> groupProductionTrackingsRegisteredTimes(final Entity productionBalance,
            final List<Entity> productionTrackings);

    /**
     * Fills production trackings with planned times
     * 
     * @param productionBalance
     *            production balance
     * @param productionTrackings
     *            production trackings
     * 
     * @return production trackings with planned times
     */
    Map<Long, Map<String, Integer>> fillProductionTrackingsWithPlannedTimes(final Entity productionBalance,
            final List<Entity> productionTrackings);

    /**
     * Disables checkboxes
     * 
     * @param view
     *            view
     */
    void disableCheckboxes(final ViewDefinitionState view);

    /**
     * Fills fields and grids
     * 
     * @param productionBalance
     *            productionBalance
     */
    void fillFieldsAndGrids(final Entity productionBalance);

}
