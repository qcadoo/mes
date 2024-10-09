package com.qcadoo.mes.masterOrders.controllers;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.view.api.crud.CrudService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;
import java.util.Map;

@Controller
public class MasterOrdersControllerMO {

    @Autowired
    private CrudService crudService;

    @Autowired
    private ParameterService parameterService;

    @RequestMapping(value = "salesParameters", method = RequestMethod.GET)
    public ModelAndView getSalesParametersPageView(final Locale locale) {
        JSONObject json = new JSONObject(ImmutableMap.of("form.id", parameterService.getParameterId().toString()));

        Map<String, String> arguments = ImmutableMap.of("context", json.toString());

        return crudService.prepareView(MasterOrdersConstants.PLUGIN_IDENTIFIER, "salesParameters", arguments, locale);
    }
}
