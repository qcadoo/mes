package com.qcadoo.mes.productionPerShift.report.columns;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productionPerShift.report.PPSReportXlsHelper;
import com.qcadoo.model.api.Entity;

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

}
