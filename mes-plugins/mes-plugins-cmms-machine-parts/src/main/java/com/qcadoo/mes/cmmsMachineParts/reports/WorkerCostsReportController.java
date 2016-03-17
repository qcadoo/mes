package com.qcadoo.mes.cmmsMachineParts.reports;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.cmmsMachineParts.constants.SourceCostReportFilterFields;
import com.qcadoo.model.api.DataDefinitionService;

@Controller
public class WorkerCostsReportController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final SimpleDateFormat df = new SimpleDateFormat(DateUtils.L_DATE_FORMAT);

    @RequestMapping(value = "/cmmsMachineParts/workerCosts.xls", method = RequestMethod.GET)
    public ModelAndView generatePlannedEventsReport(@RequestParam("sourceCost") final Long sourceCost, @RequestParam("dateFrom") String dateFromString, @RequestParam("dateTo") String dateToString) {
        try {
            HashMap<String, Object> filtersMap = new HashMap<>();

            Date dateFrom = df.parse(dateFromString);
            Date dateTo = df.parse(dateToString);

            filtersMap.put(SourceCostReportFilterFields.FROM_DATE, dateFrom);
            filtersMap.put(SourceCostReportFilterFields.TO_DATE, dateTo);
            filtersMap.put(SourceCostReportFilterFields.SOURCE_COST, sourceCost);

            return new ModelAndView("workerCostsXlsView", "filtersMap", filtersMap);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }

    }

}
