/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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

package com.qcadoo.mes.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.SystemProperties;
import com.qcadoo.mes.api.TranslationService;

@Controller
public final class LoginController {

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ModelAndView getLoginPageView(@RequestParam(required = false) final String loginError,
            @RequestParam(required = false, defaultValue = "false") final Boolean iframe,
            @RequestParam(required = false, defaultValue = "false") final Boolean logout,
            @RequestParam(required = false, defaultValue = "false") final Boolean timeout, final Locale locale) {

        ModelAndView mav = new ModelAndView();
        mav.setViewName("core/login");
        mav.addObject("translation", translationService.getSecurityMessages(locale));
        mav.addObject("currentLanguage", locale.getLanguage());

        mav.addObject("iframe", iframe);

        if (logout) {
            mav.addObject("messageType", "success");
            mav.addObject("messageHeader", "security.message.logoutHeader");
            mav.addObject("messageContent", "security.message.logoutContent");
        } else if (timeout || iframe) {
            mav.addObject("messageType", "info");
            mav.addObject("messageHeader", "security.message.timeoutHeader");
            mav.addObject("messageContent", "security.message.timeoutContent");
        } else if (loginError != null) {
            mav.addObject("messageType", "error");
            mav.addObject("messageHeader", "security.message.errorHeader");
            mav.addObject("messageContent", "security.message.errorContent");
        }

        if (SystemProperties.getEnviroment().equals(SystemProperties.env.AMAZON)) {
            mav.addObject("isInAmazon", true);
        } else {
            mav.addObject("isInAmazon", false);
        }

        return mav;
    }

    @RequestMapping(value = "accessDenied", method = RequestMethod.GET)
    public ModelAndView getAccessDeniedPageView(final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("core/accessDenied");

        mav.addObject("translation", translationService.getSecurityMessages(locale));

        return mav;
    }
}
