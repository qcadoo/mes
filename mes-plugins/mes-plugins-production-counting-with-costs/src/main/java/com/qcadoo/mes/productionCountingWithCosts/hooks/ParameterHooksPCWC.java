package com.qcadoo.mes.productionCountingWithCosts.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCountingWithCosts.constants.ParameterFieldsPCWC;
import com.qcadoo.mes.productionCountingWithCosts.constants.PriceBasedOn;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterHooksPCWC {

    public void onCreate(final DataDefinition dataDefinition, final Entity parameter) {
        parameter.setField(ParameterFieldsPCWC.PRICE_BASED_ON, PriceBasedOn.NOMINAL_PRODUCT_COST.getStringValue());
    }

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity parameter) {
        String priceBasedOn = parameter.getStringField(ParameterFieldsPCWC.PRICE_BASED_ON);
        if (priceBasedOn != null && priceBasedOn.compareTo("") != 0) {
            return true;
        }
        parameter.addError(dataDefinition.getField(ParameterFieldsPCWC.PRICE_BASED_ON), "basic.parameter.priceBasedOn.required");
        return false;
    }

}
