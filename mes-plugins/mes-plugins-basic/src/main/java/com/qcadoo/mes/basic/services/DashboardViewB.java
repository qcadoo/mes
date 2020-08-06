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
package com.qcadoo.mes.basic.services;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.security.api.SecurityService;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

@Service
public class DashboardViewB implements DashboardView {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Value("${useCompressedStaticResources}")
    private boolean useCompressedStaticResources;

    public ModelAndView getModelAndView(final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("basic/dashboard");
        mav.addObject("locale", locale.getLanguage());
        mav.addObject("translationsMap", translationService.getMessagesGroup("dashboard", locale));
        mav.addObject("useCompressedStaticResources", useCompressedStaticResources);

        return mav;
    }

}
