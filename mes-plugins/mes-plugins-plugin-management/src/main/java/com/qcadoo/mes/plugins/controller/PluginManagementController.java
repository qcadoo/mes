package com.qcadoo.mes.plugins.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.crud.CrudController;

@Controller
public class PluginManagementController {

    @Autowired
    private CrudController crudController;

    @RequestMapping(value = "pluginPages/downloadPage", method = RequestMethod.GET)
    public ModelAndView getUpdatePageView(final Locale locale) {

        Map<String, String> crudArgs = new HashMap<String, String>();
        crudArgs.put("popup", "true");

        ModelAndView mav = crudController.prepareView("plugins", "pluginDownload", crudArgs, locale);

        return mav;
    }
}
