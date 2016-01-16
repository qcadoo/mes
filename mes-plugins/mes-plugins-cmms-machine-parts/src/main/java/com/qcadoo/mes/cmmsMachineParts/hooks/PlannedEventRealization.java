package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventRealizationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class PlannedEventRealization {

    public void onCreate(final DataDefinition eventRealizationDD, final Entity eventRealization) {
        eventRealization.setField(PlannedEventRealizationFields.CONFIRMED, true);
    }
}
