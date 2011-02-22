package com.qcadoo.mes.qualityControl.print;

import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.qcadoo.mes.utils.xls.ReportXlsView;

public class QualityControlForBatchXlsView extends ReportXlsView {

    @Override
    protected String addContent(final Map<String, Object> model, final HSSFWorkbook workbook, final Locale locale) {
        HSSFSheet sheet = workbook.createSheet(getTranslationService().translate(
                "qualityControls.qualityControlForBatch.report.title", locale));
        sheet.setZoom(4, 3);
        return getTranslationService().translate("qualityControls.qualityControlForBatch.report.fileName", locale);
    }

}
