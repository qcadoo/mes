package com.qcadoo.mes.productionCounting.utils;

import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.AUTO_CLOSE_ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.LAST_RECORD;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OrderClosingHelper {

    private static final Function<Entity, Long> FUNC_EXTRACT_PROJECTION_ID = new Function<Entity, Long>() {

        @Override
        public Long apply(final Entity from) {
            return (Long) from.getField("id");
        }
    };

    public boolean orderShouldBeClosed(final Entity productionRecord) {
        Entity order = productionRecord.getBelongsToField(ProductionRecordFields.ORDER);
        if (order == null) {
            return false;
        }
        Boolean autoCloseOrder = order.getBooleanField(AUTO_CLOSE_ORDER);
        Boolean isLastRecord = productionRecord.getBooleanField(LAST_RECORD);
        TypeOfProductionRecording recType = TypeOfProductionRecording.parseString(order
                .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));

        return isLastRecord
                && autoCloseOrder
                && (!(TypeOfProductionRecording.FOR_EACH.equals(recType)) || eachOperationHasLastRecords(order, productionRecord));
    }

    private boolean eachOperationHasLastRecords(final Entity order, final Entity productionRecord) {
        int numOfOrderOperations = order.getHasManyField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS).size();
        Set<Long> matchingRecordIds = getIdsOfAcceptedLastRecords(productionRecord.getDataDefinition(), order.getId());
        int numOfMatchingRecords = matchingRecordIds.size();
        long recordId = productionRecord.getId();

        return (numOfMatchingRecords >= numOfOrderOperations)
                || (numOfMatchingRecords == (numOfOrderOperations - 1) && !matchingRecordIds.contains(recordId));
    }

    private Set<Long> getIdsOfAcceptedLastRecords(final DataDefinition productionRecordDD, final Long orderId) {
        SearchCriteriaBuilder scb = productionRecordDD.find();
        scb.add(SearchRestrictions.eq(ProductionRecordFields.STATE, ProductionRecordState.ACCEPTED.getStringValue()));
        scb.add(SearchRestrictions.eq(ProductionRecordFields.LAST_RECORD, true));
        scb.add(SearchRestrictions.eq(ProductionRecordFields.ORDER + ".id", orderId));
        scb.setProjection(SearchProjections.alias(SearchProjections.id(), "id"));
        List<Entity> result = scb.list().getEntities();
        return Sets.newHashSet(Collections2.transform(result, FUNC_EXTRACT_PROJECTION_ID));
    }

}
