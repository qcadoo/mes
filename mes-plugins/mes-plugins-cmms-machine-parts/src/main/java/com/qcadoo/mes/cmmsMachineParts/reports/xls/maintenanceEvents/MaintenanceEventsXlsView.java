package com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents;

import com.qcadoo.report.api.xls.abstractview.AbstractXLSXView;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class MaintenanceEventsXlsView extends AbstractXLSXView {

    @Autowired
    private MaintenanceEventsXlsService maintenanceEventsXlsService;

    @Override
    protected void buildExcelDocument(Map<String, Object> filters, XSSFWorkbook xssfWorkbook, HttpServletRequest request,
            HttpServletResponse response){
        maintenanceEventsXlsService.buildExcelDocument(xssfWorkbook, filters, LocaleContextHolder.getLocale());
    }

}
