package com.qcadoo.mes.materialFlowResources.listeners;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.constants.CostNormsGeneratorFields;
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

    private static final String L_LOCATION = "location";

    public void updateCostNorms(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {

        Entity generator = getFormEntity(view);
        boolean allProducts = ProductsToUpdate.of(generator).compareTo(ProductsToUpdate.ALL) == 0;
        List<Entity> products = Lists.newArrayList();
        if (!allProducts) {
            products = generator.getHasManyField(CostNormsGeneratorFields.PRODUCTS);
            if (products.isEmpty()) {
                view.addMessage("materialFlowResources.info.costNormsNotUpdated", ComponentState.MessageType.INFO);
                return;
            }
        }
        List<Entity> warehouses = Lists.newArrayList();
        String costSource = generator.getStringField(CostNormsGeneratorFields.COSTS_SOURCE);
        if ("01mes".equals(costSource)) {
            warehouses = generator.getHasManyField(CostNormsGeneratorFields.WAREHOUSES).stream()
                    .map(warehouse -> warehouse.getBelongsToField(L_LOCATION)).collect(Collectors.toList());
        }
        costNormsService.updateCostNormsForProductsFromWarehouses(products, warehouses);
        view.addMessage("materialFlowResources.success.costNormsUpdated", ComponentState.MessageType.SUCCESS);
    }

    public void toggleProductsGrid(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {

        Entity generator = getFormEntity(view);
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        boolean gridVisible = ProductsToUpdate.of(generator).compareTo(ProductsToUpdate.SELECTED) == 0;
        GridComponent grid = (GridComponent) view.getComponentByReference(L_PRODUCTS);
        grid.setVisible(gridVisible);
        if (!gridVisible) {
            generator.setField(CostNormsGeneratorFields.PRODUCTS, null);
            form.setEntity(generator);
        }

    }

    private Entity getFormEntity(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        return form.getPersistedEntityWithIncludedFormValues();
    }
}
