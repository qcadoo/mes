package com.qcadoo.mes.productFlowThruDivision.listeners;

import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyProductionLineFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductionLineAddMultiListeners {

    private static final String L_GENERATED = "generated";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void addProductionLines(final ViewDefinitionState view, final ComponentState state, final String[] args) throws JSONException {
        GridComponent productionLineGrid = (GridComponent) view.getComponentByReference("productionLineGrid");
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        Set<Long> selectedEntities = productionLineGrid.getSelectedEntitiesIds();

        if (selectedEntities.isEmpty()) {
            generated.setChecked(false);

            view.addMessage("productFlowThruDivision.productionLineAddMulti.noSelectedProductionLines", ComponentState.MessageType.INFO);

            return;
        }

        tryCreateProductionLines(view, selectedEntities);

        generated.setChecked(true);
    }

    public void tryCreateProductionLines(ViewDefinitionState view, Set<Long> selectedEntities) throws JSONException {
        JSONObject context = view.getJsonContext();

        Entity technology = getTechnologyDD().get(context.getLong("window.mainTab.form.gridLayout.technologyId"));

        List<String> errorNumbers = Lists.newArrayList();

        for (Long productionLineId : selectedEntities) {
            Entity productionLine = getProductionLineDD().get(productionLineId);

            Entity newProductionLine = createProductionLine(technology, productionLine);
            if (!newProductionLine.isValid()) {
                errorNumbers.add(productionLine.getStringField(ProductionLineFields.NUMBER));
            }
        }

        if (!errorNumbers.isEmpty()) {
            view.addMessage("productFlowThruDivision.productionLineAddMulti.errorForProductionLine", ComponentState.MessageType.INFO,
                    String.join(", ", errorNumbers));
        }
    }

    private Entity createProductionLine(final Entity technology, final Entity productionLine) {
        DataDefinition technologyProductionLineDD = getTechnologyProductionLineDD();

        Entity newProductionLine = technologyProductionLineDD.create();

        newProductionLine.setField(TechnologyProductionLineFields.TECHNOLOGY, technology);
        newProductionLine.setField(TechnologyProductionLineFields.PRODUCTION_LINE, productionLine);
        newProductionLine.setField(TechnologyProductionLineFields.MASTER, false);

        return technologyProductionLineDD.save(newProductionLine);
    }


    private DataDefinition getTechnologyProductionLineDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_TECHNOLOGY_PRODUCTION_LINE);
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    private DataDefinition getProductionLineDD() {
        return dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE);
    }

}
