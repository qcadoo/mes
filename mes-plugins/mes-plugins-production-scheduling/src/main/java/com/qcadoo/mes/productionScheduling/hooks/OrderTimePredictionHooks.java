package com.qcadoo.mes.productionScheduling.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderTimePredictionHooks {

    private static final String L_FORM = "form";

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        if (form.getEntityId() == null && view.isViewAfterRedirect()) {
            CheckBoxComponent includeTpzField = (CheckBoxComponent) view.getComponentByReference(OrderFieldsPS.INCLUDE_TPZ);
            boolean checkIncludeTpzField = parameterService.getParameter().getBooleanField("includeTpzPS");
            includeTpzField.setChecked(checkIncludeTpzField);
            includeTpzField.requestComponentUpdateState();

            CheckBoxComponent includeAdditionalTimeField = (CheckBoxComponent) view.getComponentByReference(OrderFieldsPS.INCLUDE_ADDITIONAL_TIME);
            boolean checkIncludeAdditionalTimeField = parameterService.getParameter().getBooleanField("includeAdditionalTimePS");
            includeAdditionalTimeField.setChecked(checkIncludeAdditionalTimeField);
            includeAdditionalTimeField.requestComponentUpdateState();
        }
    }
}
