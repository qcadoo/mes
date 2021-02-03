package com.qcadoo.mes.orders;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.sum;

@Service
public class OrderPackService {

    private static final String QNT_SUM_FIELD_NAME = "sumOfQuantities";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public BigDecimal getSumQuantityOrderPacksForOrderWithoutPack(Entity order, Long orderPackId) {
        if (order.getHasManyField(OrderFields.ORDER_PACKS).size() == 1 && orderPackId != null
                || order.getHasManyField(OrderFields.ORDER_PACKS).size() == 0) {
            return BigDecimal.ZERO;
        }
        SearchCriteriaBuilder scb = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_PACK)
                .find().add(SearchRestrictions.belongsTo(OrderPackFields.ORDER, order));
        if (orderPackId != null) {
            scb.add(SearchRestrictions.idNe(orderPackId));
        }
        scb.setProjection(alias(sum(OrderPackFields.QUANTITY), QNT_SUM_FIELD_NAME)).addOrder(asc(QNT_SUM_FIELD_NAME));
        return scb.setMaxResults(1).uniqueResult().getDecimalField(QNT_SUM_FIELD_NAME);
    }

    public BigDecimal getSumQuantityOrderPacksForOrder(Entity order) {
        if (order.getHasManyField(OrderFields.ORDER_PACKS).size() == 0) {
            return BigDecimal.ZERO;
        }
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_PACK).find()
                .add(SearchRestrictions.belongsTo(OrderPackFields.ORDER, order))
                .setProjection(alias(sum(OrderPackFields.QUANTITY), QNT_SUM_FIELD_NAME)).addOrder(asc(QNT_SUM_FIELD_NAME))
                .setMaxResults(1).uniqueResult().getDecimalField(QNT_SUM_FIELD_NAME);
    }
}
