package com.qcadoo.mes.orders.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.deviations.constants.DeviationType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TypeOfCorrectionCausesHooks {

    @Autowired
    private CommonReasonTypeModelHooks commonReasonTypeModelHooks;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        commonReasonTypeModelHooks.updateDate(entity, DeviationType.QUANTITY_DEVIATION);
    }

}
