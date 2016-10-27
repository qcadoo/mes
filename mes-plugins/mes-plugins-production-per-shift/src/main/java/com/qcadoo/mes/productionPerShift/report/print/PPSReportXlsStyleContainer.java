package com.qcadoo.mes.productionPerShift.report.print;

import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;

import com.google.common.collect.Maps;

/**
 * Styles and style identifiers
 */
public class PPSReportXlsStyleContainer {

    public static final String I_WhiteDataStyleSmall = "whiteDataStyleSmall";

    public static final String I_GreyDataStyleSmall = "greyDataStyleSmall";

    public static final String I_WhiteDataStyleEnd = "whiteDataStyleEnd";

    public static final String I_GreyDataStyleEnd = "greyDataStyleEnd";

    public static final String I_WhiteDataStyle = "whiteDataStyle";

    public static final String I_GreyDataStyle = "greyDataStyle";

    public static final String I_ChangeoverDataStyle = "changeoverDataStyle";

    public static final String I_WhiteDataStyleRed = "whiteDataStyleRed";

    public static final String I_GreyDataStyleRed = "greyDataStyleRed";

    public static final String I_HeaderStyle1 = "headerStyle1";

    public static final String I_HeaderStyle2 = "headerStyle2";

    public static final String I_HeaderStyle2Red = "headerStyle2Red";

    private Map<String, HSSFCellStyle> styles = Maps.newHashMap();

    private Font fontNormal;

    private Font fontRed;

    private Font fontSmall;

    private Font fontSmallRed;

    public PPSReportXlsStyleContainer(HSSFSheet sheet) {
        initStyle(sheet);
    }

    private void initStyle(final HSSFSheet sheet) {
        this.fontNormal = sheet.getWorkbook().createFont();
        fontNormal.setFontName(HSSFFont.FONT_ARIAL);
        fontNormal.setBoldweight(Font.BOLDWEIGHT_BOLD);
        fontNormal.setFontHeightInPoints((short) 9);
        fontNormal.setColor(HSSFFont.COLOR_NORMAL);

        this.fontRed = sheet.getWorkbook().createFont();
        fontRed.setFontName(HSSFFont.FONT_ARIAL);
        fontRed.setBoldweight(Font.BOLDWEIGHT_BOLD);
        fontRed.setFontHeightInPoints((short) 9);
        fontRed.setColor(HSSFFont.COLOR_NORMAL);
        fontRed.setColor(HSSFFont.COLOR_RED);

        this.fontSmall = sheet.getWorkbook().createFont();
        fontSmall.setFontName(HSSFFont.FONT_ARIAL);
        fontSmall.setBoldweight(Font.BOLDWEIGHT_BOLD);
        fontSmall.setFontHeightInPoints((short) 7);
        fontSmall.setColor(HSSFFont.COLOR_NORMAL);

        this.fontSmallRed = sheet.getWorkbook().createFont();
        fontSmallRed.setFontName(HSSFFont.FONT_ARIAL);
        fontSmallRed.setBoldweight(Font.BOLDWEIGHT_BOLD);
        fontSmallRed.setFontHeightInPoints((short) 7);
        fontSmallRed.setColor(HSSFFont.COLOR_NORMAL);
        fontSmallRed.setColor(HSSFFont.COLOR_RED);

        initWhiteDataStyleSmall(sheet);
        greyDataStyleSmall(sheet);
        whiteDataStyleEnd(sheet);
        greyDataStyleEnd(sheet);
        whiteDataStyle(sheet);
        greyDataStyle(sheet);
        changeoverDataStyle(sheet);
        whiteDataStyleRed(sheet);
        greyDataStyleRed(sheet);
        headerStyle1(sheet);
        headerStyle2(sheet);
        headerStyle2Red(sheet);
    }

    private void headerStyle2Red(HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setFont(fontSmallRed);
        styles.put(I_HeaderStyle2Red, style);

    }

    private void headerStyle2(HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setFont(fontSmall);
        styles.put(I_HeaderStyle2, style);
    }

    private void headerStyle1(HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
        style.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setFont(fontNormal);
        styles.put(I_HeaderStyle1, style);
    }

    private void greyDataStyleRed(HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setFont(fontRed);
        styles.put(I_GreyDataStyleRed, style);
    }

    private void whiteDataStyleRed(HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setFont(fontRed);
        styles.put(I_WhiteDataStyleRed, style);
    }

    private void changeoverDataStyle(HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();


        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);



        style.setFont(fontNormal);

        style.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        styles.put(I_ChangeoverDataStyle, style);
    }

    private void whiteDataStyle(HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();

        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setFont(fontNormal);
        styles.put(I_WhiteDataStyle, style);
    }

    private void greyDataStyle(HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setFont(fontNormal);
        styles.put(I_GreyDataStyle, style);
    }

    private void greyDataStyleEnd(HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style.setFont(fontNormal);
        styles.put(I_GreyDataStyleEnd, style);
    }

    private void whiteDataStyleEnd(HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();

        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setFont(fontNormal);
        style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);

        styles.put(I_WhiteDataStyleEnd, style);
    }

    private void greyDataStyleSmall(HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();

        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setFont(fontSmall);
        styles.put(I_GreyDataStyleSmall, style);
    }

    private void initWhiteDataStyleSmall(HSSFSheet sheet) {
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();

        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setFont(fontSmall);
        styles.put(I_WhiteDataStyleSmall, style);
    }

    public Map<String, HSSFCellStyle> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, HSSFCellStyle> styles) {
        this.styles = styles;
    }
}
