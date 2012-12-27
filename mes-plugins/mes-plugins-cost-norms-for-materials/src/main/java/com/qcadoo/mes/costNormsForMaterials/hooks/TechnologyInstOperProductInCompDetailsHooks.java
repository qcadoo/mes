package com.qcadoo.mes.costNormsForMaterials.hooks;

import static com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants.CURRENCY_FIELDS_ORDER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForProduct.CostNormsForProductService;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class TechnologyInstOperProductInCompDetailsHooks {

    @Autowired
    private CostNormsForProductService costNormsForProductService;

    private static final String L_COST_FOR_NUMBER_UNIT = "costForNumberUnit";

    public void fillUnitField(final ViewDefinitionState viewDefinitionState) {
        costNormsForProductService.fillUnitField(viewDefinitionState, L_COST_FOR_NUMBER_UNIT, true);
    }

    public void fillCurrencyFields(final ViewDefinitionState viewDefinitionState) {
        costNormsForProductService.fillCurrencyFields(viewDefinitionState, CURRENCY_FIELDS_ORDER);
    }
}
