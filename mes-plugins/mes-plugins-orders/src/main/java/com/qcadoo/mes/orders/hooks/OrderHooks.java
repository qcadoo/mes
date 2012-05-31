package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.FINISH_DATE;
import static com.qcadoo.mes.orders.constants.OrderFields.START_DATE;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderStates.ABANDONED;
import static com.qcadoo.mes.orders.constants.OrderStates.ACCEPTED;
import static com.qcadoo.mes.orders.constants.OrderStates.PENDING;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class OrderHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void copyStartDate(final DataDefinition dataDefinition, final Entity entity) {

        if (entity.getField(START_DATE) == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                entity.getId());
        String state = entity.getStringField(STATE);
        Date startDate = ((Date) entity.getField(START_DATE));
        Date startDateDB = (Date) order.getField(START_DATE);
        if (state.equals(PENDING.getStringValue()) && !(startDateDB.compareTo(startDate) == 0)) {
            entity.setField(DATE_FROM, entity.getField(START_DATE));
        }
        if (state.equals(ACCEPTED.getStringValue()) && !(startDateDB.compareTo(startDate) == 0)) {
            entity.setField(CORRECTED_DATE_FROM, entity.getField(START_DATE));
        }
    }

    public void copyEndDate(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField(FINISH_DATE) == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                entity.getId());
        String state = entity.getStringField(STATE);
        Date finishDate = (Date) entity.getField(FINISH_DATE);
        Date finishDateDB = (Date) order.getField(FINISH_DATE);
        if (state.equals(PENDING.getStringValue()) && !(finishDateDB.compareTo(finishDate) == 0)) {
            entity.setField(DATE_TO, entity.getField(FINISH_DATE));
        }
        if ((state.equals(ACCEPTED.getStringValue()) || state.equals(ABANDONED.getStringValue()))
                && !(finishDateDB.compareTo(finishDate) == 0)) {
            entity.setField(CORRECTED_DATE_TO, entity.getField(FINISH_DATE));
        }
    }

    public void fillStartDate(final DataDefinition dataDefinition, final Entity order) {
        if (order.getField(EFFECTIVE_DATE_FROM) != null) {
            order.setField(START_DATE, order.getField(EFFECTIVE_DATE_FROM));
        } else if (order.getField(CORRECTED_DATE_FROM) != null) {
            order.setField(START_DATE, order.getField(CORRECTED_DATE_FROM));
        } else {
            order.setField(START_DATE, order.getField(DATE_FROM));
        }
    }

    public void fillEndDate(final DataDefinition dataDefinition, final Entity order) {
        if (order.getField(EFFECTIVE_DATE_TO) != null) {
            order.setField(FINISH_DATE, order.getField(EFFECTIVE_DATE_TO));
        } else if (order.getField(CORRECTED_DATE_TO) != null) {
            order.setField(FINISH_DATE, order.getField(CORRECTED_DATE_TO));
        } else {
            order.setField(FINISH_DATE, order.getField(DATE_TO));
        }
    }

}
