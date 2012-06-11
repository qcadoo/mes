package com.qcadoo.mes.materialFlow.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;

@Component
public class TransformationListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MaterialFlowService materialFlowService;

    public void fillProductionAndConsumption(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FieldComponent operationComponent = (FieldComponent) view.getComponentByReference("operation");

        Object operationComponentValue = operationComponent.getFieldValue();

        if (operationComponentValue == null) {
            return;
        }

        Entity operation = getOperationFromId((Long) operationComponentValue);

        AwesomeDynamicListComponent consumption = (AwesomeDynamicListComponent) view.getComponentByReference("consumption");
        List<Entity> consumptionComponents = getProductQuantityComponents(operation.getHasManyField("productInComponents"));
        AwesomeDynamicListComponent production = (AwesomeDynamicListComponent) view.getComponentByReference("production");
        List<Entity> productionComponents = getProductQuantityComponents(operation.getHasManyField("productOutComponents"));

        if (!consumptionComponents.isEmpty()) {
            consumption.setFieldValue(consumptionComponents);
        }

        if (!productionComponents.isEmpty()) {
            production.setFieldValue(productionComponents);
        }

        // TODO mici, consider adding those messages after fixing: SC#QCADOO-243
        // if (modified) {
        // view.getComponentByReference("form").addMessage("materialFlow.transformations.productsLoaded.success",
        // MessageType.SUCCESS);
        // } else {
        // view.getComponentByReference("form").addMessage("materialFlow.transformations.productsLoaded.failure",
        // MessageType.FAILURE);
        // }

    }

    private Entity getOperationFromId(final long id) {
        return dataDefinitionService.get("technologies", "operation").get(id);
    }

    private List<Entity> getProductQuantityComponents(final List<Entity> productComponents) {
        List<Entity> productQuantityComponents = Lists.newArrayList();

        DataDefinition dd = dataDefinitionService.get("materialFlow", "transfer");

        for (Entity productComponent : productComponents) {
            Entity productQty = dd.create();
            Entity product = productComponent.getBelongsToField("product");
            productQty.setField("product", product);
            String number = materialFlowService.generateNumberFromProduct(product, "transfer");
            productQty.setField("number", number);
            productQty.setField("quantity", null);
            productQuantityComponents.add(productQty);
        }

        return productQuantityComponents;
    }
}
