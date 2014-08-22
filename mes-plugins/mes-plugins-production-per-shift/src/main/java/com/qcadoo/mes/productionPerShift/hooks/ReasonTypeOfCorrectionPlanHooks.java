package com.qcadoo.mes.productionPerShift.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.hooks.CommonReasonTypeModelHooks;
import com.qcadoo.mes.productionPerShift.constants.PpsDeviationModelDescribers;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ReasonTypeOfCorrectionPlanHooks {

    @Autowired
    private CommonReasonTypeModelHooks commonReasonTypeModelHooks;

    public void onSave(final DataDefinition dataDefinition, final Entity reasonTypeOfCorrectionPlanEntity) {
        commonReasonTypeModelHooks.updateDate(reasonTypeOfCorrectionPlanEntity, PpsDeviationModelDescribers.PPS_DEVIATION);
    }

}
