package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderPositionDtoFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderState;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderHooksMO {

    private static final String MASTER_ORDER_POSITIONS_QUERY = "SELECT pos FROM #masterOrders_masterOrderPositionDto pos WHERE masterOrderId = :masterOrderId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void onSave(final DataDefinition orderDD, final Entity order) {
        Entity orderDb = null;

        if (Objects.nonNull(order.getId())) {
            orderDb = orderDD.get(order.getId());
        }

        Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);

        if (Objects.nonNull(masterOrder)
                && (MasterOrderState.IN_EXECUTION.getStringValue().equals(masterOrder.getStringField(MasterOrderFields.STATE)) || MasterOrderState.NEW
                        .getStringValue().equals(masterOrder.getStringField(MasterOrderFields.STATE)))
                && canChangeToCompleted(order, orderDb)) {
            changeToCompleted(order);
        } else if (canChangeMasterOrderStateToInExecution(order, orderDb)) {
            changeToInExecution(order);
        } else if (canChangeMasterOrderStateToNew(order, orderDb)) {
            changeToNew(order);
        }

        if (Objects.nonNull(masterOrder)) {
            BigDecimal plannedQuantity = BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.PLANNED_QUANTITY));
            Entity masterOrderProduct = getMasterOrderProduct(masterOrder, order.getBelongsToField(OrderFields.PRODUCT));
            if (!MasterOrderPositionStatus.ORDERED.getStringValue().equals(
                    masterOrderProduct.getStringField(MasterOrderProductFields.MASTER_ORDER_POSITION_STATUS))) {
                BigDecimal masterOrderQuantity = BigDecimalUtils.convertNullToZero(masterOrderProduct
                        .getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY));
                if (plannedQuantity.compareTo(masterOrderQuantity) >= 0) {
                    masterOrderProduct.setField(MasterOrderProductFields.MASTER_ORDER_POSITION_STATUS,
                            MasterOrderPositionStatus.ORDERED.getText());
                    masterOrderProduct.getDataDefinition().save(masterOrderProduct);
                }
            }

        }
    }

    private void changeToCompleted(final Entity order) {
        Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);

        if (Objects.isNull(masterOrder)) {
            return;
        }

        masterOrder.setField(MasterOrderFields.STATE, MasterOrderState.COMPLETED.getStringValue());

        masterOrder = masterOrder.getDataDefinition().save(masterOrder);

        order.setField(OrderFieldsMO.MASTER_ORDER, masterOrder);
    }

    private boolean canChangeToCompleted(final Entity order, final Entity orderDb) {
        Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);

        List<Entity> positions = getMasterOrderPositions(masterOrder);

        BigDecimal doneQuantity = BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.DONE_QUANTITY));

        BigDecimal done;

        if (Objects.nonNull(orderDb)) {
            BigDecimal doneQuantityDB = orderDb.getDecimalField(OrderFields.DONE_QUANTITY);

            done = BigDecimalUtils.convertNullToZero(doneQuantity).subtract(BigDecimalUtils.convertNullToZero(doneQuantityDB),
                    numberService.getMathContext());
        } else {
            done = doneQuantity;
        }

        for (Entity position : positions) {
            Integer productId = position.getIntegerField(MasterOrderPositionDtoFields.PRODUCT_ID);
            Integer orderProductId = order.getBelongsToField(OrderFields.PRODUCT).getId().intValue();

            if ((productId != null) && productId.equals(orderProductId)) {
                BigDecimal value = position.getDecimalField(MasterOrderPositionDtoFields.PRODUCED_ORDER_QUANTITY).add(done,
                        numberService.getMathContext());

                if (value.compareTo(doneQuantity) == -1) {
                    value = doneQuantity;
                }

                position.setField(MasterOrderPositionDtoFields.PRODUCED_ORDER_QUANTITY, value);
            }
        }

        List<Entity> producedPositions = positions
                .stream()
                .filter(position -> position.getDecimalField(MasterOrderPositionDtoFields.PRODUCED_ORDER_QUANTITY).compareTo(
                        position.getDecimalField(MasterOrderPositionDtoFields.MASTER_ORDER_QUANTITY)) >= 0)
                .collect(Collectors.toList());

        return (positions.size() == producedPositions.size());
    }

    private List<Entity> getMasterOrderPositions(final Entity masterOrder) {
        return dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO)
                .find(MASTER_ORDER_POSITIONS_QUERY).setParameter("masterOrderId", masterOrder.getId().intValue()).list()
                .getEntities();
    }

    private Entity getMasterOrderProduct(final Entity masterOrder, final Entity product) {
        return dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT).find()
                .add(SearchRestrictions.belongsTo(MasterOrderProductFields.MASTER_ORDER, masterOrder))
                .add(SearchRestrictions.belongsTo(MasterOrderProductFields.PRODUCT, product)).setMaxResults(1).uniqueResult();
    }

    private void changeToNew(final Entity order) {
        Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        if (Objects.isNull(masterOrder)) {
            return;
        }
        String masterOrderStatus = masterOrder.getStringField(MasterOrderFields.STATE);

        if (MasterOrderState.IN_EXECUTION.getStringValue().equals(masterOrderStatus)
                && masterOrder.getHasManyField(MasterOrderFields.ORDERS).size() == 1) {
            masterOrder.setField(MasterOrderFields.STATE, MasterOrderState.NEW.getStringValue());

            masterOrder.getDataDefinition().save(masterOrder);
        }
    }

    private void changeToInExecution(final Entity order) {
        Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        if (Objects.isNull(masterOrder)) {
            return;
        }
        String masterOrderStatus = masterOrder.getStringField(MasterOrderFields.STATE);

        if (MasterOrderState.NEW.getStringValue().equals(masterOrderStatus)) {
            masterOrder.setField(MasterOrderFields.STATE, MasterOrderState.IN_EXECUTION.getStringValue());

            masterOrder = masterOrder.getDataDefinition().save(masterOrder);

            order.setField(OrderFieldsMO.MASTER_ORDER, masterOrder);
        }
    }

    private boolean canChangeMasterOrderStateToInExecution(final Entity order, final Entity orderDb) {
        Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        Entity masterOrderDb = null;

        if (Objects.nonNull(orderDb)) {
            masterOrderDb = orderDb.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        }

        if (Objects.isNull(masterOrder) && Objects.isNull(masterOrderDb)) {
            return false;
        } else if (Objects.nonNull(masterOrder) && Objects.isNull(masterOrderDb)) {
            return true;
        } else if (Objects.isNull(masterOrder) && Objects.nonNull(masterOrderDb)) {
            return false;
        } else if (Objects.nonNull(masterOrder) && Objects.nonNull(masterOrderDb)) {
            if (masterOrder.getId().equals(masterOrderDb.getId())) {
                return false;
            } else {
                return true;
            }
        }

        return true;
    }

    private boolean canChangeMasterOrderStateToNew(final Entity order, final Entity orderDB) {
        Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        Entity masterOrderDb = null;

        if (Objects.nonNull(orderDB)) {
            masterOrderDb = orderDB.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        }
        if (Objects.isNull(masterOrder) && Objects.nonNull(masterOrderDb)) {
            return true;
        }

        return false;
    }

}
