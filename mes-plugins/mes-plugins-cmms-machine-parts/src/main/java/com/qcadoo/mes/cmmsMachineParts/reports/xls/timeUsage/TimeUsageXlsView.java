package com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractExcelView;

@Component
public class TimeUsageXlsView  extends AbstractExcelView {

    @Autowired
    private TimeUsageXlsService timeUsageXlsService;

    @Override protected void buildExcelDocument(Map<String, Object> filters, HSSFWorkbook hssfWorkbook,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        HSSFSheet sheet = hssfWorkbook.createSheet(timeUsageXlsService.getReportTitle(LocaleContextHolder.getLocale()));
        timeUsageXlsService.buildExcelContent(hssfWorkbook, sheet, filters, LocaleContextHolder.getLocale());
    }
}
