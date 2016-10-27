package com.qcadoo.mes.productionPerShift.report.columns;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.productionPerShift.report.print.PPSReportXlsStyleContainer;
import com.qcadoo.model.api.Entity;

public abstract class AbstractReportColumn implements ReportColumn {

    private TranslationService translationService;

    public AbstractReportColumn(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public String getHeader(Locale locale) {
        return translationService.translate("productionPerShift.report.xls.header." + getIdentifier(), locale);
    }

    @Override
    public void setWhiteDataStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_WhiteDataStyle));
    }

    @Override
    public void setWhiteDataStyleEnd(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_WhiteDataStyleEnd));
    }

    @Override
    public void setGreyDataStyleEnd(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_GreyDataStyleEnd));
    }

    @Override
    public void setGreyDataStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_GreyDataStyle));
    }

    @Override
    public void setHeaderStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_HeaderStyle2));
    }

    @Override
    public String getChangeoverValue(Entity pps) {
        return StringUtils.EMPTY;
    }

    @Override
    public String getFirstRowChangeoverValue(Entity pps) {
        return StringUtils.EMPTY;
    }
}
