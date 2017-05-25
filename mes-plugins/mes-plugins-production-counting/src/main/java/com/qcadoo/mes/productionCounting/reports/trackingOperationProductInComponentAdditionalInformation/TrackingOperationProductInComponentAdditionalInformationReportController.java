package com.qcadoo.mes.productionCounting.reports.trackingOperationProductInComponentAdditionalInformation;

import static com.qcadoo.localization.api.utils.DateUtils.L_DATE_TIME_FORMAT;
import static com.qcadoo.mes.productionCounting.reports.trackingOperationProductInComponentAdditionalInformation.TrackingOperationProductInComponentAdditionalInformationReportModelConstants.ADDITIONAL_INFORMATION_REPORT_DATA;
import static com.qcadoo.mes.productionCounting.reports.trackingOperationProductInComponentAdditionalInformation.TrackingOperationProductInComponentAdditionalInformationReportModelConstants.FROM_DATE;
import static com.qcadoo.mes.productionCounting.reports.trackingOperationProductInComponentAdditionalInformation.TrackingOperationProductInComponentAdditionalInformationReportModelConstants.GENERATED_BY;
import static com.qcadoo.mes.productionCounting.reports.trackingOperationProductInComponentAdditionalInformation.TrackingOperationProductInComponentAdditionalInformationReportModelConstants.TO_DATE;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.security.api.SecurityService;

@Controller
final class TrackingOperationProductInComponentAdditionalInformationReportController {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TrackingOperationProductInComponentAdditionalInformationXlsService trackingOperationProductInComponentAdditionalInformationXlsService;

    @RequestMapping(value = "/productionCounting/trackingOperationProductInComponentAdditionalInformationReport.xlsx", method = RequestMethod.GET)
    public ModelAndView generatePlannedEventsReport(@RequestParam("fromDate") String fromDateString, @RequestParam("toDate") String toDateString,
            ModelMap modelMap) {
        Date fromDate = parseDate(fromDateString);
        Date toDate = parseDate(toDateString);
        modelMap.addAttribute(FROM_DATE, fromDate);
        modelMap.addAttribute(TO_DATE, toDate);
        modelMap.addAttribute(GENERATED_BY, generatedBy());
        modelMap.addAttribute(ADDITIONAL_INFORMATION_REPORT_DATA,
                trackingOperationProductInComponentAdditionalInformationXlsService.getAdditionalInformationReportData(fromDate,
                        toDate));
        return new ModelAndView("trackingOperationProductInComponentAdditionalInformationXlsView", modelMap);
    }

    private String generatedBy() {
        SimpleDateFormat sdf = new SimpleDateFormat(L_DATE_TIME_FORMAT);
        return securityService.getCurrentUserName() + " " + sdf.format(new Date());
    }

    private Date parseDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.L_DATE_FORMAT);
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
