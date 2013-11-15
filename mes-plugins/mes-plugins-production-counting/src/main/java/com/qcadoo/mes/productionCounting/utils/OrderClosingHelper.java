package com.qcadoo.mes.productionCounting.utils;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
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

    public boolean orderShouldBeClosed(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        if (order == null) {
            return false;
        }
        Boolean autoCloseOrder = order.getBooleanField(OrderFieldsPC.AUTO_CLOSE_ORDER);
        Boolean isLastRecord = productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING);
        TypeOfProductionRecording recType = TypeOfProductionRecording.parseString(order
                .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));

        return isLastRecord
                && autoCloseOrder
                && (!(TypeOfProductionRecording.FOR_EACH.equals(recType)) || eachOperationHasLastRecords(order,
                        productionTracking));
    }

    private boolean eachOperationHasLastRecords(final Entity order, final Entity productionTracking) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        int numOfOrderOperations = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS).size();
        Set<Long> matchingRecordIds = getIdsOfAcceptedLastRecords(productionTracking.getDataDefinition(), order.getId());
        int numOfMatchingRecords = matchingRecordIds.size();
        long recordId = productionTracking.getId();

        return (numOfMatchingRecords >= numOfOrderOperations)
                || (numOfMatchingRecords == (numOfOrderOperations - 1) && !matchingRecordIds.contains(recordId));
    }

    private Set<Long> getIdsOfAcceptedLastRecords(final DataDefinition productionTrackingDD, final Long orderId) {
        SearchCriteriaBuilder scb = productionTrackingDD.find();
        scb.add(SearchRestrictions.eq(ProductionTrackingFields.STATE, ProductionTrackingStateStringValues.ACCEPTED));
        scb.add(SearchRestrictions.eq(ProductionTrackingFields.LAST_TRACKING, true));
        scb.add(SearchRestrictions.eq(ProductionTrackingFields.ORDER + ".id", orderId));
        scb.setProjection(SearchProjections.alias(SearchProjections.id(), "id"));
        List<Entity> result = scb.list().getEntities();
        return Sets.newHashSet(Collections2.transform(result, FUNC_EXTRACT_PROJECTION_ID));
    }

}
