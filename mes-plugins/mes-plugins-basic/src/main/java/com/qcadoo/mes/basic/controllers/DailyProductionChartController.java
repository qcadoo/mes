package com.qcadoo.mes.basic.controllers;

import com.qcadoo.mes.basic.controllers.dataProvider.DailyProductionChartDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/dailyProductionChart")
public class DailyProductionChartController {

    @Autowired
    private DailyProductionChartDataProvider dailyProductionChartDataProvider;

    @ResponseBody
    @RequestMapping(value = "/data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Long> getData() {
        return dailyProductionChartDataProvider.getData();
    }

}
