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
        view.getComponentByReference("setTab").setVisible(shouldSetTabBeVisible(view));
    }

    public boolean shouldSetTabBeVisible(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity productionCountingQuantity = form.getPersistedEntityWithIncludedFormValues();

        if (productionCountingQuantity.getId() == null) {
            return false;
        }

        String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
        String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);

        if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)
                && ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(typeOfMaterial)) {

            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            return setTechnologyInComponentsService.isProductASet(product);
        }

        return false;
    }
}
