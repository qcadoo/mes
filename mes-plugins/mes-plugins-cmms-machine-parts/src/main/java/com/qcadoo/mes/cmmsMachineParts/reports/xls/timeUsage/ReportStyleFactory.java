package com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;

public class ReportStyleFactory {

    boolean isFirst = false;

    short alignment = HSSFCellStyle.ALIGN_LEFT;

    short color = IndexedColors.WHITE.getIndex();

    private static final short WHITE = IndexedColors.WHITE.getIndex();

    private static final short RED = IndexedColors.RED.getIndex();

    private static final short GREEN = IndexedColors.LIME.getIndex();

    private static final short LEFT = HSSFCellStyle.ALIGN_LEFT;

    private static final short RIGHT = HSSFCellStyle.ALIGN_RIGHT;

    HSSFCellStyle firstLeftWhite;

    HSSFCellStyle firstLeftRed;

    HSSFCellStyle firstLeftGreen;

    HSSFCellStyle firstRightWhite;

    HSSFCellStyle firstRightRed;

    HSSFCellStyle firstRightGreen;

    HSSFCellStyle leftWhite;

    HSSFCellStyle leftRed;

    HSSFCellStyle leftGreen;

    HSSFCellStyle rightWhite;

    HSSFCellStyle rightRed;

    HSSFCellStyle rightGreen;

    public ReportStyleFactory(final HSSFWorkbook workbook) {
        init(workbook);
    }

    private void init(HSSFWorkbook workbook) {
        firstLeftWhite = createStyle(workbook, true, LEFT, WHITE);
        firstLeftRed = createStyle(workbook, true, LEFT, RED);
        firstLeftGreen = createStyle(workbook, true, LEFT, GREEN);
        firstRightWhite = createStyle(workbook, true, RIGHT, WHITE);
        firstRightRed = createStyle(workbook, true, RIGHT, RED);
        firstRightGreen = createStyle(workbook, true, RIGHT, GREEN);
        leftWhite = createStyle(workbook, false, LEFT, WHITE);
        leftRed = createStyle(workbook, false, LEFT, RED);
        leftGreen = createStyle(workbook, false, LEFT, GREEN);
        rightWhite = createStyle(workbook, false, RIGHT, WHITE);
        rightRed = createStyle(workbook, false, RIGHT, RED);
        rightGreen = createStyle(workbook, false, RIGHT, GREEN);
    }

    private HSSFCellStyle createStyle(final HSSFWorkbook workbook, boolean isFirst, short align, short color) {
        HSSFCellStyle style = workbook.createCellStyle();
        if (isFirst) {
            style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        }
        if (color != WHITE) {
            style.setFillForegroundColor(color);
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        }
        if (align == RIGHT) {
            style.setAlignment(RIGHT);
        }
        return style;
    }

    public HSSFCellStyle getStyle() {
        if (isFirst) {
            if (alignment == LEFT) {
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
        } else {
            if (alignment == LEFT) {
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
        }
        return leftWhite;
    }

    public void setFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    public void setLeftAligned() {
        alignment = LEFT;
    }

    public void setRightAligned() {
        alignment = RIGHT;
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
