package com.qcadoo.mes.controller;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.api.ViewDefinitionService;
import com.qcadoo.mes.core.internal.TranslationServiceImpl;

@Controller
public final class MainPageController {

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private TranslationServiceImpl translationService;

    @RequestMapping(value = "main", method = RequestMethod.GET)
    public ModelAndView getView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("mainPage");
        mav.addObject("viewsList", viewDefinitionService.getAllViews());
        mav.addObject("commonTranslations", translationService.getCommonsTranslations(locale));
        return mav;
    }
}
