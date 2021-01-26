package com.qcadoo.mes.costCalculation.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.MaterialCostsUsed;
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TechnologiesListListenersCC {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public final void createCostCalculation(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        List<Entity> selectedEntities = grid.getSelectedEntities();

        if (!selectedEntities.isEmpty()) {
            DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY);
            List<Entity> technologies = selectedEntities.stream().map(e -> technologyDD.get(e.getId()))
                    .collect(Collectors.toList());
            DataDefinition costCalculationDD = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER,
                    CostCalculationConstants.MODEL_COST_CALCULATION);
            Entity costCalculation = costCalculationDD.create();
            costCalculation.setField(CostCalculationFields.TECHNOLOGIES, technologies);
            costCalculation.setField(CostCalculationFields.QUANTITY, BigDecimal.ONE);
            costCalculation.setField(CostCalculationFields.MATERIAL_COSTS_USED, MaterialCostsUsed.NOMINAL.getStringValue());
            costCalculation.setField(CostCalculationFields.SOURCE_OF_OPERATION_COSTS,
                    SourceOfOperationCosts.TECHNOLOGY_OPERATION.getStringValue());
            costCalculation.setField(CostCalculationFields.NUMBER, numberGeneratorService
                    .generateNumber(CostCalculationConstants.PLUGIN_IDENTIFIER, CostCalculationConstants.MODEL_COST_CALCULATION));
            costCalculation = costCalculationDD.save(costCalculation);
            String url = "../page/costCalculation/costCalculationDetails.html";
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.id", costCalculation.getId());

            view.redirectTo(url, false, true, parameters);
        }
    }
}
