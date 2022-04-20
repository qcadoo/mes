package com.qcadoo.mes.masterOrders.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderPositionDtoFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderState;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderHooksMO {

    private static final String MASTER_ORDER_POSITIONS_QUERY = "SELECT pos FROM #masterOrders_masterOrderPositionDto pos WHERE masterOrderId = :masterOrderId";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void onSave(final DataDefinition orderDD, final Entity order) {
        setMasterOrderProductStatus(orderDD, order);
        setMasterOrderDateBasedOnOrderDates(orderDD, order);
    }

    public boolean onDelete(final DataDefinition orderDD, final Entity order) {

        if (parameterService.getParameter().getBooleanField("setMasterOrderDateBasedOnOrderDates")) {
            Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);

            if (Objects.isNull(masterOrder)) {
                return true;
            }
            List<Entity> orders = Lists.newArrayList(masterOrder.getHasManyField(MasterOrderFields.ORDERS));
            if (Objects.nonNull(order.getId())) {
                orders = orders.stream().filter(op -> !op.getId().equals(order.getId()))
                        .collect(Collectors.toList());
            }
            setMasterOrderDates(masterOrder, orders);
        }
        return true;
    }

    private void setMasterOrderDateBasedOnOrderDates(DataDefinition orderDD, Entity order) {
        if (parameterService.getParameter().getBooleanField("setMasterOrderDateBasedOnOrderDates")) {

            Entity orderDb = null;
            Entity masterOrderDb = null;

            if (Objects.nonNull(order.getId())) {
                orderDb = orderDD.get(order.getId());
                masterOrderDb = orderDb.getBelongsToField(OrderFieldsMO.MASTER_ORDER);
            }

            Entity masterOrder = order.getBelongsToField(OrderFieldsMO.MASTER_ORDER);

            if (Objects.nonNull(masterOrder)) {
                List<Entity> orders = Lists.newArrayList(masterOrder.getHasManyField(MasterOrderFields.ORDERS));
                if (Objects.nonNull(order.getId())) {
                    orders = orders.stream().filter(op -> !op.getId().equals(order.getId()))
                            .collect(Collectors.toList());
                }
                orders.add(order);
                setMasterOrderDates(masterOrder, orders);
            } else if (Objects.nonNull(masterOrderDb)) {
                List<Entity> orders = Lists.newArrayList(masterOrderDb.getHasManyField(MasterOrderFields.ORDERS));
                orders = orders.stream().filter(op -> !op.getId().equals(order.getId()))
                        .collect(Collectors.toList());
                setMasterOrderDates(masterOrderDb, orders);
            }
        }
    }

    private void setMasterOrderDates(Entity masterOrder, List<Entity> orders) {
        boolean anyDatesEmpty = orders.stream().allMatch(o -> Objects.isNull(o.getDateField(OrderFields.START_DATE))
                || Objects.isNull(o.getDateField(OrderFields.FINISH_DATE)));

        if (anyDatesEmpty && (Objects.nonNull(masterOrder.getDateField(MasterOrderFields.START_DATE))
                || Objects.nonNull(masterOrder.getDateField(MasterOrderFields.FINISH_DATE)))) {
            masterOrder.setField(MasterOrderFields.START_DATE, null);
            masterOrder.setField(MasterOrderFields.FINISH_DATE, null);
            masterOrder.getDataDefinition().fastSave(masterOrder);
        } else {

            List<Entity> ordersToCalculate = orders.stream().filter(o -> Objects.nonNull(o.getDateField(OrderFields.START_DATE))
                    && Objects.nonNull(o.getDateField(OrderFields.FINISH_DATE))).collect(Collectors.toList());

            Optional<Date> start = ordersToCalculate.stream().filter(o -> Objects.nonNull(o.getDateField(OrderFields.START_DATE)))
                    .map(o -> o.getDateField(OrderFields.START_DATE))
                    .min(Date::compareTo);

            Optional<Date> finish = ordersToCalculate.stream()
                    .filter(o -> Objects.nonNull(o.getDateField(OrderFields.FINISH_DATE)))
                    .map(o -> o.getDateField(OrderFields.FINISH_DATE))
                    .max(Date::compareTo);

            boolean changed = false;
            if (start.isPresent() && (Objects.isNull(masterOrder.getDateField(MasterOrderFields.START_DATE))
                    || !masterOrder.getDateField(MasterOrderFields.START_DATE).equals(start.get()))) {
                changed = true;
                masterOrder.setField(MasterOrderFields.START_DATE, start.get());
            }

            if (finish.isPresent() && (Objects.isNull(masterOrder.getDateField(MasterOrderFields.FINISH_DATE))
                    || !masterOrder.getDateField(MasterOrderFields.FINISH_DATE).equals(finish.get()))) {
                changed = true;
                masterOrder.setField(MasterOrderFields.FINISH_DATE, finish.get());
            }

            if (changed) {
                masterOrder.getDataDefinition().fastSave(masterOrder);
            }
        }
    }

    private void setMasterOrderProductStatus(DataDefinition orderDD, Entity order) {
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
            if (Objects.nonNull(masterOrderProduct)
                    && !MasterOrderPositionStatus.ORDERED.getStringValue().equals(
                    masterOrderProduct.getStringField(MasterOrderProductFields.MASTER_ORDER_POSITION_STATUS))) {
                BigDecimal masterOrderQuantity = BigDecimalUtils.convertNullToZero(masterOrderProduct
                        .getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY));
                BigDecimal planned = masterOrder
                        .getHasManyField(MasterOrderFields.ORDERS)
                        .stream()
                        .filter(o -> o.getBelongsToField(OrderFields.PRODUCT).getId()
                                .equals(order.getBelongsToField(OrderFields.PRODUCT).getId()))
                        .map(o -> o.getDecimalField(OrderFields.PLANNED_QUANTITY)).reduce(BigDecimal.ZERO, BigDecimal::add);
                planned = planned.add(plannedQuantity, numberService.getMathContext());
                if (planned.compareTo(masterOrderQuantity) >= 0) {
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
