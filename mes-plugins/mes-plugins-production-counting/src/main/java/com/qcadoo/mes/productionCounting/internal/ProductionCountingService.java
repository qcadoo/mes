package com.qcadoo.mes.productionCounting.internal;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class ProductionCountingService {

    public void setDefaultValue(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent registerQuantityInProduct = (FieldComponent) viewDefinitionState
                .getComponentByReference("registerQuantityInProduct");
        FieldComponent registerQuantityOutProduct = (FieldComponent) viewDefinitionState
                .getComponentByReference("registerQuantityOutProduct");
        FieldComponent registerProductionTime = (FieldComponent) viewDefinitionState
                .getComponentByReference("registerProductionTime");

        registerQuantityInProduct.setRequired(true);
        registerQuantityInProduct.setFieldValue(true);
        registerQuantityOutProduct.setRequired(true);
        registerQuantityOutProduct.setFieldValue(true);
        registerProductionTime.setRequired(true);
        registerProductionTime.setFieldValue(true);

    }
}
