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
public class LinesProducedQuantitiesChartViewController {

    @Autowired
    private CrudService crudService;

    @Autowired
    private TranslationService translationService;

    @RequestMapping("/linesProducedQuantitiesChart")
    public ModelAndView getLinesProducedQuantitiesChartView(final Locale locale) {
        ModelAndView mav = crudService.prepareView(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.VIEW_LINES_PRODUCED_QUANTITIES_CHART, Collections.emptyMap(), locale);

        Map<String, String> chartTranslations = translationService.getMessagesGroup("linesProducedQuantitiesChart", locale);

        chartTranslations.putAll(translationService.getMessagesGroup("commons", locale));
        chartTranslations.putAll(translationService.getMessagesGroup("productionCounting", locale));

        mav.addObject("chartTranslations", chartTranslations);

        return mav;
    }

}
