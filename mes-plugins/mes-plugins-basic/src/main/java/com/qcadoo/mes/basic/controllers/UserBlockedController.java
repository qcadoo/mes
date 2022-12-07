/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.3
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

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.view.utils.ViewParametersAppender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Objects;

@Controller
public final class UserBlockedController {

    private static final String SYSTEM_ADMIN_CONTACT_MAIL = "pomoc@qcadoo.com";

    @Value("${systemAdminContactMail}")
    private String systemAdminContactMail;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ViewParametersAppender viewParametersAppender;

    @RequestMapping(value = "userBlocked", method = RequestMethod.GET)
    public ModelAndView getUserBlockedView(final Locale locale, final HttpServletRequest request, final HttpServletResponse response) {
        // invalidate current session
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (Objects.nonNull(authentication)) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        String adminEmail = getSystemAdminContactMail();

        ModelAndView mav = new ModelAndView();

        viewParametersAppender.appendCommonViewObjects(mav);

        mav.setViewName("basic/userBlocked");

        mav.addObject("translationsMap", translationService.getMessagesGroup("security", locale));
        mav.addObject("locales", translationService.getLocales());
        mav.addObject("adminEmail", "<a href=\"mailto:" + adminEmail + "\">" + adminEmail + "</a>");

        return mav;
    }

    public String getSystemAdminContactMail() {
        if (StringUtils.isBlank(systemAdminContactMail)) {
            systemAdminContactMail = SYSTEM_ADMIN_CONTACT_MAIL;
        }

        return systemAdminContactMail;
    }

}
