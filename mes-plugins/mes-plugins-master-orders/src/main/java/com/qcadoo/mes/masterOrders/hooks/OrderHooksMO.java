package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderState;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class OrderHooksMO {

    private static final String MASTER_ORDER_POSITIONS_QUERY = "SELECT pos FROM #masterOrders_masterOrderPositionDto pos WHERE masterOrderId = :masterOrderId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition orderDD, final Entity order) {
        if (Objects.nonNull(order.getId())) {
            Entity orderDb = orderDD.get(order.getId());
            if (canChangeMasterOrderStateToInExecution(order, orderDb)) {
                changeToInExecution(order);
            } else if (canChangeMasterOrderStateToNew(order, orderDb)) {
                changeToNew(order, orderDb);
            } else if (Objects.nonNull(order.getBelongsToField(OrderFieldsMO.MASTER_ORDER))
                    && MasterOrderState.IN_EXECUTION.getStringValue().equals(
                            order.getBelongsToField(OrderFieldsMO.MASTER_ORDER).getStringField(MasterOrderFields.STATE))) {
                changeToCompleted(order);
            }
        }
    }

    private void changeToCompleted(Entity order) {
        Entity mo = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        String queryForProducedPositions = MASTER_ORDER_POSITIONS_QUERY
                + " AND pos.producedOrderQuantity >= masterOrderQuantity";

        List<Entity> positions = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO)
                .find(MASTER_ORDER_POSITIONS_QUERY).setParameter("masterOrderId", mo.getId().intValue()).list()
                .getEntities();
        List<Entity> producedPositions = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO)
                .find(queryForProducedPositions).setParameter("masterOrderId", mo.getId().intValue()).list()
                .getEntities();
        if (positions.size() == producedPositions.size()) {
            mo.setField(MasterOrderFields.STATE, MasterOrderState.COMPLETED.getStringValue());
            mo = mo.getDataDefinition().save(mo);
            order.setField(OrderFieldsMO.MASTER_ORDER, mo);
        }
    }

    private void changeToNew(Entity order, Entity orderDb) {
        Entity moDB = orderDb.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        String masterOrderStatus = moDB.getStringField(MasterOrderFields.STATE);
        if (MasterOrderState.IN_EXECUTION.getStringValue().equals(masterOrderStatus)
                && moDB.getHasManyField(MasterOrderFields.ORDERS).size() == 1) {
            moDB.setField(MasterOrderFields.STATE, MasterOrderState.NEW.getStringValue());
            moDB = moDB.getDataDefinition().save(moDB);
        }
    }

    private void changeToInExecution(Entity order) {
        Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        String masterOrderStatus = masterOrder.getStringField(MasterOrderFields.STATE);
        List<Entity> positions = dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_MASTER_ORDER_POSITION_DTO)
                .find(MASTER_ORDER_POSITIONS_QUERY).setParameter("masterOrderId", masterOrder.getId().intValue()).list()
                .getEntities();

        boolean hasAnOrderedQuantity = checkIfHasAnOrderedQuantity(positions);
        if (hasAnOrderedQuantity && MasterOrderState.NEW.getStringValue().equals(masterOrderStatus)) {
            masterOrder.setField(MasterOrderFields.STATE, MasterOrderState.IN_EXECUTION.getStringValue());
            masterOrder = masterOrder.getDataDefinition().save(masterOrder);
            order.setField(OrderFieldsMO.MASTER_ORDER, masterOrder);
        }
    }

    private boolean checkIfHasAnOrderedQuantity(List<Entity> positions) {
        return positions
                .stream()
                .filter(position -> BigDecimal.ZERO.compareTo(BigDecimalUtils.convertNullToZero(position
                        .getDecimalField("masterOrderQuantity"))) == -1).findAny().isPresent();
    }

    private boolean canChangeMasterOrderStateToInExecution(final Entity order, final Entity orderDB) {
        Entity mo = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        Entity moDB = orderDB.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        if (Objects.isNull(mo) && Objects.isNull(moDB)) {
            return false;
        } else if (Objects.nonNull(mo) && Objects.isNull(moDB)) {
            return true;
        } else if (Objects.isNull(mo) && Objects.nonNull(moDB)) {
            return false;
        } else if (Objects.nonNull(mo) && Objects.isNull(moDB)) {
            if (mo.getId().equals(moDB.getId())) {
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    private boolean canChangeMasterOrderStateToNew(final Entity order, final Entity orderDB) {
        Entity mo = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        Entity moDB = orderDB.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
        if (Objects.isNull(mo) && Objects.nonNull(moDB)) {
            return true;
        }
        return false;

    }
}
