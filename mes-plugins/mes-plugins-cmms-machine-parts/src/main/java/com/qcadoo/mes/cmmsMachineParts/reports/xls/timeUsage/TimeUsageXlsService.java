package com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage.dto.TimeUsageDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage.dto.TimeUsageGroupDTO;
import com.qcadoo.model.api.NumberService;

@Service public class TimeUsageXlsService {

    @Autowired private TranslationService translationService;

    @Autowired
    private TimeUsageXlsDataProvider timeUsageXLSDataProvider;

    @Autowired private NumberService numberService;

    private static final int REALIZATIONS_COLUMN_POSITION_START = 25;

    private static final int PARTS_COLUMN_POSITION_START = 21;

    public String getReportTitle(final Locale locale) {
        return translationService.translate(TimeUsageXlsConstants.REPORT_TITLE, locale);
    }

    public void buildExcelContent(final HSSFWorkbook workbook, final HSSFSheet sheet, Map<String, Object> filters,
            final Locale locale) {
        List<TimeUsageDTO> usages = timeUsageXLSDataProvider.getUsages((Map<String, Object>) filters.get("filtersMap"));
        fillHeaderRow(workbook, sheet, 0, locale);
        List<TimeUsageGroupDTO> timeUsageGroups = group(usages);
        int rowCounter = 1;
        for (TimeUsageGroupDTO timeUsageGroupDTO : timeUsageGroups) {
            rowCounter = fillTimeUsageRows(workbook, sheet, timeUsageGroupDTO, rowCounter++, locale);
        }
    }

    private List<TimeUsageGroupDTO> group(List<TimeUsageDTO> usages) {
        List<TimeUsageGroupDTO> groups = Lists.newLinkedList();
        Map<Date, List<TimeUsageDTO>> dateMap = usages.stream().collect(Collectors.groupingBy(TimeUsageDTO::getStartDate));
        for (Date date : dateMap.keySet()) {
            List<TimeUsageDTO> entry = dateMap.get(date);
            Map<String, List<TimeUsageDTO>> workerMap = entry.stream().collect(Collectors.groupingBy(TimeUsageDTO::getWorker));
            for (String worker : workerMap.keySet()) {
                TimeUsageGroupDTO timeUsageGroup = new TimeUsageGroupDTO(date, worker, workerMap.get(worker));
                groups.add(timeUsageGroup);
            }
        }
        return groups.stream().sorted((g1, g2) -> {
            if (g1.getDate().equals(g2.getDate())) {
                return g1.getWorker().compareTo(g2.getWorker());
            } else {
                return g2.getDate().compareTo(g1.getDate());
            }
        }).collect(Collectors.toList());
    }

    private void fillHeaderRow(final HSSFWorkbook workbook, final HSSFSheet sheet, Integer rowNum, final Locale locale) {
        HSSFRow headerLine = sheet.createRow(rowNum);
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        HSSFCellStyle style = workbook.createCellStyle();

            style.setFont(font);

        int colNumber = 0;
        for (String column : TimeUsageXlsConstants.ALL_COLUMNS) {
            HSSFCell headerCell = headerLine.createCell(colNumber);
            headerCell.setCellValue(translationService.translate(column, locale));
            headerCell.setCellStyle(style);
            colNumber++;
        }
    }

    private int fillTimeUsageRows(final HSSFWorkbook workbook, final HSSFSheet sheet, final TimeUsageGroupDTO timeUsage,
            int rowCounter, final Locale locale) {
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);

        int usagesCounter = 0;


        for (TimeUsageDTO usage : timeUsage.getTimeUsages()) {
            HSSFRow usageRow = sheet.createRow(rowCounter + usagesCounter);
            addNewRow(usageRow, usage, locale);
            if (usagesCounter == 0) {
                addNewCell(usageRow, timeUsage.getDurationSum().toString(), 10);
                addNewCell(usageRow, timeUsage.getRegisteredTimeSum().toString(), 11);
            } else {
                addNewCell(usageRow, "", 10);
                addNewCell(usageRow, "", 11);
            }
            ++usagesCounter;
        }

        return rowCounter + usagesCounter;
    }

    private void addNewRow(HSSFRow usageRow, TimeUsageDTO timeUsage, Locale locale) {
        addNewCell(usageRow, timeUsage.getWorker(), 0);
        addNewCell(usageRow, getDateValue(timeUsage.getStartDate()), 1);
        addNewCell(usageRow, timeUsage.getNumber(), 2);
        addNewCell(usageRow, translationService.translate(timeUsage.getType(), locale), 3);
        addNewCell(usageRow, translationService.translate(timeUsage.getState(), locale), 4);
        addNewCell(usageRow, timeUsage.getObject(), 5);
        addNewCell(usageRow, timeUsage.getParts(), 6);
        addNewCell(usageRow, timeUsage.getDescription(), 7);
        addNewCell(usageRow, timeUsage.getDuration().toString(), 8);
        addNewCell(usageRow, timeUsage.getRegisteredTime().toString(), 9);
    }

    private void addNewCell(HSSFRow row, String value, int column) {
        HSSFCell cell = row.createCell(column);
        cell.setCellValue(value);
    }

    private String getIntValue(Integer value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    private String getTime(Integer value) {
        if (value == null) {
            return "";
        }
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HHHH:mm:ss");
        df.setTimeZone(tz);
        String time = df.format(new Date(value*1000L*100L));

        return time;
    }

    private String getDecimalValue(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return numberService.formatWithMinimumFractionDigits(value,0);
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

    private String getValue(Object value) {
        if (value == null) {
            return translationService.translate("qcadooView.false", LocaleContextHolder.getLocale());
        }
        if ((Boolean) value) {
            return translationService.translate("qcadooView.true", LocaleContextHolder.getLocale());
        } else {
            return translationService.translate("qcadooView.false", LocaleContextHolder.getLocale());
        }

    }
}
