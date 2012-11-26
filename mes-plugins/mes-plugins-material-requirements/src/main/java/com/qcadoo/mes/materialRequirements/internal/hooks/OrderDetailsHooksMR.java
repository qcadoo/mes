package com.qcadoo.mes.materialRequirements.internal.hooks;

import static com.qcadoo.mes.materialRequirements.internal.constants.OrderFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderDetailsHooksMR {

    private static final String L_FORM = "form";

    @Autowired
    private ParameterService parameterService;

    public void setInputProductsRequiredForTypeFromParameters(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (orderForm.getEntityId() != null) {
            return;
        }

        FieldComponent inputProductsRequiredForTypeField = (FieldComponent) view
                .getComponentByReference(INPUT_PRODUCTS_REQUIRED_FOR_TYPE);

        String inputProductsRequiredForType = (String) inputProductsRequiredForTypeField.getFieldValue();

        if (StringUtils.isEmpty(inputProductsRequiredForType)) {
            inputProductsRequiredForTypeField.setFieldValue(getInputProductsRequiredForType());
        }

        inputProductsRequiredForTypeField.requestComponentUpdateState();
    }

    private String getInputProductsRequiredForType() {
        return parameterService.getParameter().getStringField(INPUT_PRODUCTS_REQUIRED_FOR_TYPE);
    }

}
