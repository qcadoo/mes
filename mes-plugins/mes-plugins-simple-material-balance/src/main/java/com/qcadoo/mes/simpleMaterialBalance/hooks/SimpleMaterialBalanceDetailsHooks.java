package com.qcadoo.mes.simpleMaterialBalance.hooks;

import static com.qcadoo.mes.simpleMaterialBalance.internal.constants.SimpleMaterialBalanceFields.MRPALGORITHM;
import static com.qcadoo.mes.simpleMaterialBalance.internal.constants.SimpleMaterialBalanceFields.NAME;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.simpleMaterialBalance.internal.constants.SimpleMaterialBalanceFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class SimpleMaterialBalanceDetailsHooks {

    public void disableFieldsWhenGenerated(final ViewDefinitionState view) {
        Boolean enabled = false;
        ComponentState generated = (ComponentState) view.getComponentByReference("generated");
        if (generated == null || generated.getFieldValue() == null || "0".equals(generated.getFieldValue())) {
            enabled = true;
        }
        for (String reference : Arrays.asList(NAME, MRPALGORITHM)) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(reference);
            component.setEnabled(enabled);
        }
        ((GridComponent) view.getComponentByReference(SimpleMaterialBalanceFields.SIMPLE_MATERIAL_BALANCE_LOCATIONS_COMPONENTS))
                .setEnabled(enabled);
        ((GridComponent) view.getComponentByReference(SimpleMaterialBalanceFields.SIMPLE_MATERIAL_BALANCE_LOCATIONS_COMPONENTS))
                .setEditable(enabled);
        ((GridComponent) view.getComponentByReference(SimpleMaterialBalanceFields.SIMPLE_MATERIAL_BALANCE_ORDERS_COMPONENTS))
                .setEnabled(enabled);
        ((GridComponent) view.getComponentByReference(SimpleMaterialBalanceFields.SIMPLE_MATERIAL_BALANCE_ORDERS_COMPONENTS))
                .setEditable(enabled);
    }
}
