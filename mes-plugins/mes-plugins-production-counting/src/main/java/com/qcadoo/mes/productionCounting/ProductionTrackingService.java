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

import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

public interface ProductionTrackingService {

    /**
     * Sets time and piecework components visible
     * 
     * @param view
     *            view
     * @param order
     *            order
     */
    void setTimeAndPieceworkComponentsVisible(final ViewDefinitionState view, final Entity order);

    void setTimeAndPiecworkComponentsVisible(final String recordingType, final Entity order, final ViewDefinitionState view);

    ProductionTrackingState getTrackingState(final ViewDefinitionState view);

    void changeProducedQuantityFieldState(final ViewDefinitionState viewDefinitionState);

    void fillProductionLineLookup(final ViewDefinitionState view);

}
