package com.qcadoo.mes.orders.controllers;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.view.api.crud.CrudService;

@Controller
public class OrderTechnologicalProcessesAnalysisViewController {

    @Autowired
    private CrudService crudService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping("/orderTechnologicalProcessesAnalysis")
    public ModelAndView getAnalysisView(final Locale locale) {
        ModelAndView mav = crudService.prepareView(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.VIEW_ORDER_TECHNOLOGICAL_PROCESSES_ANALYSIS, Collections.emptyMap(), locale);

        Map<String, String> slickGridTranslations = translationService.getMessagesGroup("slickGrid", locale);

        slickGridTranslations.putAll(translationService.getMessagesGroup("commons", locale));
        slickGridTranslations.putAll(translationService.getMessagesGroup("orderTechnologicalProcessesAnalysis", locale));

        mav.addObject("slickGridTranslations", slickGridTranslations);

        return mav;
    }

}
