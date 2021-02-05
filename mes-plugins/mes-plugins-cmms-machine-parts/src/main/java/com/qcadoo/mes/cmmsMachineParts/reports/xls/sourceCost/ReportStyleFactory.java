package com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

public class ReportStyleFactory {

    private boolean isFirst = false;

    private HorizontalAlignment alignment = HorizontalAlignment.LEFT;

    private static final HorizontalAlignment LEFT = HorizontalAlignment.LEFT;

    private static final HorizontalAlignment RIGHT = HorizontalAlignment.RIGHT;

    private HSSFCellStyle firstLeftWhite;

    private HSSFCellStyle firstRightWhite;

    private HSSFCellStyle leftWhite;

    private HSSFCellStyle rightWhite;

    private DataFormat dataFormat;

    public ReportStyleFactory(final HSSFWorkbook workbook) {
        init(workbook);
    }

    private void init(HSSFWorkbook workbook) {
        dataFormat = workbook.createDataFormat();
        firstLeftWhite = createStyle(workbook, true, LEFT);
        firstRightWhite = createStyle(workbook, true, RIGHT);
        leftWhite = createStyle(workbook, false, LEFT);
        rightWhite = createStyle(workbook, false, RIGHT);
    }

    private HSSFCellStyle createStyle(final HSSFWorkbook workbook, boolean isFirst, HorizontalAlignment horizontalAlignment) {
        HSSFCellStyle style = workbook.createCellStyle();
        if (isFirst) {
            style.setBorderTop(BorderStyle.THIN);
        }
        if (horizontalAlignment == RIGHT) {
            style.setAlignment(RIGHT);
            style.setDataFormat(dataFormat.getFormat("[HH]:MM:SS"));
        }
        return style;
    }

    public HSSFCellStyle getStyle() {
        if (isFirst) {
            if (alignment == LEFT) {
                return firstLeftWhite;
            } else {
                return firstRightWhite;
            }
        } else {
            if (alignment == LEFT) {
                return leftWhite;
            } else {
                return rightWhite;
            }
        }
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

}
