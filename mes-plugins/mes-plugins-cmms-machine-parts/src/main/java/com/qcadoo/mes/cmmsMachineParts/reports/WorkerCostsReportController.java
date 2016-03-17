package com.qcadoo.mes.cmmsMachineParts.reports;

import com.qcadoo.localization.api.utils.DateUtils;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class WorkerCostsReportController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final SimpleDateFormat df = new SimpleDateFormat(DateUtils.L_DATE_FORMAT);

    @RequestMapping(value = "/cmmsMachineParts/workerCosts.xls", method = RequestMethod.GET)
    public ModelAndView generatePlannedEventsReport(@RequestParam("sourceCost") final Long sourceCost, @RequestParam("dateFrom") String dateFromString, @RequestParam("dateTo") String dateToString) {
        try {
            DataDefinition dataDefinition = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                    CmmsMachinePartsConstants.MODEL_TIME_USAGE_REPORT_FILTER);
//        Entity filter = dataDefinition.get(filterId);
            HashMap<String, Object> filtersMap = new HashMap<>();

            Date dateFrom = df.parse(dateFromString);
            Date dateTo = df.parse(dateToString);

            System.out.println("-------------------------------------------");
            System.out.println("sourceCost=" + sourceCost + ", dateFrom=" + dateFrom + ", dateTo=" + dateTo);

            /*
* Date fromDate = filter.getDateField(TimeUsageReportFilterFields.FROM_DATE); if (fromDate != null) {
* filtersMap.put(TimeUsageReportFilterFields.FROM_DATE, fromDate); } Date toDate =
* filter.getDateField(TimeUsageReportFilterFields.TO_DATE); if (toDate != null) {
* filtersMap.put(TimeUsageReportFilterFields.TO_DATE, toDate); } List<Entity> workers =
* filter.getManyToManyField(TimeUsageReportFilterFields.WORKERS); if (!workers.isEmpty()) { filtersMap
* .put(TimeUsageReportFilterFields.WORKERS, workers.stream().map(w -> w.getId()).collect(Collectors.toList())); }
             */
            return new ModelAndView("workerCostsXlsView", "filtersMap", filtersMap);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }

    }

}
