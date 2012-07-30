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
import com.qcadoo.mes.assignmentToShift.AssignmentToShiftReportHelper;
import com.qcadoo.mes.assignmentToShift.constants.OccupationTypeEnumStringValue;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.xls.XlsDocumentService;
import com.qcadoo.report.api.xls.XlsHelper;

@Service
public class AssignmentToShiftReportXlsService extends XlsDocumentService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private AssignmentToShiftReportHelper assignmentToShiftReportHelper;

    @Autowired
    private XlsHelper xlsHelper;

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate(TITLE, locale);
    }

    @Override
    protected void addHeader(final HSSFSheet sheet, final Locale locale, final Entity entity) {
        createHeaderForAuthor(sheet, locale);
        createHeaderForAssignmentToShift(sheet, locale, entity);
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity entity) {
        addSeriesOfReportAuthorAndDate(sheet, entity);
        addSeriesAssignmentToShift(sheet, entity);
    }

    private void addSeriesOfReportAuthorAndDate(final HSSFSheet sheet, final Entity entity) {
        HSSFRow row = sheet.createRow(1);
        row.createCell(0).setCellValue(entity.getStringField(CREATE_USER));
        row.createCell(1).setCellValue(entity.getField(UPDATE_DATE).toString());

    }

    private void createHeaderForAuthor(final HSSFSheet sheet, final Locale locale) {
        HSSFRow headerAuthorLine = sheet.createRow(0);
        HSSFCell cellAuthorLine0 = headerAuthorLine.createCell(0);
        cellAuthorLine0.setCellValue(translationService.translate(COLUMN_HEADER_SHIFT, locale));
        HSSFCell cellAuthorLine1 = headerAuthorLine.createCell(1);
        cellAuthorLine1.setCellValue(translationService.translate(COLUMN_HEADER_AUTHOR, locale));
        HSSFCell cellAuthorLine2 = headerAuthorLine.createCell(2);
        cellAuthorLine2.setCellValue(translationService.translate(COLUMN_HEADER_UPDATE_DATE, locale));
    }

    private void createHeaderForAssignmentToShift(final HSSFSheet sheet, final Locale locale, final Entity entity) {
        HSSFRow header = sheet.createRow(3);
        HSSFCell cell0 = header.createCell(0);
        cell0.setCellValue(translationService.translate(COLUMN_HEADER_OCCUPATIONTYPE, locale));
        xlsHelper.setCellStyle(sheet, cell0);
        List<DateTime> days = assignmentToShiftReportHelper.getDaysFromGivenDate(entity);

        int columnNumber = 1;
        for (DateTime day : days) {
            HSSFCell cellDay = header.createCell(columnNumber);
            columnNumber++;
            cellDay.setCellValue(translationService.translate(COLUMN_HEADER_DAY, locale,
                    DateFormat.getDateInstance().format(new Date(day.getMillis()))));
            xlsHelper.setCellStyle(sheet, cellDay);
        }
    }

    private void addSeriesAssignmentToShift(final HSSFSheet sheet, final Entity entity) {
        List<DateTime> days = assignmentToShiftReportHelper.getDaysFromGivenDate(entity);
        int rowNum = 4;
        for (OccupationTypeEnumStringValue occupationTypeEnumStringValue : OccupationTypeEnumStringValue.values()) {
            if (occupationTypeEnumStringValue.getStringValue()
                    .equals(OccupationTypeEnumStringValue.WORK_ON_LINE.getStringValue())) {
                rowNum = addRowForProductioLines(sheet, rowNum, entity, days);
            } else {
                fillColumnWithStaffsWithOccupationTypeEnumOtherThanOnLine(sheet, rowNum, entity, days,
                        occupationTypeEnumStringValue);
                rowNum++;
            }
        }
    }

    private int addRowForProductioLines(final HSSFSheet sheet, int rowNum, final Entity assignmentToShiftReport,
            final List<DateTime> days) {
        for (Entity productionLine : assignmentToShiftReportHelper.getProductionLines()) {
            HSSFRow row = sheet.createRow(rowNum++);
            String cellValue = null;
            if (productionLine.getStringField(PLACE) == null) {
                cellValue = productionLine.getStringField(NUMBER);
            } else {
                cellValue = productionLine.getStringField(NUMBER) + "-"
                        + productionLine.getStringField(ProductionLineFields.PLACE);
            }
            row.createCell(0).setCellValue(cellValue);
            int columnNumber = 1;
            for (DateTime day : days) {
                Entity assignmentToShift = assignmentToShiftReportHelper.getAssignmentToShift(
                        assignmentToShiftReport.getBelongsToField(SHIFT), day.toDate());
                if (assignmentToShift == null) {
                    continue;
                }
                List<Entity> staffs = assignmentToShiftReportHelper.getStaffsList(assignmentToShift,
                        OccupationTypeEnumStringValue.WORK_ON_LINE, productionLine);
                row.createCell(columnNumber).setCellValue(assignmentToShiftReportHelper.getListOfWorker(staffs));
                columnNumber++;
            }
        }
        return rowNum;
    }

    private void fillColumnWithStaffsWithOccupationTypeEnumOtherThanOnLine(final HSSFSheet sheet, final int rowNum,
            final Entity assignmentToShiftReport, final List<DateTime> days,
            final OccupationTypeEnumStringValue occupationTypeEnumStringValue) {
        // TODO ALBR
        // HSSFRow row = sheet.createRow(rowNum++);
        // row.createCell(0).setCellValue(occupationTypeEnumStringValue.getStringValue());
        // int columnNumber = 1;
        // for (DateTime day : days) {
        // Entity assignmentToShift = assignmentToShiftReportHelper.getAssignmentToShift(
        // assignmentToShiftReport.getBelongsToField(SHIFT), day.toDate());
        // if (assignmentToShift == null) {
        // continue;
        // }
        // List<Entity> staffs = assignmentToShiftReportHelper.getStaffsList(assignmentToShift, occupationTypeEnumStringValue,
        // null);
        // row.createCell(0).setCellValue(occupationTypeEnumStringValue.getStringValue());
        // row.createCell(1).setCellValue(assignmentToShiftReportHelper.getListOfWorker(staffs));
        // }
    }
}
