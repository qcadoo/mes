package com.qcadoo.mes.deliveries.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DeliveryHooks {

    public void clearStateFieldOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(DeliveryFields.STATE, DeliveryStateStringValues.DRAFT);
    }

}
