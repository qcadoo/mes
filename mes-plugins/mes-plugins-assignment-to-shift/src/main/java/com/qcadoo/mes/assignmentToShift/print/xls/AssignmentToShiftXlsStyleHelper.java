package com.qcadoo.mes.assignmentToShift.print.xls;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.springframework.stereotype.Service;

@Service
public class AssignmentToShiftXlsStyleHelper {

    public void setGreyDataStyleAlignLeftBold(final HSSFSheet sheet, final HSSFCell cell) {
        cell.setCellStyle(getHeaderStyle(sheet.getWorkbook(), HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE,
                HSSFCellStyle.ALIGN_LEFT, Font.BOLDWEIGHT_BOLD));
    }

    public void setGreyDataStyleAlignRightBold(final HSSFSheet sheet, final HSSFCell cell) {
        cell.setCellStyle(getHeaderStyle(sheet.getWorkbook(), HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE,
                HSSFCellStyle.ALIGN_RIGHT, Font.BOLDWEIGHT_BOLD));
    }

    public void setGreyDataStyleBorderLeftAlignLeftBold(final HSSFSheet sheet, final HSSFCell cell) {
        cell.setCellStyle(getHeaderStyle(sheet.getWorkbook(), HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE,
                HSSFCellStyle.ALIGN_LEFT, Font.BOLDWEIGHT_BOLD));
    }

    public void setGreyDataStyleBorderRightAlignLeftBold(final HSSFSheet sheet, final HSSFCell cell) {
        cell.setCellStyle(getHeaderStyle(sheet.getWorkbook(), HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM,
                HSSFCellStyle.ALIGN_LEFT, Font.BOLDWEIGHT_BOLD));
    }

    public void setWhiteDataStyleBorderBoxAlignLeft(final HSSFSheet sheet, final HSSFCell cell) {
        cell.setCellStyle(getSeriesStyle(sheet.getWorkbook(), HSSFCellStyle.BORDER_THICK, HSSFCellStyle.BORDER_THICK,
                HSSFCellStyle.ALIGN_LEFT, Font.BOLDWEIGHT_NORMAL));
    }

    public void setWhiteDataStyleBorderBoxAlignRight(final HSSFSheet sheet, final HSSFCell cell) {
        cell.setCellStyle(getSeriesStyle(sheet.getWorkbook(), HSSFCellStyle.BORDER_THICK, HSSFCellStyle.BORDER_THICK,
                HSSFCellStyle.ALIGN_RIGHT, Font.BOLDWEIGHT_NORMAL));
    }

    public void setWhiteDataStyleBorderBoxAlignLeftBold(final HSSFSheet sheet, final HSSFCell cell) {
        cell.setCellStyle(getSeriesStyle(sheet.getWorkbook(), HSSFCellStyle.BORDER_THICK, HSSFCellStyle.BORDER_THICK,
                HSSFCellStyle.ALIGN_LEFT, Font.BOLDWEIGHT_BOLD));
    }

    public void setWhiteDataStyleBorderBoxAlignRightBold(final HSSFSheet sheet, final HSSFCell cell) {
        cell.setCellStyle(getSeriesStyle(sheet.getWorkbook(), HSSFCellStyle.BORDER_THICK, HSSFCellStyle.BORDER_THICK,
                HSSFCellStyle.ALIGN_RIGHT, Font.BOLDWEIGHT_BOLD));
    }

    private HSSFCellStyle getHeaderStyle(final HSSFWorkbook workbook, final short borderLeft, final short borderRight,
            final short alignment, final short boldweight) {
        HSSFCellStyle style = workbook.createCellStyle();

        style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderLeft(borderLeft);
        style.setBorderRight(borderRight);
        style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);

        style.setAlignment(alignment);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setIndention((short) 3);
        style.setWrapText(true);

        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 12);
        font.setBoldweight(boldweight);

        style.setFont(font);

        return style;
    }

    private HSSFCellStyle getSeriesStyle(final HSSFWorkbook workbook, final short borderLeft, final short borderRight,
            final short alignment, final short boldweight) {
        HSSFCellStyle style = workbook.createCellStyle();

        style.setBorderTop(HSSFCellStyle.BORDER_THICK);
        style.setBorderLeft(borderLeft);
        style.setBorderRight(borderRight);
        style.setBorderBottom(HSSFCellStyle.BORDER_THICK);

        style.setAlignment(alignment);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);

        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 12);
        font.setBoldweight(boldweight);
        style.setFont(font);

        return style;
    }

}
