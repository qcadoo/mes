package com.qcadoo.mes.productionCounting.controller;

import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qcadoo.mes.productionCounting.controller.dataProvider.LinesProducedQuantitiesChartDataProvider;

@Controller
@RequestMapping("/lProducedQuantitiesChart")
public class LinesProducedQuantitiesChartController {

    @Autowired
    private LinesProducedQuantitiesChartDataProvider linesProducedQuantitiesChartDataProvider;

    @ResponseBody
    @RequestMapping(value = "/validate", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String validate(@RequestParam String dateFrom, @RequestParam String dateTo) throws ParseException {
        return linesProducedQuantitiesChartDataProvider.validate(dateFrom, dateTo);
    }

    @ResponseBody
    @RequestMapping(value = "/data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getData(@RequestParam String dateFrom, @RequestParam String dateTo, final Locale locale) throws ParseException {
        return linesProducedQuantitiesChartDataProvider.getData(dateFrom, dateTo, locale);
    }

}
