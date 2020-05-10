package com.qcadoo.mes.productionCounting.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.AnomalyFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class AnomalyListListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void completeWithoutIssue(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        for (Long anomalyId : gridComponent.getSelectedEntitiesIds()) {
            DataDefinition anomalyDD = getAnomalyDD();
            Entity anomaly = anomalyDD.get(anomalyId);

            anomaly.setField(AnomalyFields.STATE, AnomalyFields.State.COMPLETED);
            anomaly.setField(AnomalyFields.ISSUED, false);
            anomalyDD.save(anomaly);
        }
    }

    private DataDefinition getAnomalyDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_ANOMALY);
    }
}
