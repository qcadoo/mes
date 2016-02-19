package com.qcadoo.mes.cmmsMachineParts.reports;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.TimeUsageReportFilterFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Controller public class TimeUsageReportController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @RequestMapping(value = "/cmmsMachineParts/timeUsageReport.xls", method = RequestMethod.GET)
    public ModelAndView generatePlannedEventsReport(@RequestParam("filterId") final Long filterId) {
        DataDefinition dataDefinition = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_TIME_USAGE_REPORT_FILTER);
        Entity filter = dataDefinition.get(filterId);
        HashMap<String, Object> filtersMap = new HashMap<String, Object>();
        Date fromDate = filter.getDateField(TimeUsageReportFilterFields.FROM_DATE);
        if (fromDate != null) {
            filtersMap.put(TimeUsageReportFilterFields.FROM_DATE, fromDate);
        }
        Date toDate = filter.getDateField(TimeUsageReportFilterFields.TO_DATE);
        if (toDate != null) {
            filtersMap.put(TimeUsageReportFilterFields.TO_DATE, toDate);
        }
        List<Entity> workers = filter.getManyToManyField(TimeUsageReportFilterFields.WORKERS);
        if (!workers.isEmpty()) {
            filtersMap
                    .put(TimeUsageReportFilterFields.WORKERS, workers.stream().map(w -> w.getId()).collect(Collectors.toList()));
        }
        return new ModelAndView("timeUsageXlsView", "filtersMap", filtersMap);

    }

}
