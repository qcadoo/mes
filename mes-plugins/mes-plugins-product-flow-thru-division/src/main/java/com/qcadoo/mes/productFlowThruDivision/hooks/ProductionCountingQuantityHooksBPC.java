/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.productFlowThruDivision.constants.OperationProductInComponentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.OperationProductOutComponentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionCountingQuantityFieldsPFTD;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.util.Objects;

@Service
public class ProductionCountingQuantityHooksBPC {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onCreate(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        if (!productionCountingQuantity.getBooleanField(ProductionCountingQuantityFields.FLOW_FILLED)) {
            String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);
            String type = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);

            if (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(type)
                    || ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue().equals(type)) {
                Entity opoc = getOperationProduct(TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT,
                        productionCountingQuantity
                                .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                        productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT));
                if (opoc != null) {
                    productionCountingQuantity.setField(ProductionCountingQuantityFieldsPFTD.PRODUCTS_INPUT_LOCATION,
                            opoc.getField(OperationProductOutComponentFieldsPFTD.PRODUCTS_INPUT_LOCATION));
                }
            } else if (ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(type)) {
                Entity opoc = getOperationProduct(TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT,
                        productionCountingQuantity
                                .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                        productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT));
                if (opoc != null) {
                    productionCountingQuantity.setField(ProductionCountingQuantityFieldsPFTD.WASTE_RECEPTION_WAREHOUSE,
                            opoc.getField(OperationProductOutComponentFieldsPFTD.WASTE_RECEPTION_WAREHOUSE));
                }
            } else {
                if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)) {
                    Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
                    Entity opic = getOperationProduct(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT,
                            productionCountingQuantity
                                    .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                            product);
                    if (Objects.isNull(opic) && Objects.nonNull(product)) {
                        Entity parent = product.getBelongsToField(ProductFields.PARENT);
                        if (Objects.nonNull(parent)) {
                            opic = getOperationProduct(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT,
                                    productionCountingQuantity
                                            .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                                    parent);
                        }
                    }
                    if (opic != null) {
                        if (ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(type)) {
                            productionCountingQuantity.setField(ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION,
                                    opic.getField(OperationProductInComponentFieldsPFTD.COMPONENTS_LOCATION));
                            productionCountingQuantity.setField(ProductionCountingQuantityFieldsPFTD.COMPONENTS_OUTPUT_LOCATION,
                                    opic.getField(OperationProductInComponentFieldsPFTD.COMPONENTS_OUTPUT_LOCATION));
                        } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(type)) {
                            productionCountingQuantity.setField(ProductionCountingQuantityFieldsPFTD.PRODUCTION_FLOW,
                                    opic.getField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW));
                            productionCountingQuantity.setField(ProductionCountingQuantityFieldsPFTD.PRODUCTS_FLOW_LOCATION,
                                    opic.getField(OperationProductInComponentFieldsPFTD.PRODUCTS_FLOW_LOCATION));
                        }
                    }
                } else if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)) {
                    Entity opoc = getOperationProduct(TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT,
                            productionCountingQuantity
                                    .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT),
                            productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT));
                    if (opoc != null) {
                        productionCountingQuantity.setField(ProductionCountingQuantityFieldsPFTD.PRODUCTION_FLOW,
                                opoc.getField(OperationProductOutComponentFieldsPFTD.PRODUCTION_FLOW));
                        productionCountingQuantity.setField(ProductionCountingQuantityFieldsPFTD.PRODUCTS_FLOW_LOCATION,
                                opoc.getField(OperationProductOutComponentFieldsPFTD.PRODUCTS_FLOW_LOCATION));

                    }
                }
            }
        }
    }

    private Entity getOperationProduct(final String type, final Entity toc, final Entity product) {
        if (toc != null && product != null) {
            return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, type).find()
                    .add(SearchRestrictions.belongsTo("operationComponent", toc)).add(SearchRestrictions.belongsTo("product",
                            BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT, product.getId()))
                    .setMaxResults(1).uniqueResult();
        }

        return null;
    }

}
