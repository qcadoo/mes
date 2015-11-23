package com.qcadoo.mes.cmmsMachineParts.reports.xls.plannedEvents;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class PlannedEventsXlsView  extends AbstractExcelView {

    @Autowired
    private PlannedEventsXlsService plannedEventsXlsService;

    @Override protected void buildExcelDocument(Map<String, Object> filters, HSSFWorkbook hssfWorkbook,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        filters.size();
        HSSFSheet sheet = hssfWorkbook.createSheet( plannedEventsXlsService.getReportTitle(LocaleContextHolder.getLocale()));
        plannedEventsXlsService.buildExcelContent(sheet,filters, LocaleContextHolder.getLocale());
    }
}
