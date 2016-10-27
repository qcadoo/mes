package com.qcadoo.mes.productionPerShift.report.columns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.report.PPSReportXlsHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Component("orderSizeReportColumn")
public class OrderSizeReportColumn extends AbstractReportColumn {

    private static final String L_CARTON_UNIT = "karton";

    private static final String L_ERR = "ERR";

    private PPSReportXlsHelper ppsReportXlsHelper;

    private NumberService numberService;

    @Autowired
    public OrderSizeReportColumn(TranslationService translationService, PPSReportXlsHelper ppsReportXlsHelper,
            NumberService numberService) {
        super(translationService);
        this.ppsReportXlsHelper = ppsReportXlsHelper;
        this.numberService = numberService;
    }

    @Override
    public String getIdentifier() {
        return "orderSize";
    }

    @Override
    public String getValue(Entity productionPerShift) {
        if (ppsReportXlsHelper.isSztUnit(productionPerShift)) {
            return numberService.formatWithMinimumFractionDigits(
                    ppsReportXlsHelper.getOrder(productionPerShift).getDecimalField(OrderFields.PLANNED_QUANTITY), 0);
        } else {
            return L_ERR;
        }

    }

    @Override
    public String getFirstRowValue(Entity productionPerShift) {
        return getValue(productionPerShift);
    }

    @Override
    public int getColumnWidth() {
        return 7 * 256;
    }
}
