package com.qcadoo.mes.productionTimeNorms.listeners;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyOperationComponentDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private BigDecimal getOutputProductQuantity(final Entity technologyOperationComponent) {
        List<Entity> outputProducts = technologyOperationComponent.getHasManyField("operationProductOutComponents");

        Entity parent = technologyOperationComponent.getBelongsToField("parent");

        if (parent == null) {
            for (Entity outputProduct : outputProducts) {
                if (outputProduct.getField("product").equals(
                        technologyOperationComponent.getBelongsToField("technology").getBelongsToField("product"))) {
                    return outputProduct.getDecimalField("quantity");
                }
            }
        } else {
            List<Entity> parentInputProducts = parent.getHasManyField("operationProductInComponents");
            for (Entity outputProduct : outputProducts) {
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
        Entity existingTOC = technologyOperationComponent.getDataDefinition().get(technologyOperationComponent.getId());
        Entity technology = existingTOC.getBelongsToField("technology");
        List<Entity> operationComponents = technology.getTreeField("operationComponents");

        for (Entity operationComponent : operationComponents) {
            BigDecimal produced = getOutputProductQuantity(operationComponent);
            BigDecimal producedInOneCycle = operationComponent.getDecimalField("productionInOneCycle");
            if (!(producedInOneCycle.equals(produced))) {
                productionInOneCycle.addMessage("Normy czasowe operacji wymagają korekty", MessageType.INFO);
            }
        }
    }

}
