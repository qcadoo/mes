package com.qcadoo.mes.productionPerShift.report.columns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.report.PPSReportXlsHelper;
import com.qcadoo.model.api.Entity;

@Component("orderNumberReportColumn")
public class OrderNumberReportColumn extends AbstractReportColumn {

    private final PPSReportXlsHelper ppsReportXlsHelper;

    @Autowired
    public OrderNumberReportColumn(TranslationService translationService, PPSReportXlsHelper ppsReportXlsHelper) {
        super(translationService);
        this.ppsReportXlsHelper = ppsReportXlsHelper;
    }

    @Override
    public String getIdentifier() {
        return "orderNumber";
    }

    @Override
    public String getValue(Entity pps) {
        return ppsReportXlsHelper.getOrder(pps).getStringField(OrderFields.NUMBER);
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
