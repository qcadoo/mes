package com.qcadoo.mes.materialFlow.listeners;

import static com.qcadoo.mes.materialFlow.constants.ProductQuantityFields.PRODUCT;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.OPERATION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_PRODUCTION;
import static com.qcadoo.mes.technologies.constants.OperationFields.PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.OperationFields.PRODUCT_OUT_COMPONENTS;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Component
public class TransformationsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MaterialFlowService materialFlowService;

    public void fillTransferNumbersInADL(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        fillTransferNumbersInADL(view, TRANSFERS_CONSUMPTION);
        fillTransferNumbersInADL(view, TRANSFERS_PRODUCTION);
    }

    private void fillTransferNumbersInADL(final ViewDefinitionState view, final String adlName) {
        AwesomeDynamicListComponent adlc = (AwesomeDynamicListComponent) view.getComponentByReference(adlName);
        List<FormComponent> formComponents = adlc.getFormComponents();

        for (FormComponent formComponent : formComponents) {
            Entity transfer = formComponent.getEntity();
            String number = transfer.getStringField(NUMBER);
            Entity product = transfer.getBelongsToField(PRODUCT);

            if (product != null) {
                number = materialFlowService.generateNumberFromProduct(product, MaterialFlowConstants.MODEL_TRANSFER);

                transfer.setField(NUMBER, number);
            }
            formComponent.setEntity(transfer);
        }
    }

    public void fillProductionAndConsumption(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FieldComponent operationComponent = (FieldComponent) view.getComponentByReference(OPERATION);

        Object operationComponentValue = operationComponent.getFieldValue();

        if (operationComponentValue == null) {
            return;
        }

        Entity operation = getOperationFromId((Long) operationComponentValue);

        AwesomeDynamicListComponent transfersConsumption = (AwesomeDynamicListComponent) view
                .getComponentByReference(TRANSFERS_CONSUMPTION);

        AwesomeDynamicListComponent transfersProduction = (AwesomeDynamicListComponent) view
                .getComponentByReference(TRANSFERS_PRODUCTION);

        List<Entity> consumptionComponents = Lists.newArrayList();
        List<Entity> productionComponents = Lists.newArrayList();

        consumptionComponents = getTransfersFromProducts(operation.getHasManyField(PRODUCT_IN_COMPONENTS));
        productionComponents = getTransfersFromProducts(operation.getHasManyField(PRODUCT_OUT_COMPONENTS));

        if (!consumptionComponents.isEmpty()) {
            transfersConsumption.setFieldValue(consumptionComponents);
        }

        if (!productionComponents.isEmpty()) {
            transfersProduction.setFieldValue(productionComponents);
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

    private List<Entity> getTransfersFromProducts(final List<Entity> productComponents) {
        List<Entity> transfersFromProducts = Lists.newArrayList();

        DataDefinition dd = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);

        for (Entity productComponent : productComponents) {
            Entity product = productComponent.getBelongsToField(PRODUCT);

            String number = materialFlowService.generateNumberFromProduct(product, MaterialFlowConstants.MODEL_TRANSFER);

            Entity transfer = dd.create();

            transfer.setField(PRODUCT, product);
            transfer.setField(NUMBER, number);
            transfer.setField(QUANTITY, null);

            transfersFromProducts.add(transfer);
        }

        return transfersFromProducts;
    }

    private Entity getOperationFromId(final long id) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION).get(id);
    }

}
