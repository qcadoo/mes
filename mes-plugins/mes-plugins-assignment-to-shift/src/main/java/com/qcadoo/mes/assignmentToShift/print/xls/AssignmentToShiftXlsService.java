/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.SHIFT;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportConstants.COLUMN_HEADER_AUTHOR;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportConstants.COLUMN_HEADER_DAY;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportConstants.COLUMN_HEADER_OCCUPATIONTYPE;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportConstants.COLUMN_HEADER_SHIFT;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportConstants.COLUMN_HEADER_UPDATE_DATE;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportConstants.TITLE;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.CREATE_USER;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields.UPDATE_DATE;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.NUMBER;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.PLACE;
import static com.qcadoo.model.constants.DictionaryItemFields.NAME;
import static com.qcadoo.model.constants.DictionaryItemFields.TECHNICAL_CODE;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.assignmentToShift.constants.OccupationType;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.QcadooModelConstants;
import com.qcadoo.report.api.xls.XlsDocumentService;

@Service
public class AssignmentToShiftXlsService extends XlsDocumentService {

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
        return translationService.translate(TITLE, locale);
    }

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale, final Entity entity) {
        createHeaderForAuthor(sheet, locale, entity);
        createHeaderForAssignmentToShift(sheet, locale, entity);
    }

    private void createHeaderForAuthor(final HSSFSheet sheet, final Locale locale, final Entity entity) {
        HSSFRow headerAuthorLine = sheet.createRow(1);

        String shift = translationService.translate(COLUMN_HEADER_SHIFT, locale) + " "
                + entity.getBelongsToField(SHIFT).getStringField("name");
        String user = translationService.translate(COLUMN_HEADER_AUTHOR, locale) + " " + entity.getField(CREATE_USER).toString();
        String date = translationService.translate(COLUMN_HEADER_UPDATE_DATE, locale) + " "
                + DateFormat.getDateInstance().format(entity.getField(UPDATE_DATE));

        HSSFCell cellAuthorLine0 = headerAuthorLine.createCell(0);
        cellAuthorLine0.setCellValue(shift);
        HSSFCell cellAuthorLine3 = headerAuthorLine.createCell(3);
        cellAuthorLine3.setCellValue(user);
        HSSFCell cellAuthorLine6 = headerAuthorLine.createCell(6);
        cellAuthorLine6.setCellValue(date);

        headerAuthorLine.setHeightInPoints(15);

        assignmentToShiftXlsStyleHelper.addMarginsAndStylesForAuthor(sheet, 1,
                assignmentToShiftXlsHelper.getNumberOfDaysBetweenGivenDates(entity));

    }

    private void createHeaderForAssignmentToShift(final HSSFSheet sheet, final Locale locale, final Entity entity) {
        List<DateTime> days = assignmentToShiftXlsHelper.getDaysBetweenGivenDates(entity);

        if (days != null) {
            HSSFRow headerAssignmentToShift = sheet.createRow(3);

            String occupationType = translationService.translate(COLUMN_HEADER_OCCUPATIONTYPE, locale);

            HSSFCell cell0 = headerAssignmentToShift.createCell(0);
            cell0.setCellValue(occupationType);

            int columnNumber = 1;
            for (DateTime day : days) {
                HSSFCell cellDay = headerAssignmentToShift.createCell(columnNumber);

                cellDay.setCellValue(translationService.translate(COLUMN_HEADER_DAY, locale,
                        DateFormat.getDateInstance().format(new Date(day.getMillis()))));

                columnNumber += 3;
            }

            headerAssignmentToShift.setHeightInPoints(14);

            assignmentToShiftXlsStyleHelper.addMarginsAndStylesForAssignmentToShift(sheet, 3,
                    assignmentToShiftXlsHelper.getNumberOfDaysBetweenGivenDates(entity));
        }
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity entity) {
        List<DateTime> days = assignmentToShiftXlsHelper.getDaysBetweenGivenDates(entity);

        if (days != null) {
            int rowNum = 4;
            List<Entity> occupationTypesWithoutTechnicalCode = getOccupationTypeDictionaryWithoutTechnicalCode();
            List<Entity> productionlines = assignmentToShiftXlsHelper.getProductionLines();

            if (productionlines != null) {
                fillColumnWithStaffForWorkOnLine(sheet, rowNum, entity, days, productionlines,
                        getDictionaryItemWithProductionOnLine());
                rowNum += productionlines.size();
            }
            for (Entity dictionaryItem : occupationTypesWithoutTechnicalCode) {
                fillColumnWithStaffForOtherTypes(sheet, rowNum, entity, days, dictionaryItem);
                rowNum++;
            }
            fillColumnWithStaffForOtherTypes(sheet, rowNum, entity, days, getDictionaryItemWithOtherCase());
            sheet.autoSizeColumn(0);
        }

    }

    private void fillColumnWithStaffForWorkOnLine(final HSSFSheet sheet, int rowNum, final Entity assignmentToShiftReport,
            final List<DateTime> days, final List<Entity> productionLines, final Entity dictionaryItem) {
        if ((assignmentToShiftReport != null) && (days != null) && (productionLines != null)) {
            for (Entity productionLine : productionLines) {
                HSSFRow row = sheet.createRow(rowNum);
                String productionLineValue = null;
                if (productionLine.getStringField(PLACE) == null) {
                    productionLineValue = productionLine.getStringField(NUMBER);
                } else {
                    productionLineValue = productionLine.getStringField(NUMBER) + "-"
                            + productionLine.getStringField(ProductionLineFields.PLACE);
                }
                row.createCell(0).setCellValue(productionLineValue);

                int columnNumber = 1;
                for (DateTime day : days) {
                    Entity assignmentToShift = assignmentToShiftXlsHelper.getAssignmentToShift(
                            assignmentToShiftReport.getBelongsToField(SHIFT), day.toDate());
                    if (assignmentToShift == null) {
                        columnNumber += 3;
                        continue;
                    }

                    List<Entity> staffs = assignmentToShiftXlsHelper.getStaffsList(assignmentToShift,
                            dictionaryItem.getStringField(NAME), productionLine);

                    String staffsValue = assignmentToShiftXlsHelper.getListOfWorkers(staffs);

                    row.createCell(columnNumber).setCellValue(staffsValue);
                    row.setHeightInPoints(assignmentToShiftXlsStyleHelper.getHeightForRow(staffsValue.length(), 22, 14));

                    columnNumber += 3;
                }

                assignmentToShiftXlsStyleHelper.addMarginsAndStylesForSeries(sheet, rowNum,
                        assignmentToShiftXlsHelper.getNumberOfDaysBetweenGivenDates(assignmentToShiftReport));

                rowNum++;
            }
        }
    }

    private void fillColumnWithStaffForOtherTypes(final HSSFSheet sheet, final int rowNum, final Entity assignmentToShiftReport,
            final List<DateTime> days, final Entity occupationType) {
        if ((assignmentToShiftReport != null) && (days != null) && (occupationType != null)) {
            HSSFRow row = sheet.createRow(rowNum);
            String occupationTypeValue = occupationType.getStringField(NAME);

            row.createCell(0).setCellValue(occupationTypeValue);

            int columnNumber = 1;
            for (DateTime day : days) {
                Entity assignmentToShift = assignmentToShiftXlsHelper.getAssignmentToShift(
                        assignmentToShiftReport.getBelongsToField(SHIFT), day.toDate());
                if (assignmentToShift == null) {
                    continue;
                }
                List<Entity> staffs = assignmentToShiftXlsHelper.getStaffsList(assignmentToShift,
                        occupationType.getStringField(NAME), null);

                String staffsValue = null;
                if (OccupationType.OTHER_CASE.getStringValue().equals(occupationType.getStringField(TECHNICAL_CODE))) {
                    staffsValue = assignmentToShiftXlsHelper.getListOfWorkersWithOtherCases(staffs);
                } else {
                    staffsValue = assignmentToShiftXlsHelper.getListOfWorkers(staffs);
                }

                row.createCell(columnNumber).setCellValue(staffsValue);
                row.setHeightInPoints(assignmentToShiftXlsStyleHelper.getHeightForRow(staffsValue.length(), 22, 14));

                columnNumber += 3;
            }

            assignmentToShiftXlsStyleHelper.addMarginsAndStylesForSeries(sheet, rowNum,
                    assignmentToShiftXlsHelper.getNumberOfDaysBetweenGivenDates(assignmentToShiftReport));
        }
    }

    private List<Entity> getOccupationTypeDictionaryWithoutTechnicalCode() {
        Entity occupationTypeDictionary = dataDefinitionService
                .get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY).find()
                .add(SearchRestrictions.eq("name", "occupationType")).uniqueResult();
        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM)
                .find().add(SearchRestrictions.belongsTo("dictionary", occupationTypeDictionary))
                .add(SearchRestrictions.isNull("technicalCode")).list().getEntities();
    }

    private Entity getDictionaryItemWithProductionOnLine() {
        Entity occupationTypeDictionary = dataDefinitionService
                .get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY).find()
                .add(SearchRestrictions.eq("name", "occupationType")).uniqueResult();
        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM)
                .find().add(SearchRestrictions.belongsTo("dictionary", occupationTypeDictionary))
                .add(SearchRestrictions.eq("technicalCode", OccupationType.WORK_ON_LINE.getStringValue())).uniqueResult();
    }

    private Entity getDictionaryItemWithOtherCase() {
        Entity occupationTypeDictionary = dataDefinitionService
                .get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY).find()
                .add(SearchRestrictions.eq("name", "occupationType")).uniqueResult();
        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM)
                .find().add(SearchRestrictions.belongsTo("dictionary", occupationTypeDictionary))
                .add(SearchRestrictions.eq("technicalCode", OccupationType.OTHER_CASE.getStringValue())).uniqueResult();
    }
}
