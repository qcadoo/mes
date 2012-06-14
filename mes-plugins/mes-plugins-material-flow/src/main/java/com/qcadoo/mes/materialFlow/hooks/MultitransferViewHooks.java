package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Component
public class MultitransferViewHooks {

    private static final List<String> COMPONENTS = Arrays.asList(TYPE, TIME);

    public void makeAllFieldsRequired(final ViewDefinitionState view) {
        for (String componentRef : COMPONENTS) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(componentRef);
            component.setRequired(true);
            component.requestComponentUpdateState();
        }
    }
}
