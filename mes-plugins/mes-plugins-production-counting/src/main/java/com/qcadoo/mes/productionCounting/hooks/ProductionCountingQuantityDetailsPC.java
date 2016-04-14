package com.qcadoo.mes.productionCounting.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.productionCounting.SetTechnologyInComponentsService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionCountingQuantityDetailsPC {

    private static final String L_FORM = "form";

    @Autowired
    SetTechnologyInComponentsService setTechnologyInComponentsService;

    public void beforeRender(final ViewDefinitionState view) {
        toggleSetTab(view);
    }

    public void toggleSetTab(final ViewDefinitionState view) {
        boolean visible = false;

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity productionCountingQuantity = form.getPersistedEntityWithIncludedFormValues();

        String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
        String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);

        if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)
                && ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(typeOfMaterial)) {

            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            visible = setTechnologyInComponentsService.isProductASet(product);
        }

        view.getComponentByReference("setTab").setVisible(visible);
    }
}
