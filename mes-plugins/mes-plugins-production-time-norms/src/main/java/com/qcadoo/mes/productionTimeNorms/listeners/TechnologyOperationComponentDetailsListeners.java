package com.qcadoo.mes.productionTimeNorms.listeners;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class TechnologyOperationComponentDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private BigDecimal getOutputProductQuantity(final Entity technologyOperationComponent) {
        List<Entity> outputProducts = technologyOperationComponent.getHasManyField("operationProductOutComponents");
        Entity parent = technologyOperationComponent.getBelongsToField("parent");
        if (parent == null) {
            throw new IllegalStateException("parent not found");
        }
        List<Entity> parentInputProducts = parent.getHasManyField("operationProductInComponents");

        for (Entity outputProduct : outputProducts) {
            if (outputProduct.equals(technologyOperationComponent.getBelongsToField("technology").getBelongsToField("product"))) {
                return outputProduct.getDecimalField("quantity");
            } else {
                for (Entity parentInputProduct : parentInputProducts) {
                    if (parentInputProduct.getField("product").equals(outputProduct.getField("product"))) {
                        return outputProduct.getDecimalField("quantity");
                    }
                }
            }
        }
        return null;

    }

    public void checkIfDifferentQuantity(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent productionInOneCycle = (FieldComponent) viewDefinitionState
                .getComponentByReference("productionInOneCycle");
        Entity technologyOperationComponent = form.getEntity();
        Entity parent = technologyOperationComponent.getBelongsToField("parent");
        List<Entity> operationComponents = parent.getHasManyField("children");
        for (Entity operationComponent : operationComponents) {
            BigDecimal produced = getOutputProductQuantity(operationComponent);
            BigDecimal producedInOneCycle = operationComponent.getDecimalField("producedInOneCycle");
            if (!produced.equals(producedInOneCycle)) {
                productionInOneCycle.addMessage("bład", null);
            }
        }
    }

}
