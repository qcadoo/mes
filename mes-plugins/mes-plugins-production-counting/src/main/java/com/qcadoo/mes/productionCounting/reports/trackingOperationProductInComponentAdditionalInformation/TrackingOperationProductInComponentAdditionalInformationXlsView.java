package com.qcadoo.mes.productionCounting.reports.trackingOperationProductInComponentAdditionalInformation;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.report.api.xls.abstractview.AbstractXLSXView;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.qcadoo.localization.api.utils.DateUtils.L_REPORT_DATE_TIME_FORMAT;
import static com.qcadoo.mes.productionCounting.reports.trackingOperationProductInComponentAdditionalInformation.TrackingOperationProductInComponentAdditionalInformationReportModelConstants.*;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

@Component
final class TrackingOperationProductInComponentAdditionalInformationXlsView extends AbstractXLSXView {

    private static final String REPORT_TITLE = "productionCounting.trackingOperationProductInComponentAdditionalInformationReport.title";

    private static final String FROM_DATE_LABEL = "productionCounting.trackingOperationProductInComponentAdditionalInformationReport.fromDate.label";

    private static final String TO_DATE_LABEL = "productionCounting.trackingOperationProductInComponentAdditionalInformationReport.toDate.label";

    private static final String GENERATED_BY_LABEL = "productionCounting.trackingOperationProductInComponentAdditionalInformationReport.generatedBy.label";

    private static final String FILE_NAME = "productionCounting.trackingOperationProductInComponentAdditionalInformationReport.filename";

    @Autowired
    private TranslationService translationService;

    private static Font createFontArialBold(XSSFWorkbook workbook) {
        return createFontArialWithGivenBoldweight(workbook, true);
    }

    private static Font createFontArialNormal(XSSFWorkbook workbook) {
        return createFontArialWithGivenBoldweight(workbook, false);
    }

    private static Font createFontArialWithGivenBoldweight(XSSFWorkbook workbook, boolean bold) {
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBold(bold);
        return font;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, XSSFWorkbook workbook, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Locale locale = getLocale();
        XSSFSheet sheet = workbook.createSheet(translationService.translate(REPORT_TITLE, locale));
        fillHeaderData(workbook, sheet, locale, model);
        fillHeaderRow(workbook, sheet, locale);
        fillDataRows(sheet, model);
        setColumnsWidths(sheet);
    }

    @Override
    protected void setupResponse(HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename() + EXTENSION + "\"");
    }

    private String filename() {
        SimpleDateFormat sdf = new SimpleDateFormat(L_REPORT_DATE_TIME_FORMAT, getLocale());
        return translationService.translate(FILE_NAME, getLocale()) + sdf.format(new Date());
    }

    private void fillHeaderData(XSSFWorkbook workbook, XSSFSheet sheet, Locale locale, Map<String, Object> model) {
        XSSFCellStyle boldFontStyle = workbook.createCellStyle();
        boldFontStyle.setFont(createFontArialBold(workbook));

        CellStyle cellDateStyle = workbook.createCellStyle();
        cellDateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
        cellDateStyle.setAlignment(HorizontalAlignment.LEFT);

        XSSFRow titleRow = sheet.createRow(0);

        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(translationService.translate(REPORT_TITLE, locale));
        titleCell.setCellStyle(boldFontStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        XSSFRow dataRow = sheet.createRow(1);

        XSSFCell fromDateLabel = dataRow.createCell(0);
        fromDateLabel.setCellValue(translationService.translate(FROM_DATE_LABEL, locale));
        fromDateLabel.setCellStyle(boldFontStyle);

        Date fromDate = (Date) model.get(FROM_DATE);
        XSSFCell fromDateCell = dataRow.createCell(1);
        fromDateCell.setCellValue(fromDate);
        fromDateCell.setCellStyle(cellDateStyle);

        XSSFCell toDateLabel = dataRow.createCell(2);
        toDateLabel.setCellValue(translationService.translate(TO_DATE_LABEL, locale));
        toDateLabel.setCellStyle(boldFontStyle);

        Date toDate = (Date) model.get(TO_DATE);
        XSSFCell toDateCell = dataRow.createCell(3);
        toDateCell.setCellValue(toDate);
        toDateCell.setCellStyle(cellDateStyle);

        XSSFRow generatedByRow = sheet.createRow(2);

        XSSFCell generatedByLabelCell = generatedByRow.createCell(0);
        generatedByLabelCell.setCellValue(translationService.translate(GENERATED_BY_LABEL, locale));
        generatedByLabelCell.setCellStyle(boldFontStyle);

        String generatedBy = (String) model.get(GENERATED_BY);
        XSSFCell generatedByCell = generatedByRow.createCell(1);
        generatedByCell.setCellValue(generatedBy);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 3));
    }

    private void fillHeaderRow(XSSFWorkbook workbook, XSSFSheet sheet, Locale locale) {
        XSSFRow header = sheet.createRow(3);
        Font font = createFontArialNormal(workbook);
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        for (int i = 0; i < TrackingOperationProductInComponentAdditionalInformationReportColumns.ALL_COLUMNS.length; i++) {
            String column = TrackingOperationProductInComponentAdditionalInformationReportColumns.ALL_COLUMNS[i];
            XSSFCell headerCell = header.createCell(i);
            headerCell.setCellValue(translationService.translate(column, locale));
            headerCell.setCellStyle(style);
        }
    }

    private void fillDataRows(final XSSFSheet sheet, Map<String, Object> model) {
        final int rowOffset = 4;

        @SuppressWarnings("unchecked")
        List<TrackingOperationProductInComponentAdditionalInformationReportDto> records = (List<TrackingOperationProductInComponentAdditionalInformationReportDto>) model
                .get(ADDITIONAL_INFORMATION_REPORT_DATA);

        int rowCounter = 0;
        for (TrackingOperationProductInComponentAdditionalInformationReportDto record : records) {
            XSSFRow row = sheet.createRow(rowOffset + rowCounter);
            row.createCell(0).setCellValue(record.getProductionTrackingNumber());
            row.createCell(1).setCellValue(record.getOrderNumber());
            row.createCell(2).setCellValue(record.getProductNumber());
            row.createCell(3).setCellValue(record.getProductName());
            row.createCell(4).setCellValue(record.getAdditionalInformation());
            rowCounter++;
        }
    }

    private static void setColumnsWidths(XSSFSheet sheet) {
        sheet.setColumnWidth(0, 5250);
        sheet.setColumnWidth(1, 6000);
        sheet.setColumnWidth(2, 5000);
        sheet.setColumnWidth(3, 6250);
        sheet.setColumnWidth(4, 10000);
    }
}
