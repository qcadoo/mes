package com.qcadoo.mes.masterOrders.util;

import static com.qcadoo.model.api.search.SearchProjections.*;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;
import com.qcadoo.model.api.utils.EntityUtils;

@Service
public class MasterOrderOrdersDataProvider {

    public static final SearchProjection ORDER_NUMBER_PROJECTION = list().add(
            alias(field(OrderFields.NUMBER), OrderFields.NUMBER)).add(alias(id(), "id"));

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public long countBelongingOrders(final Long masterOrderId, final SearchCriterion criteria) {
        List<Entity> ordersCountProjection = findBelongingOrders(masterOrderId, alias(rowCount(), "count"), criteria,
                SearchOrders.desc("count"));
        for (Entity entity : ordersCountProjection) {
            return (Long) entity.getField("count");
        }
        return 0L;
    }

    public Collection<String> findBelongingOrderNumbers(final Long masterOrderId, final SearchCriterion searchCriteria) {
        List<Entity> ordersProjection = findBelongingOrders(masterOrderId, ORDER_NUMBER_PROJECTION, searchCriteria, null);
        return EntityUtils.getFieldsView(ordersProjection, OrderFields.NUMBER);
    }

    public List<Entity> findBelongingOrders(final Long masterOrderId, final SearchProjection projection,
            final SearchCriterion criteria, final SearchOrder searchOrder) {
        SearchCriteriaBuilder scb = getOrderDD().find();
        scb.add(belongsTo(OrderFieldsMO.MASTER_ORDER, MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_MASTER_ORDER, masterOrderId));
        if (criteria != null) {
            scb.add(criteria);
        }
        if (projection != null) {
            scb.setProjection(projection);
        }
        if (searchOrder != null) {
            scb.addOrder(searchOrder);
        }
        return scb.list().getEntities();
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

}
