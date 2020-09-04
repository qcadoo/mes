package com.qcadoo.mes.ordersForSubproductsGeneration.hooks;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import org.springframework.stereotype.Service;

@Service
public class ParametersHooksOFSPG {

    public final void onBeforeRender(final ViewDefinitionState view) {
        CheckBoxComponent automaticallyGenerateOrdersForComponents = (CheckBoxComponent) view
                .getComponentByReference("automaticallyGenerateOrdersForComponents");
        CheckBoxComponent ordersGeneratedByCoverage = (CheckBoxComponent) view
                .getComponentByReference("ordersGeneratedByCoverage");

        ordersGeneratedByCoverage.setEnabled(automaticallyGenerateOrdersForComponents.isChecked());
    }

    public void onChangeAutomaticallyGenerate(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        CheckBoxComponent automaticallyGenerateOrdersForComponents = (CheckBoxComponent) view
                .getComponentByReference("automaticallyGenerateOrdersForComponents");
        CheckBoxComponent ordersGeneratedByCoverage = (CheckBoxComponent) view
                .getComponentByReference("ordersGeneratedByCoverage");

        if (!automaticallyGenerateOrdersForComponents.isChecked()) {
            ordersGeneratedByCoverage.setChecked(false);
        }
    }
}
