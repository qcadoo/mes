/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.mes.view.crud;

import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.model.api.aop.Monitorable;
import com.qcadoo.view.api.ViewDefinition;
import com.qcadoo.view.api.ViewDefinitionService;

@Controller
public class CrudController {

    private static final String VIEW_NAME_VARIABLE = "viewName";

    private static final String PLUGIN_IDENTIFIER_VARIABLE = "pluginIdentifier";

    private static final String CONTROLLER_PATH = "page/{" + PLUGIN_IDENTIFIER_VARIABLE + "}/{" + VIEW_NAME_VARIABLE + "}";

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Monitorable(threshold = 500)
    @RequestMapping(value = CONTROLLER_PATH, method = RequestMethod.GET)
    public ModelAndView prepareView(@PathVariable(PLUGIN_IDENTIFIER_VARIABLE) final String pluginIdentifier,
            @PathVariable(VIEW_NAME_VARIABLE) final String viewName, @RequestParam final Map<String, String> arguments,
            final Locale locale) {

        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        ModelAndView modelAndView = new ModelAndView("crud/crudView");

        String context = viewDefinition.translateContextReferences(arguments.get("context"));

        JSONObject jsonContext = new JSONObject();

        if (StringUtils.hasText(context)) {
            try {
                jsonContext = new JSONObject(context);
            } catch (JSONException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        modelAndView.addObject("model", viewDefinition.prepareView(jsonContext, locale));
        modelAndView.addObject("viewName", viewName);
        modelAndView.addObject("pluginIdentifier", pluginIdentifier);
        modelAndView.addObject("context", context);

        boolean popup = false;
        if (arguments.containsKey("popup")) {
            popup = Boolean.parseBoolean(arguments.get("popup"));
        }
        modelAndView.addObject("popup", popup);

        modelAndView.addObject("locale", locale.getLanguage());

        return modelAndView;
    }

    @Monitorable(threshold = 500)
    @RequestMapping(value = { CONTROLLER_PATH }, method = RequestMethod.POST)
    @ResponseBody
    public Object performEvent(@PathVariable(PLUGIN_IDENTIFIER_VARIABLE) final String pluginIdentifier,
            @PathVariable(VIEW_NAME_VARIABLE) final String viewName, @RequestBody final JSONObject body, final Locale locale) {

        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        try {
            return viewDefinition.performEvent(body, locale);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
