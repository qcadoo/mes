package com.qcadoo.mes.productionCounting.states.constants;

import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_PRODUCTION_RECORD_STATE_CHANGE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PLUGIN_IDENTIFIER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.AbstractStateChangeDescriber;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

@Service
public class ProductionRecordStateChangeDescriber extends AbstractStateChangeDescriber {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public DataDefinition getDataDefinition() {
        return dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_PRODUCTION_RECORD_STATE_CHANGE);
    }

    @Override
    public String getOwnerFieldName() {
        return ProductionRecordStateChangeFields.PRODUCTION_RECORD;
    }

    @Override
    public StateEnum parseStateEnum(final String stringValue) {
        return ProductionRecordState.parseString(stringValue);
    }

}
