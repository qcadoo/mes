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

import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import com.google.common.collect.Maps;

public class AssignmentToShiftXlsStyleContainer {

    private final Map<String, HSSFCellStyle> styles = Maps.newHashMap();

    public static final String GREY_DATA_STYLE_BORDER_TOP_ALIGN_LEFT_BOLD = "greyDataStyleBorderTopAlignLeftBold";
    public static final String GREY_DATA_STYLE_BORDER_BOTTOM_ALIGN_LEFT_BOLD = "greyDataStyleBorderBottomAlignLeftBold";
    public static final String GREY_DATA_STYLE_BORDER_TOP_LEFT_ALIGN_LEFT_BOLD = "greyDataStyleBorderTopLeftAlignLeftBold";
    public static final String GREY_DATA_STYLE_BORDER_TOP_RIGHT_ALIGN_LEFT_BOLD = "greyDataStyleBorderTopRightAlignLeftBold";
    public static final String GREY_DATA_STYLE_BORDER_LEFT_BOTTOM_ALIGN_LEFT_BOLD = "greyDataStyleBorderLeftBottomAlignLeftBold";
    public static final String GREY_DATA_STYLE_BORDER_RIGHT_BOTTOM_ALIGN_LEFT_BOLD = "greyDataStyleBorderRightBottomAlignLeftBold";
    public static final String WHITE_DATA_STYLE_BORDER_BOX_ALIGN_LEFT = "whiteDataStyleBorderBoxAlignLeft";
    public static final String WHITE_DATA_STYLE_BORDER_BOX_ALIGN_CENTER_BOLD = "whiteDataStyleBorderBoxAlignCenterBold";

    public AssignmentToShiftXlsStyleContainer(HSSFSheet sheet) {
        initStyle(sheet);
    }

    private void initStyle(final HSSFSheet sheet) {
        styles.put(GREY_DATA_STYLE_BORDER_TOP_ALIGN_LEFT_BOLD, createHeaderStyle(sheet.getWorkbook(), BorderStyle.MEDIUM, BorderStyle.NONE,
                BorderStyle.NONE, BorderStyle.NONE));
        styles.put(GREY_DATA_STYLE_BORDER_BOTTOM_ALIGN_LEFT_BOLD, createHeaderStyle(sheet.getWorkbook(), BorderStyle.NONE, BorderStyle.NONE,
                BorderStyle.NONE, BorderStyle.MEDIUM));
        styles.put(GREY_DATA_STYLE_BORDER_TOP_LEFT_ALIGN_LEFT_BOLD, createHeaderStyle(sheet.getWorkbook(), BorderStyle.MEDIUM, BorderStyle.MEDIUM,
                BorderStyle.NONE, BorderStyle.NONE));
        styles.put(GREY_DATA_STYLE_BORDER_TOP_RIGHT_ALIGN_LEFT_BOLD, createHeaderStyle(sheet.getWorkbook(), BorderStyle.MEDIUM, BorderStyle.NONE,
                BorderStyle.MEDIUM, BorderStyle.NONE));
        styles.put(GREY_DATA_STYLE_BORDER_LEFT_BOTTOM_ALIGN_LEFT_BOLD, createHeaderStyle(sheet.getWorkbook(), BorderStyle.NONE, BorderStyle.MEDIUM,
                BorderStyle.NONE, BorderStyle.MEDIUM));
        styles.put(GREY_DATA_STYLE_BORDER_RIGHT_BOTTOM_ALIGN_LEFT_BOLD, createHeaderStyle(sheet.getWorkbook(), BorderStyle.NONE, BorderStyle.NONE,
                BorderStyle.MEDIUM, BorderStyle.MEDIUM));
        styles.put(WHITE_DATA_STYLE_BORDER_BOX_ALIGN_LEFT, createSeriesStyle(sheet.getWorkbook(), HorizontalAlignment.LEFT, false));
        styles.put(WHITE_DATA_STYLE_BORDER_BOX_ALIGN_CENTER_BOLD, createSeriesStyle(sheet.getWorkbook(), HorizontalAlignment.CENTER, true));
    }


    private HSSFCellStyle createHeaderStyle(final HSSFWorkbook workbook, final BorderStyle borderTop, final BorderStyle borderLeft,
                                            final BorderStyle borderRight, final BorderStyle borderBottom) {
        HSSFCellStyle style = workbook.createCellStyle();

        style.setBorderTop(borderTop);
        style.setBorderLeft(borderLeft);
        style.setBorderRight(borderRight);
        style.setBorderBottom(borderBottom);

        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setIndention((short) 3);
        style.setWrapText(true);

        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);

        style.setFont(font);

        return style;
    }

    private HSSFCellStyle createSeriesStyle(final HSSFWorkbook workbook, final HorizontalAlignment horizontalAlignment, final boolean bold) {
        HSSFCellStyle style = workbook.createCellStyle();

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);

        style.setAlignment(horizontalAlignment);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 11);
        font.setBold(bold);
        style.setFont(font);

        return style;
    }

    public Map<String, HSSFCellStyle> getStyles() {
        return styles;
    }
}
