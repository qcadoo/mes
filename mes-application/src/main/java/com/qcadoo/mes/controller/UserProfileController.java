/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.crud.CrudController;

@Controller
public class UserProfileController {

    @Autowired
    private CrudController crudController;

    @Autowired
    private SecurityService securityService;

    @RequestMapping(value = "userProfile", method = RequestMethod.GET)
    public ModelAndView getAccessDeniedPageView(final Locale locale) {
        Map<String, String> arguments = new HashMap<String, String>();

        arguments.put("entityId", securityService.getCurrentUser().getId().toString());

        return crudController.getView("users", "userProfileView", arguments, locale);
    }
}
