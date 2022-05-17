/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.assignmentToShift.print.xls;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields;
import com.qcadoo.mes.assignmentToShift.constants.OccupationType;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.FactoryFields;
import com.qcadoo.mes.basic.constants.ShiftFields;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.DictionaryFields;
import com.qcadoo.model.constants.DictionaryItemFields;
import com.qcadoo.model.constants.QcadooModelConstants;
import com.qcadoo.report.api.xls.XlsDocumentService;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssignmentToShiftXlsService extends XlsDocumentService {

    public static final String L_OCCUPATION_TYPE = "occupationType";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private AssignmentToShiftXlsHelper assignmentToShiftXlsHelper;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsService shiftsService;

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate(AssignmentToShiftReportConstants.TITLE, locale);
    }

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale, final Entity assignmentToShiftReport) {
        AssignmentToShiftXlsStyleContainer styleContainer = new AssignmentToShiftXlsStyleContainer(sheet);
        createHeaderForAuthor(sheet, locale, assignmentToShiftReport, styleContainer);
        createHeaderForAssignmentToShift(sheet, locale, assignmentToShiftReport, styleContainer);
    }

    private void createHeaderForAuthor(final HSSFSheet sheet, final Locale locale, final Entity assignmentToShiftReport, AssignmentToShiftXlsStyleContainer styleContainer) {
        HSSFRow headerAuthorLine = sheet.createRow(1);

        String shift = translationService.translate(AssignmentToShiftReportConstants.COLUMN_HEADER_SHIFT, locale) + " "
                + assignmentToShiftReport.getBelongsToField(AssignmentToShiftFields.SHIFT).getStringField(ShiftFields.NAME);
        String factory = translationService.translate(AssignmentToShiftReportConstants.COLUMN_HEADER_FACTORY, locale) + " "
                + assignmentToShiftReport.getBelongsToField(AssignmentToShiftFields.FACTORY).getStringField(FactoryFields.NAME);
        String user = translationService.translate(AssignmentToShiftReportConstants.COLUMN_HEADER_AUTHOR, locale) + " "
                + assignmentToShiftReport.getField(AssignmentToShiftReportFields.CREATE_USER).toString();
        String date = translationService.translate(AssignmentToShiftReportConstants.COLUMN_HEADER_UPDATE_DATE, locale) + " "
                + DateFormat.getDateInstance()
                .format(assignmentToShiftReport.getField(AssignmentToShiftReportFields.UPDATE_DATE));

        HSSFCell headerAuthorLineCell0 = headerAuthorLine.createCell(0);
        headerAuthorLineCell0.setCellValue(shift);
        HSSFCell headerAuthorLineCell3 = headerAuthorLine.createCell(3);
        headerAuthorLineCell3.setCellValue(date);
        HSSFCell headerAuthorLineCell6 = headerAuthorLine.createCell(6);
        headerAuthorLineCell6.setCellValue(user);

        headerAuthorLine.setHeightInPoints(30);

        HSSFRow headerAuthorFactoryLine = sheet.createRow(2);
        HSSFCell headerAuthorFactoryLineCell0 = headerAuthorFactoryLine.createCell(0);
        headerAuthorFactoryLineCell0.setCellValue(factory);

        headerAuthorFactoryLine.setHeightInPoints(20);

        DateTime dateFrom = new DateTime(assignmentToShiftReport.getDateField(AssignmentToShiftReportFields.DATE_FROM));
        DateTime dateTo = new DateTime(assignmentToShiftReport.getDateField(AssignmentToShiftReportFields.DATE_TO));
        addMarginsAndStylesForAuthor(sheet, 1,
                shiftsService.getNumberOfDaysBetweenGivenDates(dateFrom, dateTo), styleContainer);
        addMarginsAndStylesForAuthorFactory(sheet, 2,
                shiftsService.getNumberOfDaysBetweenGivenDates(dateFrom, dateTo), styleContainer);
    }

    private void createHeaderForAssignmentToShift(final HSSFSheet sheet, final Locale locale,
                                                  final Entity assignmentToShiftReport, AssignmentToShiftXlsStyleContainer styleContainer) {
        DateTime dateFrom = new DateTime(assignmentToShiftReport.getDateField(AssignmentToShiftReportFields.DATE_FROM));
        DateTime dateTo = new DateTime(assignmentToShiftReport.getDateField(AssignmentToShiftReportFields.DATE_TO));
        List<DateTime> days = shiftsService.getDaysBetweenGivenDates(dateFrom, dateTo);

        if (days != null) {
            HSSFRow headerAssignmentToShift = sheet.createRow(4);

            String occupationType = translationService.translate(AssignmentToShiftReportConstants.COLUMN_HEADER_OCCUPATIONTYPE,
                    locale);

            HSSFCell cell0 = headerAssignmentToShift.createCell(0);
            cell0.setCellValue(occupationType);

            int columnNumber = 1;
            for (DateTime day : days) {
                HSSFCell cellDay = headerAssignmentToShift.createCell(columnNumber);

                cellDay.setCellValue(translationService.translate(AssignmentToShiftReportConstants.COLUMN_HEADER_DAY, locale,
                        DateFormat.getDateInstance().format(new Date(day.getMillis()))));

                columnNumber += 3;
            }

            headerAssignmentToShift.setHeightInPoints(14);

            addMarginsAndStylesForAssignmentToShift(sheet, 4,
                    shiftsService.getNumberOfDaysBetweenGivenDates(dateFrom, dateTo), styleContainer);
        }
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity assignmentToShiftReport) {
        AssignmentToShiftXlsStyleContainer styleContainer = new AssignmentToShiftXlsStyleContainer(sheet);
        DateTime dateFrom = new DateTime(assignmentToShiftReport.getDateField(AssignmentToShiftReportFields.DATE_FROM));
        DateTime dateTo = new DateTime(assignmentToShiftReport.getDateField(AssignmentToShiftReportFields.DATE_TO));
        List<DateTime> days = shiftsService.getDaysBetweenGivenDates(dateFrom, dateTo);

        if (days != null) {
            int rowNum = 5;

            List<Entity> occupationTypesWithoutTechnicalCode = getOccupationTypeDictionaryWithoutTechnicalCode();
            List<Entity> productionlines = assignmentToShiftXlsHelper.getProductionLines();

            if (!productionlines.isEmpty()) {
                rowNum = fillColumnWithStaffForWorkOnLine(sheet, rowNum, assignmentToShiftReport, days, productionlines,
                        getDictionaryItemWithProductionOnLine(), styleContainer);
            }

            for (Entity dictionaryItem : occupationTypesWithoutTechnicalCode) {
                rowNum = fillColumnWithStaffForOtherTypes(sheet, rowNum, assignmentToShiftReport, days, dictionaryItem, styleContainer);
            }

            fillColumnWithStaffForOtherTypes(sheet, rowNum, assignmentToShiftReport, days, getDictionaryItemWithOtherCase(), styleContainer);

            sheet.autoSizeColumn(0);
        }
    }

    private int fillColumnWithStaffForWorkOnLine(final HSSFSheet sheet, int rowNum, final Entity assignmentToShiftReport,
                                                 final List<DateTime> days, final List<Entity> productionLines, final Entity dictionaryItem, AssignmentToShiftXlsStyleContainer styleContainer) {
        if ((assignmentToShiftReport != null) && (days != null) && (productionLines != null)) {
            DateTime dateFrom = new DateTime(assignmentToShiftReport.getDateField(AssignmentToShiftReportFields.DATE_FROM));
            DateTime dateTo = new DateTime(assignmentToShiftReport.getDateField(AssignmentToShiftReportFields.DATE_TO));
            for (Entity productionLine : productionLines) {
                int rowNumFromLastSection = rowNum;
                int numberOfColumnsForWorkers = getNumberOfRowsForWorkers(assignmentToShiftReport, days, productionLine,
                        dictionaryItem);

                for (int i = 0; i < numberOfColumnsForWorkers; i++) {
                    sheet.createRow(rowNum);
                    rowNum++;
                }

                String productionLineValue;

                if (productionLine.getStringField(ProductionLineFields.PLACE) == null) {
                    productionLineValue = productionLine.getStringField(ProductionLineFields.NUMBER);
                } else {
                    productionLineValue = productionLine.getStringField(ProductionLineFields.NUMBER) + "-"
                            + productionLine.getStringField(ProductionLineFields.PLACE);
                }

                HSSFRow firstRowInSection;

                if (sheet.getRow(rowNumFromLastSection) == null) {
                    firstRowInSection = sheet.createRow(rowNumFromLastSection);

                    rowNum++;
                } else {
                    firstRowInSection = sheet.getRow(rowNumFromLastSection);

                }

                HSSFCell cell = firstRowInSection.createCell(0);
                cell.setCellValue(productionLineValue);

                int columnNumber = 1;
                int maxLength = 0;

                for (DateTime day : days) {
                    List<Entity> assignmentsToShift = assignmentToShiftXlsHelper.getAssignmentToShift(
                            assignmentToShiftReport.getBelongsToField(AssignmentToShiftReportFields.SHIFT),
                            assignmentToShiftReport.getBelongsToField(AssignmentToShiftReportFields.FACTORY), day);

                    if (assignmentsToShift == null || assignmentsToShift.isEmpty()) {
                        columnNumber += 3;

                        continue;
                    }
                    List<Entity> staffs = Lists.newArrayList();
                    for (Entity assignmentToShift : assignmentsToShift) {
                        List<Entity> staffsForDay = assignmentToShiftXlsHelper.getStaffsList(assignmentToShift,
                                dictionaryItem.getStringField(DictionaryItemFields.NAME), productionLine);
                        staffs.addAll(staffsForDay);
                    }
                    if (staffs.isEmpty()) {
                        columnNumber += 3;

                        continue;
                    }

                    String staffsValue = assignmentToShiftXlsHelper.getListOfWorkers(staffs);

                    List<String> workers = assignmentToShiftXlsHelper.getListOfWorker(staffs);

                    int rowIndex = rowNumFromLastSection;

                    for (String worker : workers) {
                        sheet.getRow(rowIndex).createCell(columnNumber).setCellValue(worker);
                        rowIndex++;
                    }

                    if (workers.isEmpty()) {
                        sheet.getRow(rowIndex).createCell(columnNumber).setCellValue(" ");

                    }

                    if (maxLength < staffsValue.length()) {
                        maxLength = staffsValue.length();
                    }

                    columnNumber += 3;
                }

                for (int i = rowNumFromLastSection; i < rowNum; i++) {
                    addMarginsAndStylesForSeries(sheet, i,
                            shiftsService.getNumberOfDaysBetweenGivenDates(dateFrom, dateTo), styleContainer);
                }
            }
        }

        return rowNum;
    }

    private int getNumberOfRowsForWorkers(final Entity assignmentToShiftReport, final List<DateTime> days,
                                          final Entity productionLine, final Entity dictionaryItem) {
        int numberOfWorkers = 0;

        for (DateTime day : days) {
            List<Entity> assignmentsToShift = assignmentToShiftXlsHelper.getAssignmentToShift(
                    assignmentToShiftReport.getBelongsToField(AssignmentToShiftFields.SHIFT),
                    assignmentToShiftReport.getBelongsToField(AssignmentToShiftReportFields.FACTORY), day);

            List<Entity> staffs = Lists.newArrayList();
            for (Entity assignmentToShift : assignmentsToShift) {
                staffs.addAll(assignmentToShiftXlsHelper.getStaffsList(assignmentToShift,
                        dictionaryItem.getStringField(DictionaryItemFields.NAME), productionLine));
            }

            List<String> workers = assignmentToShiftXlsHelper.getListOfWorker(staffs);

            if (workers.size() > numberOfWorkers) {
                numberOfWorkers = workers.size();
            }
        }

        return numberOfWorkers;
    }

    private int getNumberOfRowsForWorkersForOtherTypes(final Entity assignmentToShiftReport, final List<DateTime> days,
                                                       final Entity dictionaryItem) {
        int numberOfWorkers = 0;

        for (DateTime day : days) {
            List<Entity> assignmentsToShift = assignmentToShiftXlsHelper.getAssignmentToShift(
                    assignmentToShiftReport.getBelongsToField(AssignmentToShiftFields.SHIFT),
                    assignmentToShiftReport.getBelongsToField(AssignmentToShiftReportFields.FACTORY), day);

            List<Entity> staffs = Lists.newArrayList();
            for (Entity assignmentToShift : assignmentsToShift) {
                staffs.addAll(assignmentToShiftXlsHelper.getStaffsList(assignmentToShift,
                        dictionaryItem.getStringField(DictionaryItemFields.NAME), null));
            }

            List<String> workers;

            if (OccupationType.OTHER_CASE.getStringValue()
                    .equals(dictionaryItem.getStringField(DictionaryItemFields.TECHNICAL_CODE))) {
                workers = assignmentToShiftXlsHelper.getListOfWorkerWithOtherCases(staffs);
            } else {
                workers = assignmentToShiftXlsHelper.getListOfWorker(staffs);
            }

            if (workers.size() > numberOfWorkers) {
                numberOfWorkers = workers.size();
            }
        }

        return numberOfWorkers;
    }

    private int fillColumnWithStaffForOtherTypes(final HSSFSheet sheet, int rowNum, final Entity assignmentToShiftReport,
                                                 final List<DateTime> days, final Entity dictionaryItem, AssignmentToShiftXlsStyleContainer styleContainer) {
        if ((assignmentToShiftReport != null) && (days != null) && (dictionaryItem != null)) {
            DateTime dateFrom = new DateTime(assignmentToShiftReport.getDateField(AssignmentToShiftReportFields.DATE_FROM));
            DateTime dateTo = new DateTime(assignmentToShiftReport.getDateField(AssignmentToShiftReportFields.DATE_TO));
            int rowNumFromLastSection = rowNum;

            int numberOfColumnsForWorkers = getNumberOfRowsForWorkersForOtherTypes(assignmentToShiftReport, days, dictionaryItem);

            for (int i = 0; i < numberOfColumnsForWorkers; i++) {
                sheet.createRow(rowNum);
                rowNum++;
            }

            String occupationTypeValue = dictionaryItem.getStringField(DictionaryItemFields.NAME);

            HSSFRow firstRowInSection;

            if (sheet.getRow(rowNumFromLastSection) == null) {
                firstRowInSection = sheet.createRow(rowNumFromLastSection);
                rowNum++;
            } else {
                firstRowInSection = sheet.getRow(rowNumFromLastSection);
            }

            HSSFCell cell = firstRowInSection.createCell(0);
            cell.setCellValue(occupationTypeValue);

            int columnNumber = 1;

            for (DateTime day : days) {
                List<Entity> assignmentsToShift = assignmentToShiftXlsHelper.getAssignmentToShift(
                        assignmentToShiftReport.getBelongsToField(AssignmentToShiftReportFields.SHIFT),
                        assignmentToShiftReport.getBelongsToField(AssignmentToShiftReportFields.FACTORY), day);

                if (assignmentsToShift == null || assignmentsToShift.isEmpty()) {
                    columnNumber += 3;

                    continue;
                }
                List<Entity> staffs = Lists.newArrayList();
                for (Entity assignmentToShift : assignmentsToShift) {
                    staffs.addAll(assignmentToShiftXlsHelper.getStaffsList(assignmentToShift,
                            dictionaryItem.getStringField(DictionaryItemFields.NAME), null));
                }

                if (staffs.isEmpty()) {
                    columnNumber += 3;

                    continue;
                }

                List<String> workers;

                if (OccupationType.OTHER_CASE.getStringValue()
                        .equals(dictionaryItem.getStringField(DictionaryItemFields.TECHNICAL_CODE))) {
                    workers = assignmentToShiftXlsHelper.getListOfWorkerWithOtherCases(staffs);
                } else {
                    workers = assignmentToShiftXlsHelper.getListOfWorker(staffs);
                }

                int rowIndex = rowNumFromLastSection;

                for (String worker : workers) {
                    sheet.getRow(rowIndex).createCell(columnNumber).setCellValue(worker);
                    rowIndex++;
                }

                if (workers.isEmpty()) {
                    sheet.getRow(rowIndex).createCell(columnNumber).setCellValue(" ");

                }

                columnNumber += 3;
            }

            for (int i = rowNumFromLastSection; i < rowNum; i++) {
                addMarginsAndStylesForSeries(sheet, i,
                        shiftsService.getNumberOfDaysBetweenGivenDates(dateFrom, dateTo), styleContainer);
            }
        }

        return rowNum;
    }

    private List<Entity> getOccupationTypeDictionaryWithoutTechnicalCode() {
        Entity occupationTypeDictionary = dataDefinitionService
                .get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY).find()
                .add(SearchRestrictions.eq(DictionaryFields.NAME, L_OCCUPATION_TYPE)).uniqueResult();

        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM)
                .find().add(SearchRestrictions.belongsTo(DictionaryItemFields.DICTIONARY, occupationTypeDictionary))
                .add(SearchRestrictions.isNull(DictionaryItemFields.TECHNICAL_CODE)).add(SearchRestrictions.eq("active", true))
                .list().getEntities();
    }

    private Entity getDictionaryItemWithProductionOnLine() {
        Entity occupationTypeDictionary = dataDefinitionService
                .get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY).find()
                .add(SearchRestrictions.eq(DictionaryFields.NAME, L_OCCUPATION_TYPE)).uniqueResult();

        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM)
                .find().add(SearchRestrictions.belongsTo(DictionaryItemFields.DICTIONARY, occupationTypeDictionary))
                .add(SearchRestrictions.eq(DictionaryItemFields.TECHNICAL_CODE, OccupationType.WORK_ON_LINE.getStringValue()))
                .uniqueResult();
    }

    private Entity getDictionaryItemWithOtherCase() {
        Entity occupationTypeDictionary = dataDefinitionService
                .get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY).find()
                .add(SearchRestrictions.eq(DictionaryFields.NAME, L_OCCUPATION_TYPE)).uniqueResult();

        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM)
                .find().add(SearchRestrictions.belongsTo(DictionaryItemFields.DICTIONARY, occupationTypeDictionary))
                .add(SearchRestrictions.eq(DictionaryItemFields.TECHNICAL_CODE, OccupationType.OTHER_CASE.getStringValue()))
                .uniqueResult();
    }

    private void addMarginsAndStylesForAuthor(final HSSFSheet sheet, final int rowNumber, final int numberOfDays, AssignmentToShiftXlsStyleContainer styleContainer) {
        int firstColumnNumber = 0;
        int lastColumnNumber;
        int margin = 3;

        if (numberOfDays < 3) {
            lastColumnNumber = 10;
        } else {
            lastColumnNumber = (numberOfDays + 1) * margin;
        }

        for (int columnNumber = firstColumnNumber; columnNumber <= lastColumnNumber; columnNumber++) {
            if (sheet.getRow(rowNumber).getCell(columnNumber) == null) {
                sheet.getRow(rowNumber).createCell(columnNumber);
            }

            if (columnNumber == firstColumnNumber) {
                sheet.getRow(rowNumber).getCell(columnNumber).setCellStyle(styleContainer.getStyles().get(AssignmentToShiftXlsStyleContainer.GREY_DATA_STYLE_BORDER_TOP_LEFT_ALIGN_LEFT_BOLD));
            } else if (columnNumber == lastColumnNumber) {
                sheet.getRow(rowNumber).getCell(columnNumber).setCellStyle(styleContainer.getStyles().get(AssignmentToShiftXlsStyleContainer.GREY_DATA_STYLE_BORDER_TOP_RIGHT_ALIGN_LEFT_BOLD));
            } else {
                sheet.getRow(rowNumber).getCell(columnNumber).setCellStyle(styleContainer.getStyles().get(AssignmentToShiftXlsStyleContainer.GREY_DATA_STYLE_BORDER_TOP_ALIGN_LEFT_BOLD));
            }
        }

        sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, firstColumnNumber, firstColumnNumber + margin - 1));
        sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, firstColumnNumber + margin, firstColumnNumber
                + (margin * 2) - 1));
        sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, firstColumnNumber + (margin * 2), lastColumnNumber));
    }

    private void addMarginsAndStylesForAuthorFactory(final HSSFSheet sheet, final int rowNumber, final int numberOfDays, AssignmentToShiftXlsStyleContainer styleContainer) {
        int firstColumnNumber = 0;
        int lastColumnNumber;
        int margin = 3;

        if (numberOfDays < 3) {
            lastColumnNumber = 10;
        } else {
            lastColumnNumber = (numberOfDays + 1) * margin;
        }

        for (int columnNumber = firstColumnNumber; columnNumber <= lastColumnNumber; columnNumber++) {
            if (sheet.getRow(rowNumber).getCell(columnNumber) == null) {
                sheet.getRow(rowNumber).createCell(columnNumber);
            }

            if (columnNumber == firstColumnNumber) {
                sheet.getRow(rowNumber).getCell(columnNumber).setCellStyle(styleContainer.getStyles().get(AssignmentToShiftXlsStyleContainer.GREY_DATA_STYLE_BORDER_LEFT_BOTTOM_ALIGN_LEFT_BOLD));
            } else if (columnNumber == lastColumnNumber) {
                sheet.getRow(rowNumber).getCell(columnNumber).setCellStyle(styleContainer.getStyles().get(AssignmentToShiftXlsStyleContainer.GREY_DATA_STYLE_BORDER_RIGHT_BOTTOM_ALIGN_LEFT_BOLD));
            } else {
                sheet.getRow(rowNumber).getCell(columnNumber).setCellStyle(styleContainer.getStyles().get(AssignmentToShiftXlsStyleContainer.GREY_DATA_STYLE_BORDER_BOTTOM_ALIGN_LEFT_BOLD));
            }
        }

        sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, firstColumnNumber, lastColumnNumber));
    }

    private void addMarginsAndStylesForAssignmentToShift(final HSSFSheet sheet, final int rowNumber, final int numberOfDays, AssignmentToShiftXlsStyleContainer styleContainer) {
        int margin = 3;
        int firstColumn = 0;
        int lastColumn = (numberOfDays + 1) * margin;

        for (int columnNumber = firstColumn; columnNumber <= lastColumn; columnNumber++) {
            if (sheet.getRow(rowNumber).getCell(columnNumber) == null) {
                sheet.getRow(rowNumber).createCell(columnNumber);
            }

            sheet.getRow(rowNumber).getCell(columnNumber).setCellStyle(styleContainer.getStyles().get(AssignmentToShiftXlsStyleContainer.WHITE_DATA_STYLE_BORDER_BOX_ALIGN_CENTER_BOLD));
        }

        for (int columnNumber = 1; columnNumber <= lastColumn; columnNumber += margin) {
            sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, columnNumber, columnNumber + 2));
        }
    }

    private void addMarginsAndStylesForSeries(final HSSFSheet sheet, final int rowNumber, final int numberOfDays, AssignmentToShiftXlsStyleContainer styleContainer) {
        int margin = 3;
        int firstColumn = 0;
        int lastColumn = (numberOfDays + 1) * margin;

        for (int columnNumber = firstColumn; columnNumber <= lastColumn; columnNumber++) {
            if (sheet.getRow(rowNumber).getCell(columnNumber) == null) {
                sheet.getRow(rowNumber).createCell(columnNumber);
            }

            if (columnNumber == firstColumn) {
                sheet.getRow(rowNumber).getCell(columnNumber).setCellStyle(styleContainer.getStyles().get(AssignmentToShiftXlsStyleContainer.WHITE_DATA_STYLE_BORDER_BOX_ALIGN_CENTER_BOLD));
            } else {
                sheet.getRow(rowNumber).getCell(columnNumber).setCellStyle(styleContainer.getStyles().get(AssignmentToShiftXlsStyleContainer.WHITE_DATA_STYLE_BORDER_BOX_ALIGN_LEFT));
            }
        }

        for (int columnNumber = 1; columnNumber <= lastColumn; columnNumber += margin) {
            sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, columnNumber, columnNumber + 2));
        }
    }
}
