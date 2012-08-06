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
import org.apache.poi.ss.util.CellRangeAddress;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.assignmentToShift.constants.OccupationTypeEnumStringValue;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.xls.XlsDocumentService;
import com.qcadoo.report.api.xls.XlsHelper;

@Service
public class AssignmentToShiftXlsService extends XlsDocumentService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private AssignmentToShiftXlsHelper assignmentToShiftXlsHelper;

    @Autowired
    private AssignmentToShiftXlsStyleHelper assignmentToShiftXlsStyleHelper;

    @Autowired
    private XlsHelper xlsHelper;

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

        addMarginsAndStylesForHeader(sheet, entity);

    }

    private void addMarginsAndStylesForHeader(final HSSFSheet sheet, final Entity entity) {
        int numberOfDays = assignmentToShiftXlsHelper.getNumberOfDaysBetweenGivenDates(entity);

        int firstColumn = 0;
        int lastColumn;
        int margin = 3;

        if (numberOfDays < 3) {
            lastColumn = 10;
        } else {
            lastColumn = (numberOfDays + 1) * 3;
        }

        for (int column = firstColumn; column <= lastColumn; column++) {
            if (sheet.getRow(1).getCell(column) == null) {
                sheet.getRow(1).createCell(column);
            }

            if (column == firstColumn) {
                assignmentToShiftXlsStyleHelper.setGreyDataStyleBorderLeftAlignLeftBold(sheet, sheet.getRow(1).getCell(column));
            } else if (column == lastColumn) {
                assignmentToShiftXlsStyleHelper.setGreyDataStyleBorderRightAlignLeftBold(sheet, sheet.getRow(1).getCell(column));
            } else {
                assignmentToShiftXlsStyleHelper.setGreyDataStyleAlignLeftBold(sheet, sheet.getRow(1).getCell(column));
            }
        }

        sheet.addMergedRegion(new CellRangeAddress(1, 1, firstColumn, firstColumn + margin - 1));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, firstColumn + margin, firstColumn + (margin * 2) - 1));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, firstColumn + (margin * 2), lastColumn));
    }

    private void createHeaderForAssignmentToShift(final HSSFSheet sheet, final Locale locale, final Entity entity) {
        HSSFRow header = sheet.createRow(3);
        HSSFCell cell0 = header.createCell(0);
        cell0.setCellValue(translationService.translate(COLUMN_HEADER_OCCUPATIONTYPE, locale));
        xlsHelper.setCellStyle(sheet, cell0);
        List<DateTime> days = assignmentToShiftXlsHelper.getDaysBetweenGivenDates(entity);

        int columnNumber = 1;
        for (DateTime day : days) {
            HSSFCell cellDay = header.createCell(columnNumber);

            cellDay.setCellValue(translationService.translate(COLUMN_HEADER_DAY, locale,
                    DateFormat.getDateInstance().format(new Date(day.getMillis()))));
            xlsHelper.setCellStyle(sheet, cellDay);

            sheet.addMergedRegion(new CellRangeAddress(3, 3, columnNumber, columnNumber + 2));

            columnNumber += 3;
        }
    }

    @Override
    protected void addSeries(final HSSFSheet sheet, final Entity entity) {
        // TODO LUPO fix problem with report
        List<DateTime> days = assignmentToShiftXlsHelper.getDaysBetweenGivenDates(entity);
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
        for (Entity productionLine : assignmentToShiftXlsHelper.getProductionLines()) {
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
                Entity assignmentToShift = assignmentToShiftXlsHelper.getAssignmentToShift(
                        assignmentToShiftReport.getBelongsToField(SHIFT), day.toDate());
                if (assignmentToShift == null) {
                    continue;
                }
                List<Entity> staffs = assignmentToShiftXlsHelper.getStaffsList(assignmentToShift,
                        OccupationTypeEnumStringValue.WORK_ON_LINE, productionLine);
                row.createCell(columnNumber).setCellValue(assignmentToShiftXlsHelper.getListOfWorker(staffs));
                columnNumber++;
            }
        }
        return rowNum;
    }

    private void fillColumnWithStaffsWithOccupationTypeEnumOtherThanOnLine(final HSSFSheet sheet, final int rowNum,
            final Entity assignmentToShiftReport, final List<DateTime> days,
            final OccupationTypeEnumStringValue occupationTypeEnumStringValue) {
        // TODO LUPO fix problem with report
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
