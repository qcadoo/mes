/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.materialFlow.listeners;

import static com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants.MODEL_TRANSFER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.NUMBER;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.QUANTITY;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.OPERATION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_PRODUCTION;
import static com.qcadoo.mes.technologies.constants.OperationFields.PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.OperationFields.PRODUCT_OUT_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.ProductComponentFields.PRODUCT;

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
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Component
public class TransformationsListeners {

    @Autowired
    private MaterialFlowService materialFlowService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void fillTransferNumbersInTransfersConsumption(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        fillTransferNumbersInADL(view, TRANSFERS_CONSUMPTION);
    }

    public void fillTransferNumbersInTransfersProduction(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        fillTransferNumbersInADL(view, TRANSFERS_PRODUCTION);
    }

    private void fillTransferNumbersInADL(final ViewDefinitionState view, final String transfersName) {
        AwesomeDynamicListComponent transfersConsumption = (AwesomeDynamicListComponent) view
                .getComponentByReference(TRANSFERS_CONSUMPTION);
        AwesomeDynamicListComponent transfersProduction = (AwesomeDynamicListComponent) view
                .getComponentByReference(TRANSFERS_PRODUCTION);

        List<FormComponent> consumptionComponents = transfersConsumption.getFormComponents();
        List<FormComponent> productionComponents = transfersProduction.getFormComponents();

        List<FormComponent> formComponents = null;

        if (TRANSFERS_CONSUMPTION.equals(transfersName)) {
            formComponents = consumptionComponents;
        } else if (TRANSFERS_PRODUCTION.equals(transfersName)) {
            formComponents = productionComponents;
        }

        if (formComponents != null) {
            for (FormComponent formComponent : formComponents) {
                Entity transfer = formComponent.getEntity();
                Entity product = transfer.getBelongsToField(PRODUCT);
                String number = transfer.getStringField(NUMBER);

                if ((product != null) && ((number == null) || !number.contains(product.getStringField(NUMBER)))) {
                    number = generateNumberForTransfer(product, MODEL_TRANSFER, consumptionComponents, productionComponents);

                    transfer.setField(NUMBER, number);
                }

                formComponent.setEntity(transfer);
            }
        }
    }

    public void fillProductionAndConsumption(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        fillProductionAndConsumption(view);
    }

    public void fillProductionAndConsumption(final ViewDefinitionState view) {
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

        List<Entity> consumptionComponents = null;
        List<Entity> productionComponents = Lists.newArrayList();

        consumptionComponents = getTransfersFromProducts(operation.getHasManyField(PRODUCT_IN_COMPONENTS), productionComponents);
        productionComponents = getTransfersFromProducts(operation.getHasManyField(PRODUCT_OUT_COMPONENTS), consumptionComponents);

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

    private List<Entity> getTransfersFromProducts(final List<Entity> productComponents, final List<Entity> transfers) {
        List<Entity> transfersFromProducts = Lists.newArrayList();

        DataDefinition dd = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_TRANSFER);

        for (Entity productComponent : productComponents) {
            Entity product = productComponent.getBelongsToField(PRODUCT);

            String number = generateNumberForTransfer(product, MaterialFlowConstants.MODEL_TRANSFER, transfers);

            Entity transfer = dd.create();

            transfer.setField(PRODUCT, product);
            transfer.setField(NUMBER, number);
            transfer.setField(QUANTITY, null);

            transfersFromProducts.add(transfer);
        }

        return transfersFromProducts;
    }

    private String generateNumberForTransfer(final Entity product, final String model, final List<Entity> transfers) {
        String number = "";

        if ((product != null) && (transfers != null)) {
            String generatedNumber = numberGeneratorService.generateNumber(MaterialFlowConstants.PLUGIN_IDENTIFIER, model, 3);

            String prefix = product.getStringField(NUMBER);

            number = prefix + "-" + generatedNumber;

            Long parsedNumber = Long.parseLong(generatedNumber);

            while (numberAlreadyExistsOnList(transfers, number) || materialFlowService.numberAlreadyExist(model, number)) {
                parsedNumber++;

                number = prefix + "-" + String.format("%03d", parsedNumber);
            }
        }

        return number;
    }

    private String generateNumberForTransfer(final Entity product, final String model,
            final List<FormComponent> consumptionComponents, final List<FormComponent> productionComponents) {
        String number = "";

        if ((product != null) && (consumptionComponents != null) && (productionComponents != null)) {
            String generatedNumber = numberGeneratorService.generateNumber(MaterialFlowConstants.PLUGIN_IDENTIFIER, model, 3);

            String prefix = product.getStringField(NUMBER);

            number = prefix + "-" + generatedNumber;

            Long parsedNumber = Long.parseLong(generatedNumber);

            while (numberAlreadyExistsOnADL(consumptionComponents, number)
                    || numberAlreadyExistsOnADL(productionComponents, number)
                    || materialFlowService.numberAlreadyExist(model, number)) {
                parsedNumber++;

                number = prefix + "-" + String.format("%03d", parsedNumber);
            }
        }

        return number;
    }

    private boolean numberAlreadyExistsOnList(final List<Entity> transfers, final String number) {
        if (transfers == null) {
            return false;
        }

        for (Entity transfer : transfers) {
            if (number.equals(transfer.getStringField(NUMBER))) {
                return true;
            }
        }

        return false;
    }

    private boolean numberAlreadyExistsOnADL(final List<FormComponent> formComponents, final String number) {
        if (formComponents == null) {
            return false;
        }

        for (FormComponent formComponent : formComponents) {
            Entity transfer = formComponent.getEntity();

            if ((transfer != null) && number.equals(transfer.getStringField(NUMBER))) {
                return true;
            }
        }

        return false;
    }

    private Entity getOperationFromId(final long id) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION).get(id);
    }

}
