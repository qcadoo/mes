package com.qcadoo.mes.plugins.crud.controller;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.ViewDefinitionService;
import com.qcadoo.mes.plugins.crud.translation.TranslationServiceImpl;

@Controller
public class MainPageController {

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private TranslationServiceImpl translationService;

    @RequestMapping(value = "main", method = RequestMethod.GET)
    public ModelAndView getView(@RequestParam final Map<String, String> arguments, final Locale locale) {

        for (Entry<String, String> entry : arguments.entrySet()) {
            System.out.println(entry.getKey() + "-" + entry.getValue());
        }

        ModelAndView mav = new ModelAndView();
        mav.setViewName("mainPage");

        mav.addObject("viewsList", viewDefinitionService.getAllViews());

        mav.addObject("commonTranslations", translationService.getCommonsTranslations(locale));

        return mav;
    }
}
