package com.qcadoo.mes.productionCounting.controller;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.view.api.crud.CrudService;

@Controller
public class ProductionBalanceAnalysisViewController {

    @Autowired
    private CrudService crudService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping("/productionBalanceAnalysis")
    public ModelAndView getProductionBalanceAnalysisView(final Locale locale) {
        ModelAndView mav = crudService.prepareView(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.VIEW_PRODUCTION_BALANCE_ANALYSIS, Collections.emptyMap(), locale);

        Map<String, String> slickGridTranslations = translationService.getMessagesGroup("slickGrid", locale);
        slickGridTranslations.putAll(translationService.getMessagesGroup("productionCounting", locale));

        slickGridTranslations.putAll(translationService.getMessagesGroup("commons", locale));
        slickGridTranslations.putAll(translationService.getMessagesGroup("productionBalanceAnalysis", locale));

        mav.addObject("slickGridTranslations", slickGridTranslations);

        return mav;
    }

}
