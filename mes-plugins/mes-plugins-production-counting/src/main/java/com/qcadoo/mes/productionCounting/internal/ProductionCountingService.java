package com.qcadoo.mes.productionCounting.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProductionCountingService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    public void setDefaultValue(final ViewDefinitionState viewDefinitionState) {
        FieldComponent registerQuantityInProduct = (FieldComponent) viewDefinitionState
                .getComponentByReference("registerQuantityInProduct");
        FieldComponent registerQuantityOutProduct = (FieldComponent) viewDefinitionState
                .getComponentByReference("registerQuantityOutProduct");
        FieldComponent registerProductionTime = (FieldComponent) viewDefinitionState
                .getComponentByReference("registerProductionTime");

        Entity parameter = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).get(
                (long) 1);

        if (parameter == null || parameter.getField("registerQuantityInProduct") == null) {
            registerQuantityInProduct.setFieldValue(true);
            registerQuantityInProduct.requestComponentUpdateState();
        }
        if (parameter == null || parameter.getField("registerQuantityOutProduct") == null) {
            registerQuantityOutProduct.setFieldValue(true);
            registerQuantityOutProduct.requestComponentUpdateState();
        }
        if (parameter == null || parameter.getField("registerProductionTime") == null) {
            registerProductionTime.setFieldValue(true);
            registerProductionTime.requestComponentUpdateState();
        }
    }
}
