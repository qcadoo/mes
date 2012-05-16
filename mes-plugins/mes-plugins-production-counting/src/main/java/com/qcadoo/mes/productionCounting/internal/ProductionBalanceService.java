/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.lowagie.text.DocumentException;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public interface ProductionBalanceService {

    void updateRecordsNumber(final DataDefinition productionBalanceDD, final Entity productionBalance);

    void clearGeneratedOnCopy(final DataDefinition productionBalanceDD, final Entity productionBalance);

    boolean validateOrder(final DataDefinition productionBalanceDD, final Entity productionBalance);

    void printProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args);

    void generateProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args);

    void generateProductionBalanceDocuments(final Entity productionBalance, final Locale locale) throws IOException,
            DocumentException;

    Map<Long, Entity> groupProductionRecords(final List<Entity> productionRecords);

    Map<Long, Map<String, Integer>> fillProductionRecordsWithPlannedTimes(final Entity productionBalance,
            final List<Entity> productionRecords);

    boolean checkIfTypeOfProductionRecordingIsBasic(final Entity order);

    Entity getProductionBalanceFromDB(final Long productionBalanceId);

    List<Entity> getProductionRecordsFromDB(final Entity order);

    Entity getTechnologyOperationComponentFromDB(final Long technologyOperationComponentId);

    Entity getOrderFromDB(final Long orderId);

    Entity getCompanyFromDB();

}
