package com.qcadoo.mes.productionPerShift.report.columns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productionPerShift.report.PPSReportXlsHelper;
import com.qcadoo.model.api.Entity;

@Component("productNameReportColumn")
public class ProductNameReportColumn extends AbstractReportColumn {

    private final PPSReportXlsHelper ppsReportXlsHelper;

    @Autowired
    public ProductNameReportColumn(TranslationService translationService, PPSReportXlsHelper ppsReportXlsHelper) {
        super(translationService);
        this.ppsReportXlsHelper = ppsReportXlsHelper;
    }

    @Override
    public String getIdentifier() {
        return "productName";
    }

    @Override
    public String getValue(Entity pps) {
        return ppsReportXlsHelper.getProduct(pps).getStringField(ProductFields.NAME);
    }

    @Override
    public String getFirstRowValue(Entity pps) {
        return getValue(pps);
    }

    @Override
    public int getColumnWidth() {
        return 10 * 256;
    }
}
