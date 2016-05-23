package com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.cmmsMachineParts.constants.SourceCostReportFilterFields;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.factory.EventFieldsForTypeFactory;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost.dto.WorkerCostsDTO;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;

@Service public class WorkerCostsXlsService {

    @Autowired private TranslationService translationService;

    @Autowired
    private WorkerCostsXlsDataProvider workerCostsXLSDataProvider;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private EventFieldsForTypeFactory fieldsForTypeFactory;

    private ReportStyleFactory reportStyleFactory;

    public String getReportTitle(final Locale locale) {
        return translationService.translate(WorkerCostsXlsConstants.REPORT_TITLE, locale);
    }

    public void buildExcelContent(final HSSFWorkbook workbook, final HSSFSheet sheet, Map<String, Object> filters,
            final Locale locale) {
        reportStyleFactory = new ReportStyleFactory(workbook);
        List<WorkerCostsDTO> usages = workerCostsXLSDataProvider.getCosts((Map<String, Object>) filters.get("filtersMap"));
        fillSums(usages);
        fillHeaderData(workbook, sheet, 0, locale, (Map<String, Object>) filters.get("filtersMap"));
        fillHeaderRow(workbook, sheet, 4, locale);
        int rowCounter = 5;
        fillUsages(workbook, sheet, usages, rowCounter, locale);
        setColumnsWidths(sheet);
    }

    private void fillSums(List<WorkerCostsDTO> workerCosts) {
        Map<String, List<WorkerCostsDTO>> groups = workerCosts.stream().collect(
                Collectors.groupingBy(WorkerCostsDTO::getSourceCost));
        for (Entry<String, List<WorkerCostsDTO>> group : groups.entrySet()) {
            sumGroup(group.getValue());
            Integer sum = group.getValue().stream().filter(g -> g.getWorkerTimeSum() != null)
                    .mapToInt(WorkerCostsDTO::getWorkerTimeSum).sum();
            group.getValue().get(0).setCostSourceTimeSum(sum);
        }
    }


    private void sumGroup(List<WorkerCostsDTO> group) {
        Map<String, List<WorkerCostsDTO>> groups = group.stream().collect(Collectors.groupingBy(WorkerCostsDTO::getWorker));
        for (Entry<String, List<WorkerCostsDTO>> subGroup : groups.entrySet()) {
            Integer sum = subGroup.getValue().stream().mapToInt(WorkerCostsDTO::getWorkTime).sum();
            subGroup.getValue().get(0).setWorkerTimeSum(sum);
        }
    }

