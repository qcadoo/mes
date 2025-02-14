/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.materialFlow;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.DashboardButtonFields;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.services.DashboardView;
import com.qcadoo.mes.materialFlow.constants.ParameterFieldsMF;
import com.qcadoo.mes.materialFlow.constants.WhatToShowOnDashboard;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.LogoComponent;
import com.qcadoo.view.constants.MenuItemFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@Primary
public class DashboardViewMF implements DashboardView {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private LogoComponent logoComponent;

    @Value("${useCompressedStaticResources}")
    private boolean useCompressedStaticResources;

    public ModelAndView getModelAndView(final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();

        Entity parameter = parameterService.getParameter();
        Entity currentUser = userService.getCurrentUserEntity();

        if (PluginUtils.isEnabled("configurator")
                && (securityService.hasCurrentUserRole("ROLE_ADMIN") || securityService.hasCurrentUserRole("ROLE_SUPERADMIN"))
                && !parameter.getBooleanField("applicationConfigured")) {
            mav.setViewName("configurator/configurator");

            mav.addObject("translationsMap", translationService.getMessagesGroup("configurator", locale));
            mav.addObject("locale", locale.getLanguage());
            mav.addObject("logoPath", logoComponent.prepareMenuLogoPath(false));

            return mav;
        }

        mav.setViewName("basic/dashboard");

        String whatToShowOnDashboard = getWhatToShowOnDashboard(parameter);
        if (WhatToShowOnDashboard.OPERATIONAL_TASKS.getStringValue().equals(whatToShowOnDashboard) &&
                !securityService.hasCurrentUserRole("ROLE_OPERATIONAL_TASKS")) {
            whatToShowOnDashboard = null;
        }
        mav.addObject("locale", locale.getLanguage());
        mav.addObject("translationsMap", translationService.getMessagesGroup("dashboard", locale));
        mav.addObject("useCompressedStaticResources", useCompressedStaticResources);
        mav.addObject("showChartOnDashboard", getShowChartOnDashboard(parameter));
        mav.addObject("showKanbanOnDashboard", securityService.hasCurrentUserRole("ROLE_DASHBOARD_KANBAN"));
        mav.addObject("enableCreateOrdersOnDashboard", securityService.hasCurrentUserRole("ROLE_DASHBOARD_KANBAN_CREATE_ORDERS"));
        mav.addObject("enableOrdersLinkOnDashboard", securityService.hasCurrentUserRole("ROLE_DASHBOARD_KANBAN_GOTO_ORDER_EDIT"));
        mav.addObject("enablePrintLabelOnDashboard", securityService.hasCurrentUserRole("ROLE_DASHBOARD_KANBAN_PRINT_LABEL"));
        mav.addObject("enableRegistrationTerminalOnDashboard", securityService.hasCurrentUserRole("ROLE_PRODUCTION_REGISTRATION_TERMINAL"));
        mav.addObject("whatToShowOnDashboard", whatToShowOnDashboard);
        mav.addObject("quantityMadeOnTheBasisOfDashboard", parameter.getStringField("quantityMadeOnTheBasisOfDashboard"));
        mav.addObject("dashboardButtons", filterDashboardButtons(getDashboardButtons(parameter), currentUser));

        if (arguments.containsKey("wizardToOpen")) {
            mav.addObject("wizardToOpen", arguments.get("wizardToOpen"));
        } else {
            mav.addObject("wizardToOpen", null);
        }

        return mav;
    }

    private boolean getShowChartOnDashboard(final Entity parameter) {
        return parameter.getBooleanField(ParameterFieldsMF.SHOW_CHART_ON_DASHBOARD);
    }

    private String getWhatToShowOnDashboard(final Entity parameter) {
        return parameter.getStringField(ParameterFieldsMF.WHAT_TO_SHOW_ON_DASHBOARD);
    }

    private LinkedList<Entity> getDashboardButtons(final Entity parameter) {
        return Lists.newLinkedList(parameter.getHasManyField(ParameterFields.DASHBOARD_BUTTONS).find()
                .add(SearchRestrictions.eq(DashboardButtonFields.ACTIVE, true))
                .addOrder(SearchOrders.asc(DashboardButtonFields.SUCCESSION)).list().getEntities());
    }

    private LinkedList<Entity> filterDashboardButtons(final LinkedList<Entity> dashboardButtons,
                                                      final Entity currentUser) {
        LinkedList<Entity> filteredDashboardButtons = Lists.newLinkedList();

        dashboardButtons.forEach(dashboardButton -> {
            Entity item = dashboardButton.getBelongsToField(DashboardButtonFields.ITEM);

            if (Objects.nonNull(item)) {
                String authRole = item.getStringField(MenuItemFields.AUTH_ROLE);

                if (securityService.hasRole(currentUser, authRole)) {
                    filteredDashboardButtons.add(dashboardButton);
                }
            }
        });

        return filteredDashboardButtons;
    }

}
