package com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents;

import com.qcadoo.report.api.xls.abstractview.AbstractXLSXView;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class PlannedEventsXlsView  extends AbstractXLSXView {

    @Autowired
    private PlannedEventsXlsService plannedEventsXlsService;

    @Override protected void buildExcelDocument(Map<String, Object> filters, XSSFWorkbook xssfWorkbook,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        XSSFSheet sheet = xssfWorkbook.createSheet( plannedEventsXlsService.getReportTitle(LocaleContextHolder.getLocale()));
        plannedEventsXlsService.buildExcelContent(xssfWorkbook, sheet,filters, LocaleContextHolder.getLocale());
    }
}
