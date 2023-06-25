package com.qcadoo.mes.productionCounting.controller;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.view.api.crud.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

@Controller
public class EmployeePieceworkSettlementViewController {

    @Autowired
    private CrudService crudService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping("/employeePieceworkSettlement")
    public ModelAndView getAnalysisView(final Locale locale) {
        ModelAndView mav = crudService.prepareView(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.VIEW_EMPLOYEE_PIECEWORK_SETTLEMENT, Collections.emptyMap(), locale);

        Map<String, String> slickGridTranslations = translationService.getMessagesGroup("slickGrid", locale);

        slickGridTranslations.putAll(translationService.getMessagesGroup("commons", locale));
        slickGridTranslations.putAll(translationService.getMessagesGroup("employeePieceworkSettlement", locale));

        mav.addObject("slickGridTranslations", slickGridTranslations);

        return mav;
    }

}
