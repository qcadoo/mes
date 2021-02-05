package com.qcadoo.mes.orders.hooks;

import com.qcadoo.mes.orders.OrderPackService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Objects;

@Service
public class OrderPackHooks {

    @Autowired
    private OrderPackService orderPackService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private NumberService numberService;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        setNumber(entity);
    }

    private void setNumber(final Entity entity) {
        if (checkIfShouldInsertNumber(entity)) {
            String number = jdbcTemplate.queryForObject("select generate_order_pack_number()", Collections.emptyMap(),
                    String.class);
            entity.setField(OrderPackFields.NUMBER, number);
        }
    }

    private boolean checkIfShouldInsertNumber(final Entity entity) {
        if (!Objects.isNull(entity.getId())) {
            return false;
        }
        return !StringUtils.isNotBlank(entity.getStringField(OrderPackFields.NUMBER));
    }

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        Entity order = entity.getBelongsToField(OrderPackFields.ORDER);
        BigDecimal sumQuantityOrderPacks = orderPackService.getSumQuantityOrderPacksForOrderWithoutPack(order, entity.getId())
                .add(entity.getDecimalField(OrderPackFields.QUANTITY), numberService.getMathContext());
        if (order.getDecimalField(OrderFields.PLANNED_QUANTITY).compareTo(sumQuantityOrderPacks) < 0) {
            entity.addError(dataDefinition.getField(OrderPackFields.QUANTITY), "orderPacks.validate.global.error.quantityError");
            return false;
        }
        return true;
    }
}
