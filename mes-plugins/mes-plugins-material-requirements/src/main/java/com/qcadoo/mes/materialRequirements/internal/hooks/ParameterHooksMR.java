package com.qcadoo.mes.materialRequirements.internal.hooks;

import static com.qcadoo.mes.materialRequirements.internal.constants.InputProductsRequiredForType.START_ORDER;
import static com.qcadoo.mes.materialRequirements.internal.constants.ParameterFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialRequirements.internal.MaterialRequirementService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterHooksMR {

    @Autowired
    private MaterialRequirementService materialRequirementService;

    public final boolean checkIfInputProductsRequiredForTypeIsSelected(final DataDefinition parameterDD, final Entity parameter) {
        return materialRequirementService.checkIfInputProductsRequiredForTypeIsSelected(parameterDD, parameter,
                INPUT_PRODUCTS_REQUIRED_FOR_TYPE, "basic.parameter.message.inputProductsRequiredForTypeIsNotSelected");
    }

    public void setInputProductsRequiredForTypeDefaultValue(final DataDefinition parameterDD, final Entity parameter) {
        materialRequirementService.setInputProductsRequiredForTypeDefaultValue(parameter, INPUT_PRODUCTS_REQUIRED_FOR_TYPE,
                START_ORDER.getStringValue());
    }

}
