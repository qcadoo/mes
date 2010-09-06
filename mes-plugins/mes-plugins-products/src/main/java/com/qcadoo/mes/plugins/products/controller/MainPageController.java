package com.qcadoo.mes.plugins.products.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.data.api.ViewDefinitionService;

@Controller
public class MainPageController {

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @RequestMapping(value = "main", method = RequestMethod.GET)
    public ModelAndView getView(final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("mainPage");

        mav.addObject("headerLabel", "QCADOOmes");

        mav.addObject("viewsList", viewDefinitionService.getAllViews());

        return mav;
    }
}
