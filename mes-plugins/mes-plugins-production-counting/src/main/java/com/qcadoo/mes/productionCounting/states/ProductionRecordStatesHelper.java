package com.qcadoo.mes.productionCounting.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.states.aop.ProductionRecordStateChangeAspect;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordState;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateChangeDescriber;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateChangeFields;
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
public class ProductionRecordStatesHelper {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private ProductionRecordStateChangeDescriber stateChangeDescriber;

    @Autowired
    private ProductionRecordStateChangeAspect productionRecordStateChangeAspect;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setInitialState(final Entity productionRecord) {
        stateChangeEntityBuilder.buildInitial(stateChangeDescriber, productionRecord, ProductionRecordState.DRAFT);
    }

    public void resumeStateChange(final StateChangeContext context) {
        context.setStatus(StateChangeStatus.IN_PROGRESS);
        productionRecordStateChangeAspect.changeState(context);
    }

    public void cancelStateChange(final StateChangeContext context) {
        context.setStatus(StateChangeStatus.FAILURE);
        productionRecordStateChangeAspect.changeState(context);
    }

    public StateChangeContext findPausedStateTransition(final Entity productionRecord) {
        Entity stateChangeEntity = findPausedStateChangeEntity(productionRecord);
        if (stateChangeEntity == null) {
            return null;
        }
        return stateChangeContextBuilder.build(stateChangeDescriber, stateChangeEntity);
    }

    private Entity findPausedStateChangeEntity(final Entity productionRecord) {
        DataDefinition stateChangeDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_RECORD_STATE_CHANGE);
        SearchCriteriaBuilder scb = stateChangeDD.find();
        scb.add(SearchRestrictions.belongsTo(ProductionRecordStateChangeFields.PRODUCTION_RECORD, productionRecord));
        scb.add(SearchRestrictions.eq(ProductionRecordStateChangeFields.STATUS, StateChangeStatus.PAUSED.getStringValue()));
        return scb.setMaxResults(1).uniqueResult();
    }

}