    private void setColumnsWidths(HSSFSheet sheet) {
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 3500);
        sheet.setColumnWidth(3, 3500);
        sheet.setColumnWidth(4, 5000);
        sheet.setColumnWidth(5, 6000);
        sheet.setColumnWidth(6, 6000);
    }

    private void fillHeaderData(final HSSFWorkbook workbook, final HSSFSheet sheet, Integer rowNum, final Locale locale,
            Map<String, Object> filters) {
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);

        HSSFRow titleRow = sheet.createRow(0);
        HSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(translationService.translate("cmmsMachineParts.workerCostsReport.report.title", locale));
        titleCell.setCellStyle(style);

        HSSFRow datesRow = sheet.createRow(1);
        HSSFCell startingFromLabelCell = datesRow.createCell(0);
        startingFromLabelCell.setCellValue(translationService.translate("cmmsMachineParts.workerCostsReport.report.startingFrom",
                locale));
        startingFromLabelCell.setCellStyle(style);
        if (filters.containsKey(SourceCostReportFilterFields.FROM_DATE)) {
            HSSFCell startingFromCell = datesRow.createCell(1);
            startingFromCell.setCellValue(getDateOnly((Date) filters.get(SourceCostReportFilterFields.FROM_DATE)));
        }
        HSSFCell toLabelCell = datesRow.createCell(2);
        toLabelCell.setCellValue(translationService.translate("cmmsMachineParts.workerCostsReport.report.to", locale));
        toLabelCell.setCellStyle(style);
        if (filters.containsKey(SourceCostReportFilterFields.TO_DATE)) {
            HSSFCell toCell = datesRow.createCell(3);
            toCell.setCellValue(getDateOnly((Date) filters.get(SourceCostReportFilterFields.TO_DATE)));
        }

        HSSFRow authorRow = sheet.createRow(2);
        HSSFCell authorLabelCell = authorRow.createCell(0);
        authorLabelCell.setCellValue(translationService.translate("cmmsMachineParts.workerCostsReport.report.generatedBy", locale));
        authorLabelCell.setCellStyle(style);
        HSSFCell authorCell = authorRow.createCell(1);
        authorCell.setCellValue(getUserString());
    }

    private String getUserString() {
        Entity user = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                .get(securityService.getCurrentUserId());
        StringBuilder builder = new StringBuilder();
        builder.append(user.getStringField("firstName"));
        builder.append(" ");
        builder.append(user.getStringField("lastName"));
        builder.append(" ");
        builder.append(getDateValue(new Date()));
        return builder.toString();
    }

    private void fillHeaderRow(final HSSFWorkbook workbook, final HSSFSheet sheet, Integer rowNum, final Locale locale) {
        HSSFRow headerLine = sheet.createRow(rowNum);
        headerLine.setHeight((short) 800);
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setWrapText(true);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

        int colNumber = 0;
        for (String column : WorkerCostsXlsConstants.ALL_COLUMNS) {
            HSSFCell headerCell = headerLine.createCell(colNumber);
            headerCell.setCellValue(translationService.translate(column, locale));
            headerCell.setCellStyle(style);
            colNumber++;
        }
    }

    private void fillUsages(final HSSFWorkbook workbook, final HSSFSheet sheet,
 final List<WorkerCostsDTO> group,
            int rowCounter, final Locale locale) {
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);

        int usagesCounter = 0;

        for (WorkerCostsDTO workerCost : group) {
                HSSFRow usageRow = sheet.createRow(rowCounter + usagesCounter);
            HSSFCellStyle style = getLeftAlignedStyle(workbook, workerCost.getWorkerTimeSum() != null);
            HSSFCellStyle styleRight = getRightAlignedStyle(workbook, workerCost.getWorkerTimeSum() != null);
                addNewRow(usageRow, workerCost, locale, style, styleRight);
            if (workerCost.getWorkerTimeSum() != null) {
                addNewCell(usageRow, workerCost.getWorkerTimeSum(), 5, styleRight);
                if (workerCost.getCostSourceTimeSum() != null) {
                    addNewCell(usageRow, workerCost.getCostSourceTimeSum(), 6, styleRight);
                } else {
                    addNewCell(usageRow, "", 6, styleRight);
                }
            }
                ++usagesCounter;
            }

    }

    private void addNewRow(HSSFRow usageRow, WorkerCostsDTO timeUsage, Locale locale, HSSFCellStyle style,
            HSSFCellStyle styleAlignRight) {
        addNewCell(usageRow, timeUsage.getSourceCost(), 0, style);
        addNewCell(usageRow, timeUsage.getWorker(), 1, style);
        addNewCell(usageRow, timeUsage.getEvent(), 2, style);
        addNewCell(usageRow, translationService.translate(timeUsage.getType(), locale), 3, style);
        addNewCell(usageRow, timeUsage.getWorkTime(), 4, styleAlignRight);
    }

    private void addNewCell(HSSFRow row, String value, int column, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void addNewCell(HSSFRow row, Integer value, int column, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(column);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue(value / 86400.0D);
        cell.setCellStyle(style);
    }

    private HSSFCellStyle getStyle(final HSSFWorkbook workbook, boolean isFirst, boolean isLeft) {
        reportStyleFactory.setFirst(isFirst);
        if (isLeft) {
            reportStyleFactory.setLeftAligned();
        } else {
            reportStyleFactory.setRightAligned();
        }
        return reportStyleFactory.getStyle();
    }

    private HSSFCellStyle getRightAlignedStyle(final HSSFWorkbook workbook, boolean isFirst) {
        return getStyle(workbook, isFirst, false);
    }

    private HSSFCellStyle getLeftAlignedStyle(final HSSFWorkbook workbook, boolean isFirst) {
        return getStyle(workbook, isFirst, true);
    }

    private String getDateValue(Date date) {
        if (date == null) {
            return "";
        }
        return DateUtils.toDateTimeString(date);
    }

    private String getDateOnly(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat(DateUtils.L_DATE_FORMAT);
        String time = df.format(date);

        return time;
    }
}
