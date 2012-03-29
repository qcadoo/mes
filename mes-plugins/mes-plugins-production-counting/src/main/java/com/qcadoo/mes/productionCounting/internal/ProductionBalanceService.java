/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package com.qcadoo.mes.productionCounting.internal;

import java.util.List;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public interface ProductionBalanceService {

    public void updateRecordsNumber(final DataDefinition productionBalanceDD, final Entity productionBalance);

    public void clearGeneratedOnCopy(final DataDefinition productionBalanceDD, final Entity productionBalance);

    public boolean validateOrder(final DataDefinition productionBalanceDD, final Entity productionBalance);

    public void generateProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args);

    public void printProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args);

    public boolean checkIfTypeOfProductionRecordingIsBasic(final Entity order);

    public List<Entity> getProductionRecordsFromDB(final Entity order);

    public Entity getProductionBalanceFromDB(final Long productionBalanceId);

    public Entity getOrderFromDB(final Long orderId);

    public Entity getCompanyFromDB();
}
