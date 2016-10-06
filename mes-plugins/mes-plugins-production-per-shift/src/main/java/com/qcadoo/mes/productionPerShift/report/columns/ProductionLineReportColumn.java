package com.qcadoo.mes.productionPerShift.report.columns;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionPerShift.report.PPSReportXlsHelper;
import com.qcadoo.mes.productionPerShift.report.print.PPSReportXlsStyleContainer;
import com.qcadoo.model.api.Entity;

@Component("productionLineReportColumn")
public class ProductionLineReportColumn extends AbstractReportColumn {

    private final PPSReportXlsHelper ppsReportXlsHelper;

    @Autowired
    public ProductionLineReportColumn(TranslationService translationService, PPSReportXlsHelper ppsReportXlsHelper) {
        super(translationService);
        this.ppsReportXlsHelper = ppsReportXlsHelper;
    }

    @Override
    public String getIdentifier() {
        return "productionLine";
    }

    @Override
    public String getValue(Entity pps) {
        return StringUtils.EMPTY;
    }

    @Override
    public String getFirstRowValue(Entity pps) {
        return ppsReportXlsHelper.getProductionLine(pps).getStringField(ProductionLineFields.NUMBER);
    }

    @Override
    public String getFirstRowChangeoverValue(Entity pps) {
        return ppsReportXlsHelper.getProductionLine(pps).getStringField(ProductionLineFields.NUMBER);
    }

    @Override
    public int getColumnWidth() {
        return 8 * 256;
    }


}
