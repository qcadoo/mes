/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.materialRequirements.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialRequirements.api.MaterialRequirementReportDataService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.print.ReportDataService;
import com.qcadoo.model.api.Entity;

@Service
public class MaterialRequirementReportDataServiceImpl implements MaterialRequirementReportDataService {

    @Autowired
    private ReportDataService reportDataService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Override
    public final Map<Entity, BigDecimal> getQuantitiesForMaterialRequirementProducts(
            final List<Entity> materialRequirementComponents, final Boolean onlyComponents) {
        List<Entity> orders = new ArrayList<Entity>();
        for (Entity component : materialRequirementComponents) {
            orders.add(component.getBelongsToField("order"));
        }
        return getQuantitiesForOrdersTechnologyProducts(orders, onlyComponents);
    }

    @Override
    public final Map<Entity, BigDecimal> getQuantitiesForOrdersTechnologyProducts(final List<Entity> orders,
            final Boolean onlyComponents) {
        return productQuantitiesService.getNeededProductQuantities(orders, onlyComponents);
    }
}
