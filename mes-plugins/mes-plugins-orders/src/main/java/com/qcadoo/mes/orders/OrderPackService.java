package com.qcadoo.mes.orders;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderPackFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
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

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    public BigDecimal getSumQuantityOrderPacksForOrderWithoutPack(Entity order, Long orderPackId) {
        if (order.getHasManyField(OrderFields.ORDER_PACKS).size() == 1 && orderPackId != null
                || order.getHasManyField(OrderFields.ORDER_PACKS).isEmpty()) {
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
        if (order.getHasManyField(OrderFields.ORDER_PACKS).isEmpty()) {
            return BigDecimal.ZERO;
        }
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_PACK).find()
                .add(SearchRestrictions.belongsTo(OrderPackFields.ORDER, order))
                .setProjection(alias(sum(OrderPackFields.QUANTITY), QNT_SUM_FIELD_NAME)).addOrder(asc(QNT_SUM_FIELD_NAME))
                .setMaxResults(1).uniqueResult().getDecimalField(QNT_SUM_FIELD_NAME);
    }

    public void generateOrderPacks(Entity order) {
        Entity parameter = parameterService.getParameter();
        if (order.getHasManyField(OrderFields.ORDER_PACKS).isEmpty()
                && parameter.getBooleanField(ParameterFieldsO.GENERATE_PACKS_FOR_ORDERS)) {
            BigDecimal optimalPackSize = parameter.getDecimalField(ParameterFieldsO.OPTIMAL_PACK_SIZE);
            BigDecimal restFeedingLastPack = parameter.getDecimalField(ParameterFieldsO.REST_FEEDING_LAST_PACK);
            BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
            BigDecimal[] values = plannedQuantity.divideAndRemainder(optimalPackSize, numberService.getMathContext());
            DataDefinition dataDefinition = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_ORDER_PACK);
            Entity lastOrderPack = null;
            for (int i = 0; i < values[0].intValue(); i++) {
                Entity orderPack = dataDefinition.create();
                orderPack.setField(OrderPackFields.ORDER, order);
                orderPack.setField(OrderPackFields.QUANTITY, optimalPackSize);
                lastOrderPack = dataDefinition.save(orderPack);
            }
            if (values[1].compareTo(restFeedingLastPack) > 0 || lastOrderPack == null) {
                Entity orderPack = dataDefinition.create();
                orderPack.setField(OrderPackFields.ORDER, order);
                orderPack.setField(OrderPackFields.QUANTITY, values[1]);
                dataDefinition.save(orderPack);
            } else {
                lastOrderPack.setField(OrderPackFields.QUANTITY,
                        lastOrderPack.getDecimalField(OrderPackFields.QUANTITY).add(values[1], numberService.getMathContext()));
                dataDefinition.save(lastOrderPack);
            }
        }
    }
}
