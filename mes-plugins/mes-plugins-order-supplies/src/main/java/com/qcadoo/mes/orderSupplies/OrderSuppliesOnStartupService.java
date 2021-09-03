/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.orderSupplies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.orderSupplies.columnExtension.OrderSuppliesColumnLoader;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.mes.basic.services.DashboardButtonService;
import com.qcadoo.plugin.api.Module;

@Component
public class OrderSuppliesOnStartupService extends Module {

    private static final String BASIC_DASHBOARD_BUTTON_IDENTIFIER_REQUIREMENTS_GENERATE_MATERIAL_REQUIREMENT_COVERAGE = "basic.dashboardButton.identifier.requirements.generateMaterialRequirementCoverage";

    @Autowired
    private OrderSuppliesColumnLoader orderSuppliesColumnLoader;

    @Autowired
    private DashboardButtonService dashboardButtonService;

    @Transactional
    @Override
    public void multiTenantEnable() {
        orderSuppliesColumnLoader.addColumnsForCoverages();
        dashboardButtonService.addButton(BASIC_DASHBOARD_BUTTON_IDENTIFIER_REQUIREMENTS_GENERATE_MATERIAL_REQUIREMENT_COVERAGE,
                "/qcadooView/public/css/core/images/dashboard/materialRequirementCoverage.png",
                OrderSuppliesConstants.PLUGIN_IDENTIFIER, "generateMaterialRequirementCoverage");
    }

    @Transactional
    @Override
    public void multiTenantDisable() {
        orderSuppliesColumnLoader.deleteColumnsForCoverages();
        dashboardButtonService
                .deleteButton(BASIC_DASHBOARD_BUTTON_IDENTIFIER_REQUIREMENTS_GENERATE_MATERIAL_REQUIREMENT_COVERAGE);
    }

}
