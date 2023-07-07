package com.qcadoo.mes.productionCounting.controller;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qcadoo.mes.basic.controllers.dataProvider.dto.ColumnDTO;
import com.qcadoo.mes.basic.services.ExportToCsvServiceB;
import com.qcadoo.mes.productionCounting.controller.dataProvider.EmployeeWorkingTimeSettlementDataProvider;

@Controller
@RequestMapping("/emplWorkingTimeSettlement")
public class EmployeeWorkingTimeSettlementController {

    @Autowired
    private EmployeeWorkingTimeSettlementDataProvider employeeWorkingTimeSettlementDataProvider;

    @Autowired
    private ExportToCsvServiceB exportToCsvServiceB;

    @ResponseBody
    @RequestMapping(value = "/columns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ColumnDTO> getColumns(final Locale locale) {
        return employeeWorkingTimeSettlementDataProvider.getColumns(locale);
    }

    @ResponseBody
    @RequestMapping(value = "/validate", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String validate(@RequestParam String dateFrom, @RequestParam String dateTo) throws ParseException {
        return employeeWorkingTimeSettlementDataProvider.validate(dateFrom, dateTo);
    }

    @ResponseBody
    @RequestMapping(value = "/records", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getRecords(@RequestParam String dateFrom, @RequestParam String dateTo) {
        try {
            return employeeWorkingTimeSettlementDataProvider.getRecords(dateFrom, dateTo, new JSONObject(), "", false);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/exportToCsv", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object exportToCsv(@RequestBody final JSONObject data, final Locale locale) {
        return exportToCsvServiceB.prepareJsonForCsv(employeeWorkingTimeSettlementDataProvider, "employeeWorkingTimeSettlement", data, locale);
    }

}
