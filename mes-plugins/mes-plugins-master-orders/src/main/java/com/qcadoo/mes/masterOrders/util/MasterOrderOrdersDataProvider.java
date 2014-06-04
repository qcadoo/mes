package com.qcadoo.mes.masterOrders.util;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.field;
import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;
import static com.qcadoo.model.api.search.SearchProjections.sum;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.utils.EntityUtils;

@Service
public class MasterOrderOrdersDataProvider {

    public static final SearchProjection ORDER_NUMBER_PROJECTION = list().add(
            alias(field(OrderFields.NUMBER), OrderFields.NUMBER)).add(alias(id(), "id"));

    private static final String QUANTITIES_SUM_ALIAS = "quantity";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public long countBelongingOrders(final Entity masterOrder, final SearchCriterion additionalCriteria) {
        List<Entity> ordersCountProjection = findBelongingOrders(masterOrder, alias(rowCount(), "count"), additionalCriteria,
                SearchOrders.desc("count"));
        for (Entity entity : ordersCountProjection) {
            return (Long) entity.getField("count");
        }
        return 0L;
    }

    public BigDecimal sumBelongingOrdersPlannedQuantities(final Entity masterOrder, final Entity product) {
        SearchProjection quantitiesSumProjection = list().add(alias(sum(OrderFields.PLANNED_QUANTITY), QUANTITIES_SUM_ALIAS))
                .add(rowCount());
        SearchCriterion productCriterion = belongsTo(OrderFields.PRODUCT, product);
        List<Entity> quantitiesSumProjectionResults = findBelongingOrders(masterOrder, quantitiesSumProjection, productCriterion,
                SearchOrders.desc(QUANTITIES_SUM_ALIAS));
        for (Entity entity : quantitiesSumProjectionResults) {
            return entity.getDecimalField(QUANTITIES_SUM_ALIAS);
        }
        return BigDecimal.ZERO;
    }

    public Collection<String> findBelongingOrderNumbers(final Entity masterOrder, final SearchCriterion searchCriteria) {
        List<Entity> ordersProjection = findBelongingOrders(masterOrder, ORDER_NUMBER_PROJECTION, searchCriteria, null);
        return EntityUtils.getFieldsView(ordersProjection, OrderFields.NUMBER);
    }

    public List<Entity> findBelongingOrders(final Entity masterOrder, final SearchProjection projection,
            final SearchCriterion additionalCriteria, final SearchOrder searchOrder) {
        SearchCriteriaBuilder scb = getOrderDD().find();
        scb.add(belongsTo(OrderFieldsMO.MASTER_ORDER, masterOrder));
        if (additionalCriteria != null) {
            scb.add(additionalCriteria);
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
