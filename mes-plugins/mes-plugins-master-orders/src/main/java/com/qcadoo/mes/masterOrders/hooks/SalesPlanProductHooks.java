package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.masterOrders.constants.SalesPlanProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SalesPlanProductHooks {

    public void onCopy(final DataDefinition salesPlanProductDD, final Entity salesPlanProduct) {
        salesPlanProduct.setField(SalesPlanProductFields.ORDERED_QUANTITY, BigDecimal.ZERO);
        salesPlanProduct.setField(SalesPlanProductFields.ORDERED_TO_WAREHOUSE, BigDecimal.ZERO);
        salesPlanProduct.setField(SalesPlanProductFields.SURPLUS_FROM_PLAN,
                salesPlanProduct.getDecimalField(SalesPlanProductFields.PLANNED_QUANTITY));
    }

}
