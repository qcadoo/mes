package com.qcadoo.mes.basic;

import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.crud.CrudController;

@Controller
public class BasicController {

    @Autowired
    private CrudController crudController;

    @Autowired
    private ParameterService parameterService;

    @RequestMapping(value = "parameter", method = RequestMethod.GET)
    public ModelAndView getParameterPageView(final Locale locale) {
        JSONObject json = new JSONObject(ImmutableMap.of("window.parameter.id", parameterService.getParameterId().toString()));
        Map<String, String> arguments = ImmutableMap.of("context", json.toString());
        return crudController.prepareView("basic", "parameter", arguments, locale);
    }

}
