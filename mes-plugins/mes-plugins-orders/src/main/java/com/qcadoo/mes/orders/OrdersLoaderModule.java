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
package com.qcadoo.mes.orders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.basic.services.DashboardButtonService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.plugin.api.Module;

@Component
public class OrdersLoaderModule extends Module {

    public static final String BASIC_DASHBOARD_BUTTON_IDENTIFIER_ORDERS_SCHEDULES_LIST = "basic.dashboardButton.identifier.orders.schedulesList";

    public static final String BASIC_DASHBOARD_BUTTON_IDENTIFIER_ORDERS_PRODUCTION_LINE_SCHEDULES_LIST = "basic.dashboardButton.identifier.orders.productionLineSchedulesList";

    public static final String BASIC_DASHBOARD_BUTTON_IDENTIFIER_ORDERS_ORDERS_PLANNING_LIST = "basic.dashboardButton.identifier.orders.ordersPlanningList";

    public static final String BASIC_DASHBOARD_BUTTON_IDENTIFIER_ORDERS_OPERATIONAL_TASKS_LIST = "basic.dashboardButton.identifier.orders.operationalTasksList";

    @Autowired
    private DashboardButtonService dashboardButtonService;

    @Transactional
    @Override
    public void multiTenantEnable() {
        dashboardButtonService.addButton(BASIC_DASHBOARD_BUTTON_IDENTIFIER_ORDERS_SCHEDULES_LIST,
                "/qcadooView/public/css/core/images/dashboard/schedules.png", OrdersConstants.PLUGIN_IDENTIFIER, "schedulesList");
        dashboardButtonService.addButton(BASIC_DASHBOARD_BUTTON_IDENTIFIER_ORDERS_PRODUCTION_LINE_SCHEDULES_LIST,
                "/qcadooView/public/css/core/images/dashboard/schedules.png", OrdersConstants.PLUGIN_IDENTIFIER, "productionLineSchedulesList");
        dashboardButtonService.addButton(BASIC_DASHBOARD_BUTTON_IDENTIFIER_ORDERS_ORDERS_PLANNING_LIST,
                "/qcadooView/public/css/core/images/dashboard/orders.png", OrdersConstants.PLUGIN_IDENTIFIER,
                "productionOrdersPlanning");
        dashboardButtonService.addButton(BASIC_DASHBOARD_BUTTON_IDENTIFIER_ORDERS_OPERATIONAL_TASKS_LIST,
                "/qcadooView/public/css/core/images/dashboard/operationalTasks.png", OrdersConstants.PLUGIN_IDENTIFIER,
                "operationalTasks");
    }

    @Transactional
    @Override
    public void multiTenantDisable() {
        dashboardButtonService.deleteButton(BASIC_DASHBOARD_BUTTON_IDENTIFIER_ORDERS_SCHEDULES_LIST);
        dashboardButtonService.deleteButton(BASIC_DASHBOARD_BUTTON_IDENTIFIER_ORDERS_PRODUCTION_LINE_SCHEDULES_LIST);
        dashboardButtonService.deleteButton(BASIC_DASHBOARD_BUTTON_IDENTIFIER_ORDERS_ORDERS_PLANNING_LIST);
        dashboardButtonService.deleteButton(BASIC_DASHBOARD_BUTTON_IDENTIFIER_ORDERS_OPERATIONAL_TASKS_LIST);
    }

}
