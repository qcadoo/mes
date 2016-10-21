/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.productionPerShift.report.print;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.springframework.stereotype.Service;

@Service
public class PPSReportXlsStyleHelper {

    public void setWhiteDataStyleSmall(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_WhiteDataStyleSmall));
    }

    public void setGreyDataStyleSmall(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_GreyDataStyleSmall));
    }

    public void setWhiteDataStyleEnd(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_WhiteDataStyleEnd));
    }

    public void setGreyDataStyleEnd(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_GreyDataStyleEnd));
    }

    public void setWhiteDataStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_WhiteDataStyle));
    }

    public void setGreyDataStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_GreyDataStyle));
    }

    public void setChangeoverDataStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_ChangeoverDataStyle));
    }

    public void setWhiteDataStyleRed(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_WhiteDataStyleRed));
    }

    public void setGreyDataStyleRed(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_GreyDataStyleRed));
    }

    public void setHeaderStyle1(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_HeaderStyle1));
    }

    public void setHeaderStyle2(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_HeaderStyle2));
    }

    public void setHeaderStyle2Red(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_HeaderStyle2Red));
    }

    private HSSFCellStyle getHeaderStyle0(final HSSFWorkbook workbook, final boolean shouldRed, final boolean shouldBackground,
            final boolean shouldleft, final Font font) {
        HSSFCellStyle style = workbook.createCellStyle();

        style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);

        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setFontHeightInPoints((short) 7);
        font.setColor(HSSFFont.COLOR_NORMAL);

        style.setFont(font);

        if (shouldRed) {
            font.setColor(HSSFFont.COLOR_RED);
            style.setFont(font);
        }
        if (shouldBackground) {
            style.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        }
        if (shouldleft) {
            style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        }

        return style;
    }

    private HSSFCellStyle getHeaderStyle2(final HSSFWorkbook workbook, final boolean shouldRed, final boolean shouldBackground,
            final boolean shouldSmall, final boolean end, final boolean changeover, final Font font) {
        HSSFCellStyle style = workbook.createCellStyle();

        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);

        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 9);
        font.setColor(HSSFFont.COLOR_NORMAL);

        style.setFont(font);

        if (shouldRed) {
            font.setColor(HSSFFont.COLOR_RED);
            style.setFont(font);
        }
        if (shouldBackground) {
            if (changeover) {
                style.setFillForegroundColor(HSSFColor.LIGHT_BLUE.index);

            } else {
                style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);

            }
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        }
        if (shouldSmall) {
            font.setFontHeightInPoints((short) 7);
            style.setFont(font);
        }
        if (end) {
            style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        }

        return style;
    }

    private HSSFCellStyle getHeaderStyleChangeover(final HSSFWorkbook workbook, final Font font) {
        HSSFCellStyle style = workbook.createCellStyle();

        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);

        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 9);
        font.setColor(HSSFFont.COLOR_NORMAL);

        style.setFont(font);

        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        return style;
    }

    private HSSFCellStyle getHeaderStyleChangeoverEnd(final HSSFWorkbook workbook, final Font font) {
        HSSFCellStyle style = workbook.createCellStyle();

        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);

        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);

        style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        return style;
    }

    public static HSSFCellStyle rowStyle2(final HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();

        style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);

        return style;
    }
}
