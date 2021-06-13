package com.qcadoo.mes.orders.hooks;

import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.OrderPackService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.mes.orders.states.OrderPackServiceMarker;
import com.qcadoo.mes.orders.states.constants.OrderPackStateStringValues;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderPackHooks {

    @Autowired
    private OrderPackService orderPackService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private NumberService numberService;


    @Autowired
    private StateExecutorService stateExecutorService;

    public void onCreate(final DataDefinition orderPackDD, final Entity orderPack) {
        setInitialState(orderPack);
    }

    public void onCopy(final DataDefinition orderPackDD, final Entity orderPack) {
        setInitialState(orderPack);
    }

    private void setInitialState(final Entity orderPack) {
        stateExecutorService.buildInitial(OrderPackServiceMarker.class, orderPack, OrderPackStateStringValues.PENDING);
    }

    public boolean validatesWith(final DataDefinition orderPackDD, final Entity orderPack) {
        Entity order = orderPack.getBelongsToField(OrderPackFields.ORDER);
        String orderState = order.getStringField(OrderFields.STATE);

        if (OrderState.COMPLETED.getStringValue().equals(orderState) || OrderState.DECLINED.getStringValue().equals(orderState)
                || OrderState.ABANDONED.getStringValue().equals(orderState)
                || OrderState.PENDING.getStringValue().equals(orderState)) {
            orderPack.addError(orderPackDD.getField(OrderPackFields.ORDER), "orderPacks.validate.global.error.orderStateError");

            return false;
        }

        BigDecimal sumQuantityOrderPacks = orderPackService.getSumQuantityOrderPacksForOrderWithoutPack(order, orderPack.getId())
                .add(orderPack.getDecimalField(OrderPackFields.QUANTITY), numberService.getMathContext());

        if (order.getDecimalField(OrderFields.PLANNED_QUANTITY).compareTo(sumQuantityOrderPacks) < 0) {
            orderPack.addError(orderPackDD.getField(OrderPackFields.QUANTITY), "orderPacks.validate.global.error.quantityError");

            return false;
        }

        return true;
    }

    public void onSave(final DataDefinition orderPackDD, final Entity orderPack) {
        setNumber(orderPack);
    }

    private void setNumber(final Entity entity) {
        if (checkIfShouldInsertNumber(entity)) {
            String number = jdbcTemplate.queryForObject("SELECT generate_order_pack_number()", Collections.emptyMap(),
                    String.class);

            entity.setField(OrderPackFields.NUMBER, number);
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity orderPack) {
        if (!Objects.isNull(orderPack.getId())) {
            return false;
        }

        return !StringUtils.isNotBlank(orderPack.getStringField(OrderPackFields.NUMBER));
    }

    public boolean onDelete(final DataDefinition orderPackDD, final Entity orderPack) {
        List<Entity> orderTechnologicalProcesses = orderPack.getHasManyField(OrderPackFields.ORDER_TECHNOLOGICAL_PROCESSES);

        if (!orderTechnologicalProcesses.isEmpty()) {
            orderPack.addGlobalError("orderPacks.validate.global.error.orderTechnologicalProcessesExists");

            return false;
        }

        return true;
    }

}
