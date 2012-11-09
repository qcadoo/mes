package com.qcadoo.mes.materialRequirements.internal.hooks;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialRequirements.internal.InputProductsRequiredForType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderDetailsHooksMR {

    private static final String L_FORM = "form";

    @Autowired
    private ParameterService parameterService;

    public void setInputProductsRequiredForTypeFromDefaultParameter(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() != null) {
            return;
        }
        FieldComponent inputProductsRequiredForTypeField = (FieldComponent) view
                .getComponentByReference("inputProductsRequiredForType");
        Entity parameter = parameterService.getParameter();
        String inputProductsRequiredForType = parameter.getStringField("inputProductsRequiredForType");
        if (!StringUtils.isEmpty(inputProductsRequiredForType)) {
            inputProductsRequiredForTypeField.setFieldValue(inputProductsRequiredForType);
        } else {
            inputProductsRequiredForTypeField.setFieldValue(InputProductsRequiredForType.START_ORDER.getStringValue());
        }
        inputProductsRequiredForTypeField.requestComponentUpdateState();
    }
}
