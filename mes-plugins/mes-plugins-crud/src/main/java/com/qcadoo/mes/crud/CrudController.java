/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.crud;

import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.ViewDefinition;

@Controller
public final class CrudController {

    private static final String VIEW_NAME_VARIABLE = "viewName";

    private static final String PLUGIN_IDENTIFIER_VARIABLE = "pluginIdentifier";

    private static final String CONTROLLER_PATH = "page/{" + PLUGIN_IDENTIFIER_VARIABLE + "}/{" + VIEW_NAME_VARIABLE + "}";

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @RequestMapping(value = CONTROLLER_PATH, method = RequestMethod.GET)
    public ModelAndView prepareView(@PathVariable(PLUGIN_IDENTIFIER_VARIABLE) final String pluginIdentifier,
            @PathVariable(VIEW_NAME_VARIABLE) final String viewName, @RequestParam final Map<String, String> arguments,
            final Locale locale) {

        ViewDefinition viewDefinition = viewDefinitionService.get(pluginIdentifier, viewName);

        ModelAndView modelAndView = new ModelAndView("crud/crudView");

        modelAndView.addObject("model", viewDefinition.prepareView(locale));
        modelAndView.addObject("viewName", viewName);
        modelAndView.addObject("pluginIdentifier", pluginIdentifier);
        modelAndView.addObject("context", arguments.get("context"));

        boolean popup = false;
        if (arguments.containsKey("popup")) {
            popup = Boolean.parseBoolean(arguments.get("popup"));
        }
        modelAndView.addObject("popup", popup);

        modelAndView.addObject("locale", locale.getLanguage());

        // String lookupComponentName = arguments.get("lookupComponent");
        //
        // if (StringUtils.hasText(lookupComponentName)) {
        // LookupComponent lookupComponent = (LookupComponent) viewDefinition.lookupComponent(arguments.get("lookupComponent"));
        // checkNotNull(lookupComponent, "Cannot find lookup component " + lookupComponentName);
        // ViewDefinition lookupViewDefinition = lookupComponent.getLookupViewDefinition(viewDefinitionService);
        // lookupViewDefinition.updateTranslations(translationsMap, locale);
        // modelAndView.addObject("viewDefinition", lookupViewDefinition);
        // modelAndView.addObject("lookupComponentName", lookupComponentName);
        // } else {
        // modelAndView.addObject("viewDefinition", viewDefinition);
        // }
        // modelAndView.addObject("arguments", arguments);

        return modelAndView;
    }

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
