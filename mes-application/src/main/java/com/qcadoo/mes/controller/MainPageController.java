package com.qcadoo.mes.controller;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;

@Controller
public final class MainPageController {

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @RequestMapping(value = "mainPage", method = RequestMethod.GET)
    public ModelAndView getView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("mainPage");
        mav.addObject("viewsList", viewDefinitionService.list());
        mav.addObject("commonTranslations", translationService.getCommonsMessages(locale));
        return mav;
    }

    @RequestMapping(value = "main", method = RequestMethod.GET)
    public ModelAndView getMainView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("main");
        mav.addObject("viewsList", viewDefinitionService.list());
        mav.addObject("commonTranslations", translationService.getCommonsMessages(locale));
        mav.addObject("menuStructure", viewDefinitionService.getMenu(locale).getAsJson());
        return mav;
    }

    @RequestMapping(value = "homePage", method = RequestMethod.GET)
    public ModelAndView getHomePageView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("userLogin", securityService.getCurrentUser().getUserName());

        mav.addObject("translationsMap", translationService.getDashboardMessages(locale));

        mav.setViewName("dashboard");
        return mav;
    }
}
