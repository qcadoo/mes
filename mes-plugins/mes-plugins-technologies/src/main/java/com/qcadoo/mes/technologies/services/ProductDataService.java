/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.technologies.services;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class ProductDataService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void createProductDataInputs(final Entity productData, final List<Entity> operationProductInComponents) {
        operationProductInComponents.forEach(operationProductInComponent -> {
            Entity product = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);

            String name = product.getStringField(ProductFields.NAME);
            String number = product.getStringField(ProductFields.NUMBER);
            BigDecimal quantity = operationProductInComponent.getDecimalField(OperationProductInComponentFields.GIVEN_QUANTITY);
            String unit = operationProductInComponent.getStringField(OperationProductInComponentFields.GIVEN_UNIT);

            if (Objects.isNull(quantity)) {
                quantity = operationProductInComponent.getDecimalField(OperationProductInComponentFields.QUANTITY);

                unit = product.getStringField(ProductFields.UNIT);
            }

            Entity productDataInput = getProductDataInputDD().create();

            productDataInput.setField(ProductDataInputFields.PRODUCT_DATA, productData);
            productDataInput.setField(ProductDataInputFields.OPERATION_PRODUCT_IN_COMPONENT, operationProductInComponent);
            productDataInput.setField(ProductDataInputFields.NAME, name);
            productDataInput.setField(ProductDataInputFields.NUMBER, number);
            productDataInput.setField(ProductDataInputFields.QUANTITY, quantity);
            productDataInput.setField(ProductDataInputFields.UNIT, unit);

            productDataInput.getDataDefinition().save(productDataInput);
        });
    }

    public void createProductDataOperations(final Entity productData, final List<Entity> technologyOperationComponents) {
        technologyOperationComponents.forEach(technologyOperationComponent -> {
            Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

            String name = operation.getStringField(OperationFields.NAME);
            String number = operation.getStringField(OperationFields.NUMBER);

            String description = technologyOperationComponent.getStringField(TechnologyOperationComponentFields.COMMENT);

            Entity productDataOperation = getProductDataOperationDD().create();

            productDataOperation.setField(ProductDataOperationFields.PRODUCT_DATA, productData);
            productDataOperation.setField(ProductDataOperationFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);
            productDataOperation.setField(ProductDataOperationFields.NAME, name);
            productDataOperation.setField(ProductDataOperationFields.NUMBER, number);
            productDataOperation.setField(ProductDataOperationFields.DESCRIPTION, description);

            productDataOperation.getDataDefinition().save(productDataOperation);
        });
    }

    private class OperationConsumer implements Consumer<Entity> {

        private List<Entity> nodes = Lists.newArrayList();

        public void accept(final Entity node) {
            boolean showInProductData = node.getBooleanField("showInProductData");

            if (showInProductData) {
                nodes.add(node);
            }
        }

        public List<Entity> getNodes() {
            return nodes;
        }

    }

    public List<Entity> getOperations(final EntityTree operations) {
        OperationConsumer consumer = new OperationConsumer();

        operations.forEach(consumer);

        return consumer.getNodes();
    }

    public List<Entity> getInputs(final EntityTree operations) {
        OperationConsumer consumer = new OperationConsumer();

        operations.forEach(consumer);

        return operations
                .stream()
                .flatMap(
                        c -> {
                            List<Entity> operationProductInComponents = c
                                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
                            return operationProductInComponents.stream();
                        })
                .filter(c -> c.getBooleanField("showInProductData"))
                .map(c -> c
                        .getBelongsToField(OperationProductInComponentFields.PRODUCT))
                .collect(Collectors.toList());
    }

    public List<Entity> getOperationProductInComponents(final EntityTree operations) {
        OperationConsumer consumer = new OperationConsumer();

        operations.forEach(consumer);

        return operations
                .stream()
                .flatMap(
                        c -> {
                            List<Entity> operationProductInComponents = c
                                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
                            return operationProductInComponents.stream();
                        })
                .filter(c -> c.getBooleanField("showInProductData"))
                .collect(Collectors.toList());
    }

    public DataDefinition getProductDataInputDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_PRODUCT_DATA_INPUT);
    }

    public DataDefinition getProductDataOperationDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_PRODUCT_DATA_OPERATION);
    }

}
