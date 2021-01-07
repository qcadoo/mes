package com.qcadoo.mes.ordersForSubproductsGeneration.costCalculation;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CostCalculationDetailsListenersOFSPG {

    public void saveComponentsNominalCosts(final ViewDefinitionState view, final ComponentState state, final String[] args){
        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity costsEntity = formComponent.getEntity();
        costsEntity = costsEntity.getDataDefinition().get(costsEntity.getId());
        List<Entity> componentCosts = costsEntity.getHasManyField("componentCosts");
        try {
            saveComponentsNominalCosts(componentCosts);
            view.addMessage("costCalculation.messages.success.saveCostsSuccess", ComponentState.MessageType.SUCCESS);
        } catch (Exception ex) {
            view.addMessage("costCalculation.messages.success.saveCostsFailure", ComponentState.MessageType.FAILURE, false);
        }
    }

    @Transactional
    private void saveComponentsNominalCosts(List<Entity> componentCosts) {
        for(Entity component : componentCosts){
            Entity product = component.getBelongsToField("product");
            product = product.getDataDefinition().get(product.getId());
            product.setField("nominalCost", component.getDecimalField("pricePerUnit"));
            product = product.getDataDefinition().save(product);
            if(!product.isValid()){
                throw new IllegalStateException();
            }
        }
    }
}
