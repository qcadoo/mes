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

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields;
import com.qcadoo.mes.assignmentToShift.constants.OccupationType;
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

@Service
public class AssignmentToShiftXlsService extends XlsDocumentService {

    public static final String L_OCCUPATION_TYPE = "occupationType";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private AssignmentToShiftXlsHelper assignmentToShiftXlsHelper;

    @Autowired
    private AssignmentToShiftXlsStyleHelper assignmentToShiftXlsStyleHelper;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate(AssignmentToShiftReportConstants.TITLE, locale);
    }

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale, final Entity assignmentToShiftReport) {
        createHeaderForAuthor(sheet, locale, assignmentToShiftReport);
        createHeaderForAssignmentToShift(sheet, locale, assignmentToShiftReport);
    }

    private void createHeaderForAuthor(final HSSFSheet sheet, final Locale locale, final Entity assignmentToShiftReport) {
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

        assignmentToShiftXlsStyleHelper.addMarginsAndStylesForAuthor(sheet, 1,
                assignmentToShiftXlsHelper.getNumberOfDaysBetweenGivenDates(assignmentToShiftReport));
        assignmentToShiftXlsStyleHelper.addMarginsAndStylesForAuthorFactory(sheet, 2,
                assignmentToShiftXlsHelper.getNumberOfDaysBetweenGivenDates(assignmentToShiftReport));
    }

    private void createHeaderForAssignmentToShift(final HSSFSheet sheet, final Locale locale,
            final Entity assignmentToShiftReport) {
        List<DateTime> days = assignmentToShiftXlsHelper.getDaysBetweenGivenDates(assignmentToShiftReport);

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

            assignmentToShiftXlsStyleHelper.addMarginsAndStylesForAssignmentToShift(sheet, 4,
                    assignmentToShiftXlsHelper.getNumberOfDaysBetweenGivenDates(assignmentToShiftReport));
        }
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity assignmentToShiftReport) {
        List<DateTime> days = assignmentToShiftXlsHelper.getDaysBetweenGivenDates(assignmentToShiftReport);

        if (days != null) {
            int rowNum = 5;

            List<Entity> occupationTypesWithoutTechnicalCode = getOccupationTypeDictionaryWithoutTechnicalCode();
            List<Entity> productionlines = assignmentToShiftXlsHelper.getProductionLines();

            if (!productionlines.isEmpty()) {
                rowNum = fillColumnWithStaffForWorkOnLine(sheet, rowNum, assignmentToShiftReport, days, productionlines,
                        getDictionaryItemWithProductionOnLine());
            }

            for (Entity dictionaryItem : occupationTypesWithoutTechnicalCode) {
                rowNum = fillColumnWithStaffForOtherTypes(sheet, rowNum, assignmentToShiftReport, days, dictionaryItem);
            }

            fillColumnWithStaffForOtherTypes(sheet, rowNum, assignmentToShiftReport, days, getDictionaryItemWithOtherCase());

            sheet.autoSizeColumn(0);
        }
    }

    private int fillColumnWithStaffForWorkOnLine(final HSSFSheet sheet, int rowNum, final Entity assignmentToShiftReport,
            final List<DateTime> days, final List<Entity> productionLines, final Entity dictionaryItem) {
        if ((assignmentToShiftReport != null) && (days != null) && (productionLines != null)) {
            for (Entity productionLine : productionLines) {
                int rowNumFromLastSection = rowNum;
                int numberOfColumnsForWorkers = getNumberOfRowsForWorkers(assignmentToShiftReport, days, productionLine,
                        dictionaryItem);

                for (int i = 0; i < numberOfColumnsForWorkers; i++) {
                    HSSFRow row = sheet.createRow(rowNum);
                    rowNum++;
                }

                String productionLineValue = null;

                if (productionLine.getStringField(ProductionLineFields.PLACE) == null) {
                    productionLineValue = productionLine.getStringField(ProductionLineFields.NUMBER);
                } else {
                    productionLineValue = productionLine.getStringField(ProductionLineFields.NUMBER) + "-"
                            + productionLine.getStringField(ProductionLineFields.PLACE);
                }

                HSSFRow firstRowInSection = null;

                if (sheet.getRow(rowNumFromLastSection) == null) {
                    firstRowInSection = sheet.createRow(rowNumFromLastSection);

                    rowNum++;
                } else {
                    firstRowInSection = sheet.getRow(rowNumFromLastSection);

                }

                HSSFCell cell = firstRowInSection.createCell(0);
                cell.setCellValue(productionLineValue);
                sheet.addMergedRegion(new CellRangeAddress(rowNumFromLastSection, rowNum - 1, 0, 0));

                int columnNumber = 1;
                int maxLength = 0;

                for (DateTime day : days) {
                    List<Entity> assignmentsToShift = assignmentToShiftXlsHelper.getAssignmentToShift(
                            assignmentToShiftReport.getBelongsToField(AssignmentToShiftReportFields.SHIFT),
                            assignmentToShiftReport.getBelongsToField(AssignmentToShiftReportFields.FACTORY), day.toDate());

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
                    assignmentToShiftXlsStyleHelper.addMarginsAndStylesForSeries(sheet, i,
                            assignmentToShiftXlsHelper.getNumberOfDaysBetweenGivenDates(assignmentToShiftReport));

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
                    assignmentToShiftReport.getBelongsToField(AssignmentToShiftReportFields.FACTORY), day.toDate());

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
                    assignmentToShiftReport.getBelongsToField(AssignmentToShiftReportFields.FACTORY), day.toDate());

            List<Entity> staffs = Lists.newArrayList();
            for (Entity assignmentToShift : assignmentsToShift) {
                staffs.addAll(assignmentToShiftXlsHelper.getStaffsList(assignmentToShift,
                        dictionaryItem.getStringField(DictionaryItemFields.NAME), null));
            }

            List<String> workers = Lists.newArrayList();

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
            final List<DateTime> days, final Entity dictionaryItem) {
        if ((assignmentToShiftReport != null) && (days != null) && (dictionaryItem != null)) {
            int rowNumFromLastSection = rowNum;

            int numberOfColumnsForWorkers = getNumberOfRowsForWorkersForOtherTypes(assignmentToShiftReport, days, dictionaryItem);

            for (int i = 0; i < numberOfColumnsForWorkers; i++) {
                HSSFRow row = sheet.createRow(rowNum);
                rowNum++;
            }

            String occupationTypeValue = dictionaryItem.getStringField(DictionaryItemFields.NAME);

            HSSFRow firstRowInSection = null;

            if (sheet.getRow(rowNumFromLastSection) == null) {
                firstRowInSection = sheet.createRow(rowNumFromLastSection);
                rowNum++;
            } else {
                firstRowInSection = sheet.getRow(rowNumFromLastSection);

            }

            HSSFCell cell = firstRowInSection.createCell(0);
            cell.setCellValue(occupationTypeValue);
            sheet.addMergedRegion(new CellRangeAddress(rowNumFromLastSection, rowNum - 1, 0, 0));

            int columnNumber = 1;

            for (DateTime day : days) {
                List<Entity> assignmentsToShift = assignmentToShiftXlsHelper.getAssignmentToShift(
                        assignmentToShiftReport.getBelongsToField(AssignmentToShiftReportFields.SHIFT),
                        assignmentToShiftReport.getBelongsToField(AssignmentToShiftReportFields.FACTORY), day.toDate());

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

                List<String> workers = Lists.newArrayList();

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
                assignmentToShiftXlsStyleHelper.addMarginsAndStylesForSeries(sheet, i,
                        assignmentToShiftXlsHelper.getNumberOfDaysBetweenGivenDates(assignmentToShiftReport));
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

}
