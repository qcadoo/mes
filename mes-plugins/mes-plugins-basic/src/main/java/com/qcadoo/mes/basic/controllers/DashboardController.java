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

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.basic.services.DashboardView;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugins.users.constants.QcadooUsersConstants;
import com.qcadoo.security.api.UserService;
import com.qcadoo.security.constants.UserFields;
import com.qcadoo.view.api.crud.CrudService;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
public class DashboardController {

    @Value("${daysForFirstLogin:3}")
    private Integer daysForFirstLogin;

    @Autowired
    private UserService userService;

    @Autowired
    private CrudService crudService;

    @Autowired
    private DashboardView dashboardView;

    @Autowired
    private UserBlockedController userBlockedController;

    @RequestMapping(value = "dashboard", method = RequestMethod.GET)
    public ModelAndView getDashboardView(@RequestParam final Map<String, String> arguments, final Locale locale, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        Entity currentUser = userService.getCurrentUserEntity();

        if (currentUser.getBooleanField(UserFields.AFTER_FIRST_PSWD_CHANGE)) {
            return dashboardView.getModelAndView(arguments, locale);
        } else {
            Date pswdLastChanged = currentUser.getDateField(UserFields.PSWD_LAST_CHANGED);

            if (checkIfTimeForLoginIsUp(pswdLastChanged)) {
                currentUser.setField(UserFields.IS_BLOCKED, true);

                currentUser = currentUser.getDataDefinition().save(currentUser);
            }

            if (currentUser.getBooleanField(UserFields.IS_BLOCKED)) {
                return userBlockedController.getUserBlockedView(locale, request, response);
            } else {
                JSONObject json = new JSONObject(ImmutableMap.of("form.id", currentUser.getId()));

                return crudService.prepareView(QcadooUsersConstants.PLUGIN_IDENTIFIER, QcadooUsersConstants.VIEW_USER_CHANGE_PASSWORD, ImmutableMap.of("context", json.toString()), locale);
            }
        }
    }

    private boolean checkIfTimeForLoginIsUp(final Date pswdLastChanged) {
        if (Objects.isNull(pswdLastChanged) || Objects.isNull(daysForFirstLogin)) {
            return false;
        }

        DateTime now = DateTime.now();
        DateTime end = new DateTime(pswdLastChanged).plusDays(daysForFirstLogin);

        return end.isBefore(now);
    }

}
