package com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost;

import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;

@Component
public class WorkerCostsXlsView  extends AbstractExcelView {

    @Autowired
    private WorkerCostsXlsService workerCostsXlsService;

    @Autowired
    private TranslationService translationService;

    private final String localePrefix = "cmmsMachineParts.workerCostsReport.report.filename";

    @Override protected void buildExcelDocument(Map<String, Object> filters, HSSFWorkbook hssfWorkbook,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        String fileName = generateFilename();
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xls\"");
        HSSFSheet sheet = hssfWorkbook.createSheet(workerCostsXlsService.getReportTitle(LocaleContextHolder.getLocale()));
        workerCostsXlsService.buildExcelContent(hssfWorkbook, sheet, filters, LocaleContextHolder.getLocale());
    }

    private String generateFilename() {
        String translatedFileName = translationService.translate(localePrefix, getLocale());
        String date = new SimpleDateFormat(DateUtils.L_REPORT_DATE_TIME_FORMAT, getLocale()).format(new Date());
        return translatedFileName + "_" + date;
    }
}
