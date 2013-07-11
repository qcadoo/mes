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
package com.qcadoo.mes.basicProductionCounting.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductionCountingQuantityDetailsListeners {

    private static final String L_PLANNED_QUANTITY_UNIT = "plannedQuantityUnit";

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    public void fillUnitFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        List<String> referenceNames = Lists.newArrayList(L_PLANNED_QUANTITY_UNIT);

        basicProductionCountingService.fillUnitFields(view, ProductionCountingQuantityFields.PRODUCT, referenceNames);
    }

    public void setTechnologyOperationComponentFieldRequired(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        basicProductionCountingService.setTechnologyOperationComponentFieldRequired(view);
    }

}
