package com.qcadoo.mes.materialFlowResources.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.constants.ProductsToUpdate;
import com.qcadoo.mes.materialFlowResources.costNorms.CostNormsService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class CostNormsGeneratorListeners {

    @Autowired
    private CostNormsService costNormsService;

    private static final String L_FORM = "form";

    private static final String L_PRODUCTS = "products";

    public void updateCostNorms(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {

        costNormsService.updateCostNormsForProductsFromWarehouses(Lists.newArrayList(), Lists.newArrayList());
    }

    public void toggleProductsGrid(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {

        Entity generator = getFormEntity(view);
        boolean gridVisible = ProductsToUpdate.of(generator).compareTo(ProductsToUpdate.SELECTED) == 0;
        GridComponent grid = (GridComponent) view.getComponentByReference(L_PRODUCTS);
        grid.setVisible(gridVisible);
    }

    private Entity getFormEntity(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        return form.getPersistedEntityWithIncludedFormValues();
    }
}
