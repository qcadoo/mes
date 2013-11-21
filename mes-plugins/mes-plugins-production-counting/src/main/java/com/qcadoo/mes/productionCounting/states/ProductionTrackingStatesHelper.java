package com.qcadoo.mes.productionCounting.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.states.aop.ProductionTrackingStateChangeAspect;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateChangeDescriber;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateChangeFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductionTrackingStatesHelper {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private ProductionTrackingStateChangeDescriber stateChangeDescriber;

    @Autowired
    private ProductionTrackingStateChangeAspect productionTrackingStateChangeAspect;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setInitialState(final Entity productionTracking) {
        stateChangeEntityBuilder.buildInitial(stateChangeDescriber, productionTracking, ProductionTrackingState.DRAFT);
    }

    public void resumeStateChange(final StateChangeContext context) {
        context.setStatus(StateChangeStatus.IN_PROGRESS);

        productionTrackingStateChangeAspect.changeState(context);
    }

    public void cancelStateChange(final StateChangeContext context) {
        context.setStatus(StateChangeStatus.FAILURE);

        productionTrackingStateChangeAspect.changeState(context);
    }

    public StateChangeContext findPausedStateTransition(final Entity productionTracking) {
        Entity stateChangeEntity = findPausedStateChangeEntity(productionTracking);

        if (stateChangeEntity == null) {
            return null;
        }

        return stateChangeContextBuilder.build(stateChangeDescriber, stateChangeEntity);
    }

    private Entity findPausedStateChangeEntity(final Entity productionTracking) {
        DataDefinition stateChangeDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_TRACKING_STATE_CHANGE);
        SearchCriteriaBuilder scb = stateChangeDD.find();
        scb.add(SearchRestrictions.belongsTo(ProductionTrackingStateChangeFields.PRODUCTION_TRACKING, productionTracking));
        scb.add(SearchRestrictions.eq(ProductionTrackingStateChangeFields.STATUS, StateChangeStatus.PAUSED.getStringValue()));

        return scb.setMaxResults(1).uniqueResult();
    }

}
