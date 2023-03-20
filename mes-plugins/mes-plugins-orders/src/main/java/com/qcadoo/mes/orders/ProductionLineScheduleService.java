package com.qcadoo.mes.orders;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.orders.validators.ProductionLineSchedulePositionValidators;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginManager;

@Service
public class ProductionLineScheduleService {

    private static final String ORDERS_FOR_SUBPRODUCTS_GENERATION = "ordersForSubproductsGeneration";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private ProductionLineSchedulePositionValidators productionLineSchedulePositionValidators;

    public Date getFinishDate(Map<Long, Date> productionLinesFinishDates, Date scheduleStartTime, Entity productionLine, Entity order) {
        Date finishDate = productionLinesFinishDates.get(productionLine.getId());
        if (finishDate == null) {
            Date ordersMaxFinishDate = getOrdersMaxFinishDateForProductionLine(scheduleStartTime,
                    productionLine, order);
            if (ordersMaxFinishDate != null) {
                finishDate = ordersMaxFinishDate;
                productionLinesFinishDates.put(productionLine.getId(), finishDate);
            }
        }
        if (finishDate == null) {
            finishDate = scheduleStartTime;
        }
        return finishDate;
    }

    private Date getOrdersMaxFinishDateForProductionLine(Date scheduleStartTime, Entity productionLine, Entity order) {
        Entity ordersMaxFinishDateEntity = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.idNe(order.getId()))
                .add(SearchRestrictions.belongsTo(OrderFields.PRODUCTION_LINE, productionLine))
                .add(SearchRestrictions.ne(OrderFields.STATE, OrderStateStringValues.ABANDONED))
                .add(SearchRestrictions.ne(OrderFields.STATE, OrderStateStringValues.DECLINED))
                .add(SearchRestrictions.gt(OrderFields.FINISH_DATE, scheduleStartTime))
                .setProjection(list()
                        .add(alias(SearchProjections.max(OrderFields.FINISH_DATE), OrderFields.FINISH_DATE))
                        .add(rowCount()))
                .addOrder(SearchOrders.desc(OrderFields.FINISH_DATE)).setMaxResults(1).uniqueResult();
        return ordersMaxFinishDateEntity.getDateField(OrderFields.FINISH_DATE);
    }

    public Date getFinishDateWithChildren(Entity position, Date finishDate) {
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)) {
            Date childEndTime = productionLineSchedulePositionValidators.getOrdersChildrenMaxEndTime(position);
            if (!Objects.isNull(childEndTime) && childEndTime.after(finishDate)) {
                finishDate = childEndTime;
            }
        }
        return finishDate;
    }

    public Entity getPreviousOrder(Map<Long, Entity> productionLinesOrders, Entity productionLine, final Date orderStartDate) {
        Entity previousOrder = productionLinesOrders.get(productionLine.getId());
        if (Objects.isNull(previousOrder)) {
            Entity previousOrderFromDB = getPreviousOrderFromDB(productionLine, orderStartDate);
            if (previousOrderFromDB != null) {
                previousOrder = previousOrderFromDB;
            }
        }
        return previousOrder;
    }

    private Entity getPreviousOrderFromDB(final Entity productionLine, final Date orderStartDate) {
        return dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .find()
                .add(SearchRestrictions.belongsTo(OrderFields.PRODUCTION_LINE,
                        productionLine))
                .add(SearchRestrictions.or(SearchRestrictions.ne(OrderFields.STATE, OrderState.DECLINED.getStringValue()),
                        SearchRestrictions.ne(OrderFields.STATE, OrderState.ABANDONED.getStringValue())))
                .add(SearchRestrictions.le(OrderFields.FINISH_DATE, orderStartDate))
                .addOrder(SearchOrders.desc(OrderFields.FINISH_DATE)).setMaxResults(1).uniqueResult();
    }
}
