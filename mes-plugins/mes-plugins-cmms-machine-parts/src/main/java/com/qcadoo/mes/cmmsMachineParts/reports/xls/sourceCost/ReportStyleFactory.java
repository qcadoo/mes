package com.qcadoo.mes.cmmsMachineParts.reports.xls.sourceCost;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormat;

public class ReportStyleFactory {

    private boolean isFirst = false;

    private short alignment = HSSFCellStyle.ALIGN_LEFT;

    private static final short LEFT = HSSFCellStyle.ALIGN_LEFT;

    private static final short RIGHT = HSSFCellStyle.ALIGN_RIGHT;

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

    private HSSFCellStyle createStyle(final HSSFWorkbook workbook, boolean isFirst, short align) {
        HSSFCellStyle style = workbook.createCellStyle();
        if (isFirst) {
            style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        }
        if (align == RIGHT) {
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
