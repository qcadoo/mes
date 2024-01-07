package com.qcadoo.mes.technologies.hooks;

import com.qcadoo.mes.technologies.constants.ParameterFieldsT;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParameterHooksT {

    public boolean validatesWith(final DataDefinition parameterDD, final Entity parameter) {
        return checkIfAttributesExists(parameterDD, parameter);
    }

    private boolean checkIfAttributesExists(final DataDefinition parameterDD, final Entity parameter) {
        boolean dimensionControlOfProducts = parameter.getBooleanField(ParameterFieldsT.DIMENSION_CONTROL_OF_PRODUCTS);
        List<Entity> dimensionControlAttributes = parameter.getHasManyField(ParameterFieldsT.DIMENSION_CONTROL_ATTRIBUTES);

        if (dimensionControlOfProducts && dimensionControlAttributes.isEmpty()) {
            parameter.addGlobalError("basic.parameter.dimensionControlOfProducts.error.areEmpty");

            return false;
        }

        return true;
    }

}
