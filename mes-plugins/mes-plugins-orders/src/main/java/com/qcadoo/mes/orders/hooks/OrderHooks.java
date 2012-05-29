package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.FINISH_DATE;
import static com.qcadoo.mes.orders.constants.OrderFields.START_DATE;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderStates.ABANDONED;
import static com.qcadoo.mes.orders.constants.OrderStates.ACCEPTED;
import static com.qcadoo.mes.orders.constants.OrderStates.PENDING;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OrderHooks {

    public void copyStartDate(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField(START_DATE) == null) {
            return;
        }
        String state = entity.getStringField(STATE);
        if (state.equals(PENDING.getStringValue())) {
            entity.setField(DATE_FROM, entity.getField(START_DATE));
        }
        if (state.equals(ACCEPTED.getStringValue())) {
            entity.setField(CORRECTED_DATE_FROM, entity.getField(START_DATE));
        }
    }

    public void copyEndDate(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField(FINISH_DATE) == null) {
            return;
        }
        String state = entity.getStringField(STATE);
        if (state.equals(PENDING.getStringValue())) {
            entity.setField(DATE_TO, entity.getField(FINISH_DATE));
        }
        if (state.equals(ACCEPTED.getStringValue()) || state.equals(ABANDONED.getStringValue())) {
            entity.setField(CORRECTED_DATE_TO, entity.getField(FINISH_DATE));
        }
    }

}
