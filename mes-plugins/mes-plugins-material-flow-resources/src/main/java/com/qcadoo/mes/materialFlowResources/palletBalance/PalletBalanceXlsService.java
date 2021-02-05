package com.qcadoo.mes.materialFlowResources.palletBalance;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.constants.PalletBalanceFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.xls.XlsDocumentService;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PalletBalanceXlsService extends XlsDocumentService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PalletBalanceReportHelper palletBalanceReportHelper;

    private static final List<String> HEADER_KEYS = Lists.newArrayList("initialState", "inbounds", "outbounds", "moves",
            "finalState");

    @Override
    public String getReportTitle(Locale locale) {
        return translationService.translate("materialFlowResource.palletBalance.report.title", locale);
    }

    private HSSFCell createRegularCell(StylesContainer stylesContainer, HSSFRow row, int column, String content) {
        HSSFCell cell = row.createCell(column);
        cell.setCellValue(content);
        cell.setCellStyle(StylesContainer.aligned(stylesContainer.regularStyle, HorizontalAlignment.LEFT));
        return cell;
    }

    private HSSFCell createNumericCell(StylesContainer stylesContainer, HSSFRow row, int column, int value) {
        HSSFCell cell = row.createCell(column, HSSFCell.CELL_TYPE_NUMERIC);
        cell.setCellValue(value);
        cell.setCellStyle(StylesContainer.aligned(stylesContainer.regularStyle, HorizontalAlignment.LEFT));
        return cell;
    }

    private HSSFCell createHeaderCell(StylesContainer stylesContainer, HSSFRow row, String content, int column, HorizontalAlignment horizontalAlignment) {
        HSSFCell cell = row.createCell(column);
        cell.setCellValue(content);
        cell.setCellStyle(StylesContainer.aligned(stylesContainer.headerStyle, horizontalAlignment));
        return cell;
    }

    @Override
    protected void addHeader(HSSFSheet sheet, Locale locale, Entity palletBalance) {

        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);

        List<String> typesOfPallet = palletBalanceReportHelper.getTypesOfPallet();
        HSSFRow headerRow = sheet.createRow(0);
        HSSFRow typesOfPalletRow = sheet.createRow(1);
        addDateHeader(sheet, locale, headerRow, stylesContainer);
        addTypesOfPalletHeader(sheet, locale, headerRow, typesOfPalletRow, stylesContainer, typesOfPallet);
    }

    private void addDateHeader(HSSFSheet sheet, Locale locale, HSSFRow headerRow, StylesContainer stylesContainer) {
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
        createHeaderCell(stylesContainer, headerRow,
                translationService.translate("materialFlowResource.palletBalance.report.date", locale), 0,
                HorizontalAlignment.CENTER);
    }

    private void addTypesOfPalletHeader(HSSFSheet sheet, Locale locale, HSSFRow headerRow, HSSFRow typesOfPalletRow,
            StylesContainer stylesContainer, List<String> typesOfPallet) {

        int columnIndex = 1;
        int typesOfPalletCount = typesOfPallet.size();
        int lastColumnIndex = typesOfPalletCount + columnIndex - 1;

        for (String key : HEADER_KEYS) {
            if (key.equals("moves")) {

                addHeaderCell(sheet,
                        translationService.translate("materialFlowResource.palletBalance.report.header." + key, locale),
                        headerRow, columnIndex, columnIndex + 1, stylesContainer);
                createHeaderCell(stylesContainer, typesOfPalletRow,
                        translationService.translate("materialFlowResource.palletBalance.report.header.movesIn", locale),
                        columnIndex, HorizontalAlignment.LEFT);
                columnIndex++;

                createHeaderCell(stylesContainer, typesOfPalletRow,
                        translationService.translate("materialFlowResource.palletBalance.report.header.movesOut", locale),
                        columnIndex, HorizontalAlignment.LEFT);
                columnIndex++;
                lastColumnIndex = columnIndex + typesOfPalletCount - 1;

            } else {
                addHeaderCell(sheet,
                        translationService.translate("materialFlowResource.palletBalance.report.header." + key, locale),
                        headerRow, columnIndex, lastColumnIndex, stylesContainer);
                for (int i = 0; i < typesOfPalletCount; i++) {
                    createHeaderCell(stylesContainer, typesOfPalletRow, typesOfPallet.get(i), columnIndex + i,
                            HorizontalAlignment.LEFT);
                }
                columnIndex = lastColumnIndex + 1;
                lastColumnIndex += typesOfPalletCount;
            }
        }
    }

    private void addHeaderCell(HSSFSheet sheet, String content, HSSFRow headerRow, int columnIndex, int lastColumnIndex,
            StylesContainer stylesContainer) {
        sheet.addMergedRegion(new CellRangeAddress(0, 0, columnIndex, lastColumnIndex));
        createHeaderCell(stylesContainer, headerRow, content, columnIndex, HorizontalAlignment.CENTER);
    }

    @Override
    protected void addSeries(HSSFSheet sheet, Entity palletBalance) {
        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A3_PAPERSIZE);
        sheet.getPrintSetup().setHResolution((short) 1);

        final FontsContainer fontsContainer = new FontsContainer(sheet.getWorkbook());
        final StylesContainer stylesContainer = new StylesContainer(sheet.getWorkbook(), fontsContainer);

        Date dateFrom = palletBalance.getDateField(PalletBalanceFields.DATE_FROM);
        Date dateTo = DateUtils.truncate(palletBalance.getDateField(PalletBalanceFields.DATE_TO), Calendar.DATE);
        boolean includeWeekends = palletBalance.getBooleanField(PalletBalanceFields.INCLUDE_WEEKENDS);
        List<String> typesOfPallet = palletBalanceReportHelper.getTypesOfPallet();

        Map<Date, List<PalletBalanceRowDto>> inbounds = palletBalanceReportHelper.getInbounds(dateFrom);
        Map<Date, List<PalletBalanceRowDto>> outbounds = palletBalanceReportHelper.getOutbounds(dateFrom);
        Map<Date, List<PalletBalanceRowDto>> initialState = Maps.newHashMap();
        Map<Date, Integer> moves = palletBalanceReportHelper.getMoves(dateFrom);
        Map<Date, List<PalletBalanceRowDto>> finalState = palletBalanceReportHelper.getCurrentState(dateTo);
        palletBalanceReportHelper.fillFinalAndInitialState(typesOfPallet, finalState, initialState, inbounds, outbounds,
                dateFrom, dateTo);

        int columnIndex = 1;

        int rowIndex = 2;
        int columnMax = 0;
        DateTime currentDate = new DateTime(dateFrom);
        while (currentDate.toDate().compareTo(dateTo) <= 0) {
            if (!includeWeekends && currentDate.getDayOfWeek() > DateTimeConstants.FRIDAY) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            HSSFRow row = sheet.createRow(rowIndex);

            createRegularCell(stylesContainer, row, 0, currentDate.toString("dd.MM.yyyy"));
            Date current = currentDate.toDate();

            columnIndex = createRowPart(initialState, row, columnIndex, typesOfPallet, current, stylesContainer);
            columnIndex = createRowPart(inbounds, row, columnIndex, typesOfPallet, current, stylesContainer);
            columnIndex = createRowPart(outbounds, row, columnIndex, typesOfPallet, current, stylesContainer);
            columnIndex = createMovesRowPart(moves, row, columnIndex, current, stylesContainer);
            createRowPart(finalState, row, columnIndex, typesOfPallet, current, stylesContainer);

            columnMax = columnIndex;
            columnIndex = 1;
            rowIndex++;
            currentDate = currentDate.plusDays(1);
        }
        for (int i = 0; i <= columnMax; i++) {
            sheet.autoSizeColumn(i, false);
        }
    }

    private int createRowPart(Map<Date, List<PalletBalanceRowDto>> data, HSSFRow row, int columnIndex,
            List<String> typesOfPallet, Date currentDate, StylesContainer stylesContainer) {
        for (String typeOfPallet : typesOfPallet) {
            if (data.containsKey(currentDate)) {
                PalletBalanceRowDto stateForDay = data.get(currentDate).stream()
                        .filter(dto -> typeOfPallet.equals(dto.getTypeOfPallet())).findAny().orElse(new PalletBalanceRowDto());
                createNumericCell(stylesContainer, row, columnIndex, stateForDay.getPalletsCount());

            } else {
                createNumericCell(stylesContainer, row, columnIndex, 0);
            }
            columnIndex++;
        }
        return columnIndex;
    }

    private int createMovesRowPart(Map<Date, Integer> data, HSSFRow row, int columnIndex, Date currentDate,
            StylesContainer stylesContainer) {
        if (data.containsKey(currentDate)) {
            createNumericCell(stylesContainer, row, columnIndex, data.get(currentDate));
            columnIndex++;
            createNumericCell(stylesContainer, row, columnIndex, data.get(currentDate));
            columnIndex++;
        } else {
            createNumericCell(stylesContainer, row, columnIndex, 0);
            columnIndex++;
            createNumericCell(stylesContainer, row, columnIndex, 0);
            columnIndex++;
        }
        return columnIndex;
    }

    private static class StylesContainer {

        private final HSSFCellStyle regularStyle;

        private final HSSFCellStyle headerStyle;

        StylesContainer(HSSFWorkbook workbook, FontsContainer fontsContainer) {
            regularStyle = workbook.createCellStyle();
            regularStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            headerStyle = workbook.createCellStyle();
            headerStyle.setFont(fontsContainer.headerFont);
        }

        private static HSSFCellStyle aligned(HSSFCellStyle style, HorizontalAlignment horizontalAlignment) {
            style.setAlignment(horizontalAlignment);
            return style;
        }

    }

    private static class FontsContainer {

        private final Font headerFont;

        FontsContainer(HSSFWorkbook workbook) {

            headerFont = workbook.createFont();
            headerFont.setBold(true);
        }
    }
}
