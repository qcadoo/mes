package com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

public class ReportStyleFactory {

    private boolean isFirst = false;

    private HorizontalAlignment alignment = HorizontalAlignment.LEFT;

    private short color = IndexedColors.WHITE.getIndex();

    private static final short WHITE = IndexedColors.WHITE.getIndex();

    private static final short RED = IndexedColors.RED.getIndex();

    private static final short GREEN = IndexedColors.LIME.getIndex();

    private HSSFCellStyle firstLeftWhite;

    private HSSFCellStyle firstLeftRed;

    private HSSFCellStyle firstLeftGreen;

    private HSSFCellStyle firstRightWhite;

    private HSSFCellStyle firstRightRed;

    private HSSFCellStyle firstRightGreen;

    private HSSFCellStyle leftWhite;

    private HSSFCellStyle leftRed;

    private HSSFCellStyle leftGreen;

    private HSSFCellStyle rightWhite;

    private HSSFCellStyle rightRed;

    private HSSFCellStyle rightGreen;

    public ReportStyleFactory(final HSSFWorkbook workbook) {
        init(workbook);
    }

    private void init(HSSFWorkbook workbook) {
        firstLeftWhite = createStyle(workbook, true, HorizontalAlignment.LEFT, WHITE);
        firstLeftRed = createStyle(workbook, true, HorizontalAlignment.LEFT, RED);
        firstLeftGreen = createStyle(workbook, true, HorizontalAlignment.LEFT, GREEN);
        firstRightWhite = createStyle(workbook, true, HorizontalAlignment.RIGHT, WHITE);
        firstRightRed = createStyle(workbook, true, HorizontalAlignment.RIGHT, RED);
        firstRightGreen = createStyle(workbook, true, HorizontalAlignment.RIGHT, GREEN);
        leftWhite = createStyle(workbook, false, HorizontalAlignment.LEFT, WHITE);
        leftRed = createStyle(workbook, false, HorizontalAlignment.LEFT, RED);
        leftGreen = createStyle(workbook, false, HorizontalAlignment.LEFT, GREEN);
        rightWhite = createStyle(workbook, false, HorizontalAlignment.RIGHT, WHITE);
        rightRed = createStyle(workbook, false, HorizontalAlignment.RIGHT, RED);
        rightGreen = createStyle(workbook, false, HorizontalAlignment.RIGHT, GREEN);
    }

    private HSSFCellStyle createStyle(final HSSFWorkbook workbook, boolean isFirst, HorizontalAlignment horizontalAlignment, short color) {
        HSSFCellStyle style = workbook.createCellStyle();
        if (isFirst) {
            style.setBorderTop(BorderStyle.THIN);
        }
        if (color != WHITE) {
            style.setFillForegroundColor(color);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        if (horizontalAlignment == HorizontalAlignment.RIGHT) {
            style.setAlignment(HorizontalAlignment.RIGHT);
        }
        return style;
    }

    public HSSFCellStyle getStyle() {
        if (isFirst) {
            if (alignment == HorizontalAlignment.LEFT) {
                if (color == WHITE) {
                    return firstLeftWhite;
                }
                if (color == RED) {
                    return firstLeftRed;
                }
                if (color == GREEN) {
                    return firstLeftGreen;
                }
            } else {
                if (color == WHITE) {
                    return firstRightWhite;
                }
                if (color == RED) {
                    return firstRightRed;
                }
                if (color == GREEN) {
                    return firstRightGreen;
                }
            }
        } else if (alignment == HorizontalAlignment.LEFT) {
            if (color == WHITE) {
                return leftWhite;
            }
            if (color == RED) {
                return leftRed;
            }
            if (color == GREEN) {
                return leftGreen;
            }
        } else {
            if (color == WHITE) {
                return rightWhite;
            }
            if (color == RED) {
                return rightRed;
            }
            if (color == GREEN) {
                return rightGreen;
            }
        }
        return leftWhite;
    }

    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    public void setLeftAligned() {
        alignment = HorizontalAlignment.LEFT;
    }

    public void setRightAligned() {
        alignment = HorizontalAlignment.RIGHT;
    }

    public void setWhite() {
        color = WHITE;
    }

    public void setRed() {
        color = RED;
    }

    public void setGreen() {
        color = GREEN;
    }
}
