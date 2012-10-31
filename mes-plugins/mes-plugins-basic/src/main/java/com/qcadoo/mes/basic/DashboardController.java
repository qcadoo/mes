/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.basic;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.security.api.SecurityService;

@Controller
public final class DashboardController {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Value("${useCompressedStaticResources}")
    private boolean useCompressedStaticResources;

    @RequestMapping(value = "dashboard", method = RequestMethod.GET)
    public ModelAndView getDashboardView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("userLogin", securityService.getCurrentUserName());

        mav.addObject("translationsMap", translationService.getMessagesGroup("dashboard", locale));

        mav.setViewName("basic/dashboard");
        mav.addObject("useCompressedStaticResources", useCompressedStaticResources);
        return mav;
    }

}
