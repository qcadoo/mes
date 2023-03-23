/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionPerShift.report.print;

import com.google.common.collect.Maps;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.ShiftFields;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.PPSReportConstants;
import com.qcadoo.mes.productionPerShift.constants.PPSReportFields;
import com.qcadoo.mes.productionPerShift.report.PPSReportXlsHelper;
import com.qcadoo.mes.productionPerShift.report.columns.ReportColumn;
import com.qcadoo.mes.productionPerShift.report.print.utils.DayShiftHolder;
import com.qcadoo.mes.productionPerShift.report.print.utils.EntityProductionPerShiftsComparator;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.xls.XlsDocumentService;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.util.CellRangeAddress;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PPSReportXlsService extends XlsDocumentService {

    private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", LocaleContextHolder.getLocale());

    private final DateFormat UPDATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", LocaleContextHolder.getLocale());

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PPSReportXlsHelper ppsReportXlsHelper;

    @Autowired
    private PPSReportColumnHelper ppsReportColumnHelper;

    @Autowired
    private ShiftsService shiftsService;

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate(PPSReportConstants.TITLE, locale);
    }

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale, final Entity report) {
        PPSReportXlsStyleContainer styleContainer = new PPSReportXlsStyleContainer(sheet);

        createHeaderForAuthor(sheet, locale, styleContainer);

        HSSFRow headerMainLine = sheet.createRow(3);
        HSSFRow headerProduction = sheet.createRow(4);

        List<ReportColumn> columns = ppsReportColumnHelper.getReportColumns();

        createHeaderLineForProduction(sheet, locale, headerMainLine, headerProduction, styleContainer, columns);
        createHeaderLineForDaysWithShifts(sheet, locale, headerMainLine, headerProduction, report, styleContainer, columns);
    }

    private void createHeaderForAuthor(final HSSFSheet sheet, final Locale locale,
            final PPSReportXlsStyleContainer styleContainer) {
        HSSFRow headerAuthorLine = sheet.createRow(0);

        HSSFCell updateDateCell = headerAuthorLine.createCell(0);
        updateDateCell.setCellValue(translationService.translate(PPSReportConstants.COLUMN_HEADER_UPDATE_DATE, locale));

        HSSFCell authorCell = headerAuthorLine.createCell(2);
        authorCell.setCellValue(translationService.translate(PPSReportConstants.COLUMN_HEADER_AUTHOR, locale));

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 4));

        updateDateCell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_GreyDataStyle));
        authorCell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_GreyDataStyle));
    }

    private void createHeaderLineForProduction(final HSSFSheet sheet, final Locale locale, final HSSFRow headerMainLine,
            final HSSFRow headerProduction, final PPSReportXlsStyleContainer styleContainer, final List<ReportColumn> columns) {
        CreationHelper helper = sheet.getWorkbook().getCreationHelper();
        helper.createDataFormat();

        appendHeaderMainLine(sheet, locale, headerMainLine, styleContainer);

        int columnNumber = 0;

        for (ReportColumn column : columns) {
            HSSFCell cell = headerProduction.createCell(columnNumber);
            cell.setCellValue(column.getHeader(locale));

            column.setHeaderStyle(cell, styleContainer);

            columnNumber++;
        }

        headerProduction.setHeightInPoints(20);

        mergeHeaderCells(sheet, columns.size());
    }

    private void appendHeaderMainLine(final HSSFSheet sheet, final Locale locale, final HSSFRow headerMainLine,
            final PPSReportXlsStyleContainer styleContainer) {
        HSSFCell cell = headerMainLine.createCell(0);
        cell.setCellValue(translationService.translate(PPSReportConstants.COLUMN_HEADER_PLANNED_PRODUCTION, locale));

        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_HeaderStyle1));
    }

    private void mergeHeaderCells(final HSSFSheet sheet, final int numberOfColumns) {
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, numberOfColumns - 1));
        sheet.addMergedRegion(new CellRangeAddress(3, 3, numberOfColumns, 24));

        for (int columnNumber = 0; columnNumber < numberOfColumns; columnNumber++) {
            sheet.addMergedRegion(new CellRangeAddress(4, 5, columnNumber, columnNumber));
        }
    }

    private void createHeaderLineForDaysWithShifts(final HSSFSheet sheet, final Locale locale, final HSSFRow headerMainLine,
            final HSSFRow headerProductionLine, final Entity report, final PPSReportXlsStyleContainer styleContainer,
            final List<ReportColumn> columns) {
        List<Entity> shifts = shiftsService.getShifts();
        DateTime dateFrom = new DateTime(report.getDateField(PPSReportFields.DATE_FROM));
        DateTime dateTo = new DateTime(report.getDateField(PPSReportFields.DATE_TO));
        List<DateTime> days = shiftsService.getDaysBetweenGivenDates(dateFrom, dateTo);

        int columnNumber = columns.size();

        HSSFCell cell = headerMainLine.createCell(columnNumber);
        HSSFCell merge = headerMainLine.createCell(columnNumber + 1);

        cell.setCellValue(translationService.translate(PPSReportConstants.COLUMN_HEADER_PRODUCTION_PER_SHIFT, locale));

        merge.setCellValue("");

        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_HeaderStyle1));
        merge.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_HeaderStyle1));

        HSSFRow headerShifts = sheet.createRow(5);

        for (DateTime day : days) {
            HSSFCell cellDay = headerProductionLine.createCell(columnNumber);
            cellDay.setCellValue(translationService.translate(PPSReportConstants.COLUMN_HEADER_DAY, locale,
                    DATE_FORMAT.format(new Date(day.getMillis()))));

            cellDay.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_HeaderStyle2));

            int shiftColumnNumber = columnNumber;

            for (Entity shift : shifts) {
                columnNumber++;

                HSSFCell cellColumnNumber = headerShifts.createCell(shiftColumnNumber);
                cellColumnNumber.setCellValue(translationService.translate(PPSReportConstants.COLUMN_HEADER_SHIFT_NUMBER, locale,
                        shift.getStringField(ShiftFields.NAME)));

                cellColumnNumber.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_HeaderStyle2));

                shiftColumnNumber++;
            }
        }
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity report) {
        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A3_PAPERSIZE);
        sheet.getPrintSetup().setHResolution((short) 1);

        PPSReportXlsStyleContainer styleContainer = new PPSReportXlsStyleContainer(sheet);

        List<ReportColumn> columns = ppsReportColumnHelper.getReportColumns();

        addSeriesOfReportAuthorAndDate(sheet, report, styleContainer);
        addSeriesOfProductionLine(sheet, report, styleContainer, columns);
    }

    private void addSeriesOfReportAuthorAndDate(final HSSFSheet sheet, final Entity report,
            final PPSReportXlsStyleContainer styleContainer) {
        HSSFRow row = sheet.createRow(1);

        HSSFCell updateDateCell = row.createCell(0);
        updateDateCell.setCellValue(UPDATE_FORMAT.format(report.getDateField(PPSReportFields.UPDATE_DATE)));

        HSSFCell authorCell = row.createCell(2);
        authorCell.setCellValue(ppsReportXlsHelper.getDocumentAuthor(report.getStringField(PPSReportFields.CREATE_USER)));

        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 4));

        updateDateCell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_HeaderStyle2));
        authorCell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_HeaderStyle2));
    }

    private void addSeriesOfProductionLine(final HSSFSheet sheet, final Entity report,
            final PPSReportXlsStyleContainer styleContainer, final List<ReportColumn> columns) {
        List<Entity> productionPerShifts = ppsReportXlsHelper.getProductionPerShiftForReport(report);
        List<Entity> shifts = shiftsService.getShifts();

        DateTime dateFrom = new DateTime(report.getDateField(PPSReportFields.DATE_FROM));

        Shift shiftFirst = new Shift(shifts.get(0), dateFrom, false);
        List<TimeRange> ranges = shiftFirst.findWorkTimeAt(new LocalDate(report.getDateField(PPSReportFields.DATE_FROM)));

        if (ranges.isEmpty()) {
            return;
        }

        LocalTime startTime = ranges.get(0).getFrom();

        if (productionPerShifts.isEmpty()) {
            return;
        }

        productionPerShifts.sort(new EntityProductionPerShiftsComparator());

        String oldProductionLineNumber = "";
        String newProductionLineNumber;

        int rowNum = 6;
        boolean isFirstRow;
        boolean greyBg = false;

        for (Entity productionPerShift : productionPerShifts) {
            Entity order = ppsReportXlsHelper.getOrder(productionPerShift);
            Entity changeover = ppsReportXlsHelper.getChangeover(order);
            Entity productionLine = ppsReportXlsHelper.getProductionLine(productionPerShift);

            newProductionLineNumber = productionLine.getStringField(ProductionLineFields.NUMBER);

            isFirstRow = !oldProductionLineNumber.equals(newProductionLineNumber);

            if (isFirstRow) {
                greyBg = !greyBg;
            }

            if (changeover != null && isChangeOverOnThisPrint(order, report, startTime)) {
                HSSFRow row = sheet.createRow(rowNum++);

                int colIndex = 0;

                for (ReportColumn column : columns) {
                    HSSFCell cell = row.createCell(colIndex);

                    if (isFirstRow) {
                        cell.setCellValue(column.getFirstRowChangeoverValue(productionPerShift));
                    } else {
                        cell.setCellValue(column.getChangeoverValue(productionPerShift));
                    }

                    colIndex++;
                }

                isFirstRow = false;

                addSeriesForChangeOver(sheet, report, row, changeover, order, styleContainer, columns);
            }

            HSSFRow row = sheet.createRow(rowNum++);

            int colIndex = 0;

            for (ReportColumn column : columns) {
                HSSFCell cell = row.createCell(colIndex);

                if (isFirstRow) {
                    Object firstRowValue = column.getFirstRowValue(productionPerShift);

                    if (firstRowValue instanceof Double) {
                        cell.setCellValue((Double) firstRowValue);
                        cell.setCellType(CellType.NUMERIC);
                    } else {
                        cell.setCellValue((String) firstRowValue);
                    }
                } else {
                    Object value = column.getValue(productionPerShift);

                    if (value instanceof Double) {
                        cell.setCellValue((Double) value);
                        cell.setCellType(CellType.NUMERIC);
                    } else {
                        cell.setCellValue((String) value);
                    }
                }

                if (greyBg) {
                    if (columns.size() == colIndex - 1) {
                        column.setGreyDataStyleEnd(cell, styleContainer);
                    } else {
                        column.setGreyDataStyle(cell, styleContainer);
                    }
                } else {
                    if (columns.size() == colIndex - 1) {
                        column.setWhiteDataStyleEnd(cell, styleContainer);
                    } else {
                        column.setWhiteDataStyle(cell, styleContainer);
                    }
                }

                colIndex++;
            }

            addSeriesOfDailyProgress(sheet, report, row, productionPerShift, greyBg, styleContainer, columns);

            oldProductionLineNumber = newProductionLineNumber;
        }

        setColumnWidths(sheet, columns);
    }

    private boolean isChangeOverOnThisPrint(final Entity order, final Entity report, final LocalTime startTime) {
        Date startOrderDate = order.getDateField(OrderFields.START_DATE);
        Date reportFromDate = report.getDateField(PPSReportFields.DATE_FROM);

        DateTime date = new DateTime(reportFromDate).withHourOfDay(startTime.getHourOfDay());

        return startOrderDate.after(date.toDate());
    }

    private void addSeriesOfDailyProgress(final HSSFSheet sheet, final Entity entity, final HSSFRow row,
            final Entity productionPerShift, final boolean rowNumberIsEven, PPSReportXlsStyleContainer styleContainer,
            final List<ReportColumn> columns) {
        List<Entity> shifts = shiftsService.getShifts();
        DateTime dateFrom = new DateTime(entity.getDateField(PPSReportFields.DATE_FROM));
        DateTime dateTo = new DateTime(entity.getDateField(PPSReportFields.DATE_TO));
        List<DateTime> days = shiftsService.getDaysBetweenGivenDates(dateFrom, dateTo);

        int columnNumber = columns.size();

        for (DateTime day : days) {
            for (Entity shift : shifts) {
                HSSFCell cellDailyProgress = row.createCell(columnNumber);

                Entity dailyProgress = ppsReportXlsHelper.getDailyProgress(productionPerShift, day.toDate(), shift);

                if (Objects.isNull(dailyProgress)) {
                    cellDailyProgress.setCellValue("");
                } else {
                    cellDailyProgress.setCellValue(dailyProgress.getDecimalField(DailyProgressFields.QUANTITY).setScale(5).doubleValue());
                    cellDailyProgress.setCellType(CellType.NUMERIC);
                }

                if (rowNumberIsEven) {
                    cellDailyProgress.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_GreyDataStyle));
                } else {
                    cellDailyProgress.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_WhiteDataStyle));
                }

                columnNumber++;

                sheet.autoSizeColumn((short) columnNumber);
            }
        }
    }

    private void addSeriesForChangeOver(final HSSFSheet sheet, final Entity entity, final HSSFRow row, final Entity changeover,
            final Entity order, final PPSReportXlsStyleContainer styleContainer, final List<ReportColumn> columns) {
        Map<Integer, DayShiftHolder> mapCells = Maps.newHashMap();

        List<Entity> shifts = shiftsService.getShifts();
        DateTime dateFrom = new DateTime(entity.getDateField(PPSReportFields.DATE_FROM));
        DateTime dateTo = new DateTime(entity.getDateField(PPSReportFields.DATE_TO));
        List<DateTime> days = shiftsService.getDaysBetweenGivenDates(dateFrom, dateTo);

        Date startDateOrder = order.getDateField(OrderFields.START_DATE);

        Shift shiftFirst = new Shift(shifts.get(0), dateFrom, false);

        List<TimeRange> ranges = shiftFirst.findWorkTimeAt(days.get(0).toLocalDate());

        if (ranges.isEmpty()) {
            return;
        }

        LocalTime startTime = ranges.get(0).getFrom();
        DateTime firstStartShitTime = days.get(0);

        firstStartShitTime = firstStartShitTime.withHourOfDay(startTime.getHourOfDay());
        firstStartShitTime = firstStartShitTime.withMinuteOfHour(startTime.getMinuteOfHour());

        int columnNumber = columns.size();

        if (new DateTime(startDateOrder).minusSeconds(1).toDate().before(firstStartShitTime.toDate())) {
            for (DateTime day : days) {
                for (Entity shift : shifts) {
                    HSSFCell cellDailyProgress = row.createCell(columnNumber);
                    cellDailyProgress.setCellValue("");

                    cellDailyProgress.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_ChangeoverDataStyle));

                    columnNumber++;

                    sheet.autoSizeColumn((short) columnNumber);
                }
            }
        } else {
            for (DateTime day : days) {
                for (Entity shift : shifts) {
                    HSSFCell cell = row.createCell(columnNumber);
                    cell.setCellValue("");

                    DayShiftHolder holder = new DayShiftHolder(shift, day, cell);

                    cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_ChangeoverDataStyle));

                    mapCells.put(columnNumber, holder);

                    columnNumber++;

                    sheet.autoSizeColumn((short) columnNumber);
                }
            }

            columnNumber = columns.size();

            for (DateTime day : days) {
                for (Entity shift : shifts) {
                    Optional<DateTime> maybeShiftStart = getShiftStartDate(day, shift);
                    Optional<DateTime> maybeShiftEnd = getShiftEndDate(day, shift);

                    HSSFCell cell = mapCells.get(columnNumber).getCell();

                    if (!maybeShiftStart.isPresent() || !maybeShiftEnd.isPresent()) {
                        cell.setCellValue("");

                        columnNumber++;

                        continue;
                    }

                    DateTime shiftEnd = maybeShiftEnd.get();
                    DateTime shiftStart = maybeShiftStart.get();

                    if (shiftStart.toDate().after(shiftEnd.toDate())) {
                        shiftEnd = shiftEnd.plusDays(1);
                    }

                    boolean isChangeover = betweenDates(shiftStart.toDate(), shiftEnd.toDate(),
                            new DateTime(startDateOrder).plusSeconds(1).toDate());

                    if (isChangeover) {
                        int duration = changeover.getIntegerField(LineChangeoverNormsFields.DURATION);
                        int changeoverOnShift = Seconds.secondsBetween(shiftStart, new DateTime(startDateOrder)).getSeconds();

                        if (duration - changeoverOnShift > 0) {
                            if (changeoverOnShift > 0) {
                                cell.setCellValue(DurationFormatUtils.formatDuration(changeoverOnShift * 1000, "HH:mm"));
                            }

                            int durationToMark = duration - changeoverOnShift;
                            int currentIndex = columnNumber - 1;

                            if (currentIndex >= columns.size()) {
                                while (durationToMark > 0) {
                                    HSSFCell cellBefore = mapCells.get(currentIndex).getCell();

                                    Optional<DateTime> maybeStart = getShiftStartDate(day, shift);
                                    Optional<DateTime> maybeEnd = getShiftEndDate(day, shift);

                                    if (!maybeStart.isPresent() || !maybeEnd.isPresent()) {
                                        cell.setCellValue("");

                                        continue;
                                    }

                                    DateTime start = maybeStart.get();
                                    DateTime end = maybeEnd.get();

                                    if (start.toDate().after(end.toDate())) {
                                        end = end.plusDays(1);
                                    }

                                    int seconds = Seconds.secondsBetween(start, end).getSeconds();

                                    if (durationToMark > seconds) {
                                        cellBefore.setCellValue(DurationFormatUtils.formatDuration(seconds * 1000, "HH:mm"));

                                        durationToMark = durationToMark - seconds;
                                        currentIndex = currentIndex - 1;

                                        if (currentIndex < columns.size()) {
                                            break;
                                        }
                                    } else {
                                        cellBefore
                                                .setCellValue(DurationFormatUtils.formatDuration(durationToMark * 1000, "HH:mm"));

                                        durationToMark = durationToMark - seconds;
                                    }
                                }
                            }
                        } else {
                            cell.setCellValue(DurationFormatUtils.formatDuration(duration * 1000, "HH:mm"));
                        }
                    }

                    columnNumber++;
                }
            }
        }

        for (int i = 0; i < columnNumber; i++) {
            row.getCell(i).setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_ChangeoverDataStyle));
        }
    }

    private boolean betweenDates(final Date start, final Date end, final Date date) {
        return date.after(start) && date.before(end);
    }

    private Optional<DateTime> getShiftStartDate(final DateTime day, final Entity shift) {
        Shift shiftFirst = new Shift(shift);

        List<TimeRange> ranges = shiftFirst.findWorkTimeAt(day.toLocalDate());

        if (ranges.isEmpty()) {
            return Optional.empty();
        }

        LocalTime startTime = ranges.get(0).getFrom();

        DateTime startShitTime = day;

        startShitTime = startShitTime.withHourOfDay(startTime.getHourOfDay());
        startShitTime = startShitTime.withMinuteOfHour(startTime.getMinuteOfHour());

        return Optional.of(startShitTime);
    }

    private Optional<DateTime> getShiftEndDate(final DateTime day, final Entity shift) {
        Shift shiftFirst = new Shift(shift);

        List<TimeRange> ranges = shiftFirst.findWorkTimeAt(day.toLocalDate());

        if (ranges.isEmpty()) {
            return Optional.empty();
        }

        LocalTime startTime = ranges.get(0).getTo();

        DateTime startShitTime = day;

        startShitTime = startShitTime.withHourOfDay(startTime.getHourOfDay());
        startShitTime = startShitTime.withMinuteOfHour(startTime.getMinuteOfHour());

        return Optional.of(startShitTime);
    }

    private void setColumnWidths(final HSSFSheet sheet, final List<ReportColumn> columns) {
        int index = 0;

        for (ReportColumn column : columns) {
            sheet.setColumnWidth(index, column.getColumnWidth());

            index++;
        }

        for (int columnNumber = columns.size(); columnNumber < columns.size() + 21; columnNumber++) {
            sheet.setColumnWidth(columnNumber, 6 * 256);
        }
    }

}
