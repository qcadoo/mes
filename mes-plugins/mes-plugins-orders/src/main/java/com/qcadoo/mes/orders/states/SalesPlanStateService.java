package com.qcadoo.mes.orders.states;

import com.qcadoo.mes.newstates.BasicStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SalesPlanStateService extends BasicStateService implements SalesPlanServiceMarker {

    @Autowired
    private SalesPlanStateChangeDescriber salesPlanStateChangeDescriber;

    @Override
    public SalesPlanStateChangeDescriber getChangeEntityDescriber() {
        return salesPlanStateChangeDescriber;
    }

}
