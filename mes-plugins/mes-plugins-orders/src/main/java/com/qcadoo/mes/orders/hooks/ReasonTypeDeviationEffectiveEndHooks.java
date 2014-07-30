package com.qcadoo.mes.orders.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.deviationReasonTypes.OrderDeviationModelDescribers;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ReasonTypeDeviationEffectiveEndHooks {

    @Autowired
    private CommonReasonTypeModelHooks commonReasonTypeModelHooks;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        commonReasonTypeModelHooks.updateDate(entity, OrderDeviationModelDescribers.EFFECTIVE_FINISH_DATE_DEVIATION);
    }

}
