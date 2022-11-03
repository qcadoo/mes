package com.qcadoo.mes.technologies.controller;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.controllers.dataProvider.responses.WorkstationsResponse;
import com.qcadoo.mes.basic.services.DashboardView;
import com.qcadoo.mes.technologies.controller.dataProvider.TechnologyConfigurationDto;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;
import java.util.Map;

@Controller
public final class TechnologyConfiguratorController {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ParameterService parameterService;


    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DashboardView dashboardView;

    @RequestMapping(value = "/technologyConfigurator", method = RequestMethod.GET)
    public ModelAndView getDashboardView(@RequestParam final Map<String, String> arguments, final Locale locale) {

        ModelAndView mav = new ModelAndView();
        mav.setViewName("technologies/technologyConfigurator");
        mav.addObject("translationsMap", translationService.getMessagesGroup("technologyConfigurator", locale));
        mav.addObject("locale", locale.getLanguage());
        return mav;

    }

    @ResponseBody
    @RequestMapping(value = "/technologyConfigurator/configuration", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public TechnologyConfigurationDto getConfiguration() {

        TechnologyConfigurationDto configuration = new TechnologyConfigurationDto();
        configuration.setTypeOfProductionRecording(parameterService.getParameter().getStringField("typeOfProductionRecording"));
        return configuration;
    }



}
