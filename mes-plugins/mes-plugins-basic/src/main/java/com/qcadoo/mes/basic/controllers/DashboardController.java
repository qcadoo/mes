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
package com.qcadoo.mes.basic.controllers;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.services.DashboardView;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private DashboardView dashboardView;

    @Autowired
    private UserBlockedController userBlockedController;

    @RequestMapping(value = "dashboard", method = RequestMethod.GET)
    public ModelAndView getDashboardView(@RequestParam final Map<String, String> arguments, final Locale locale, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        Entity currentUser = userService.getCurrentUserEntity();

        if (userBlockedController.isAfterFirstPswdChange(currentUser)) {
            return dashboardView.getModelAndView(arguments, locale);
        } else {
            return userBlockedController.redirectToProfileChangePasswordOrUserBlocked(locale, request, response, currentUser, BasicConstants.PLUGIN_IDENTIFIER + "." + BasicConstants.VIEW_HOME);
        }
    }

}
