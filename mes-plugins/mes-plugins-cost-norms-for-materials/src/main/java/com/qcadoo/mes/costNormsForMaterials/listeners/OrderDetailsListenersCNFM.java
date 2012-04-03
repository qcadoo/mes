package com.qcadoo.mes.costNormsForMaterials.listeners;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.costNormsForProduct.CostNormsForProductService;
import com.qcadoo.mes.costNormsForProduct.constants.CostNormsForProductConstants;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OrderDetailsListenersCNFM {

    private static final String L_COST_FOR_NUMBER_UNIT = "costForNumberUnit";

    @Autowired
    private CostNormsForProductService costNormsForProductService;

    public void fillUnitFieldInOrder(final ViewDefinitionState viewDefinitionState) {
        costNormsForProductService.fillUnitField(viewDefinitionState, L_COST_FOR_NUMBER_UNIT, false);
    }

    public void fillCurrencyFieldsInOrder(final ViewDefinitionState viewDefinitionState) {
        costNormsForProductService.fillCurrencyFields(viewDefinitionState, CostNormsForProductConstants.CURRENCY_FIELDS_ORDER);
    }

    public final void showInputProductsCostInOrder(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        Long orderId = (Long) componentState.getFieldValue();

        if (orderId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("order.id", orderId);

        String url = "../page/costNormsForMaterials/costNormsForMaterialsInOrderList.html";
        viewState.redirectTo(url, false, true, parameters);
    }
}
