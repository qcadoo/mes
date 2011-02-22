package com.qcadoo.mes.qualityControls.print;

import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.qcadoo.mes.utils.xls.ReportXlsView;

public class QualityControlForOperationXlsView extends ReportXlsView {

    @Override
    protected String addContent(final Map<String, Object> model, final HSSFWorkbook workbook, final Locale locale) {
        HSSFSheet sheet = workbook.createSheet(getTranslationService().translate(
                "qualityControls.qualityControlForOperation.report.title", locale));
        sheet.setZoom(4, 3);
        return getTranslationService().translate("qualityControls.qualityControlForOperation.report.fileName", locale);
    }

}
