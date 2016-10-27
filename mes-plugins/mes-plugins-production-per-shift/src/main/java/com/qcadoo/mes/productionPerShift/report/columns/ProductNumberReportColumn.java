package com.qcadoo.mes.productionPerShift.report.columns;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productionPerShift.report.PPSReportXlsHelper;
import com.qcadoo.mes.productionPerShift.report.print.PPSReportXlsStyleContainer;
import com.qcadoo.model.api.Entity;

@Component("productNumberReportColumn")
public class ProductNumberReportColumn extends AbstractReportColumn {

    private final PPSReportXlsHelper ppsReportXlsHelper;

    @Autowired
    public ProductNumberReportColumn(TranslationService translationService, PPSReportXlsHelper ppsReportXlsHelper) {
        super(translationService);
        this.ppsReportXlsHelper = ppsReportXlsHelper;
    }

    @Override
    public String getIdentifier() {
        return "productNumber";
    }

    @Override
    public String getValue(Entity pps) {
        return ppsReportXlsHelper.getProduct(pps).getStringField(ProductFields.NUMBER);
    }

    @Override
    public String getFirstRowValue(Entity pps) {
        return getValue(pps);
    }

    @Override
    public int getColumnWidth() {
        return 8 * 256;
    }

    @Override
    public void setWhiteDataStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_WhiteDataStyleRed));
    }

    @Override
    public void setGreyDataStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_GreyDataStyleRed));
    }

    @Override
    public void setHeaderStyle(final HSSFCell cell, final PPSReportXlsStyleContainer styleContainer) {
        cell.setCellStyle(styleContainer.getStyles().get(PPSReportXlsStyleContainer.I_HeaderStyle2Red));
    }
}
