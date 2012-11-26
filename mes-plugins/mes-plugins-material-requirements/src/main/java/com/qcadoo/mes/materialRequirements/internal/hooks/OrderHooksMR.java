package com.qcadoo.mes.materialRequirements.internal.hooks;

import static com.qcadoo.mes.materialRequirements.internal.constants.OrderFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialRequirements.internal.MaterialRequirementService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OrderHooksMR {

    @Autowired
    private MaterialRequirementService materialRequirementService;

    @Autowired
    private ParameterService parameterService;

    public final boolean checkIfInputProductsRequiredForTypeIsSelected(final DataDefinition orderDD, final Entity order) {
        return materialRequirementService.checkIfInputProductsRequiredForTypeIsSelected(orderDD, order,
                INPUT_PRODUCTS_REQUIRED_FOR_TYPE, "orders.order.message.inputProductsRequiredForTypeIsNotSelected");
    }

    public void setInputProductsRequiredForTypeDefaultValue(final DataDefinition orderDD, final Entity order) {
        materialRequirementService.setInputProductsRequiredForTypeDefaultValue(order, INPUT_PRODUCTS_REQUIRED_FOR_TYPE,
                getInputProductsRequiredForType());
    }

    private String getInputProductsRequiredForType() {
        return parameterService.getParameter().getStringField(INPUT_PRODUCTS_REQUIRED_FOR_TYPE);
    }

}
