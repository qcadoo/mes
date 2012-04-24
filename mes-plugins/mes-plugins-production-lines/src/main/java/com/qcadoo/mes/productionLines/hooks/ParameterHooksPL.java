package com.qcadoo.mes.productionLines.hooks;

import static com.qcadoo.mes.productionLines.constants.ProductionLinesConstants.PLUGIN_IDENTIFIER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterHooksPL {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void addFieldsForParameter(final DataDefinition dataDefinition, final Entity parameter) {
        Entity productionLines = dataDefinitionService.get(PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE)
                .find().setMaxResults(1).uniqueResult();
        parameter.setField("defaultProductionLine", productionLines);
    }
}
