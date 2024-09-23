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
package com.qcadoo.mes.productFlowThruDivision.validators;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionCountingQuantityFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionFlowComponent;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class ProductionCountingQuantityValidatorsPFTD {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    public boolean validate(final DataDefinition dataDefinition, final Entity productionCountingQuantity) {

        boolean isValid = checkRequiredFields(dataDefinition, productionCountingQuantity);
        isValid = isValid && checkResourceReservationQuantity(dataDefinition, productionCountingQuantity);

        return isValid;

    }

    private boolean checkResourceReservationQuantity(DataDefinition dataDefinition, Entity productionCountingQuantity) {
        List<Entity> reservations = productionCountingQuantity.getHasManyField("orderProductResourceReservations");
        if (reservations == null || reservations.isEmpty()) {
            return true;
        }

        BigDecimal plannedQuantityFromResources = productionCountingQuantity.getHasManyField("orderProductResourceReservations")
                .stream()
                .map(rr -> rr.getDecimalField("planedQuantity"))
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal plannedQuantity = productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

        if (plannedQuantityFromResources.compareTo(plannedQuantity) > 0) {
            productionCountingQuantity.addError(dataDefinition.getField(ProductionCountingQuantityFields.PLANNED_QUANTITY), "productFlowThruDivision.orderProductResourceReservation.error.productionCountingQuantity.plannedQuantityLowerThanReservedQuantity");
            return false;
        }


        return true;
    }

    public boolean checkRequiredFields(final DataDefinition dataDefinition, final Entity productionCountingQuantity) {
        String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);
        String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
        if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)
                && ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(typeOfMaterial)
                && productionCountingQuantity
                .getBelongsToField(ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION) == null) {
            productionCountingQuantity.addError(
                    dataDefinition.getField(ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
        }
        if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)
                && (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(typeOfMaterial)
                || ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue().equals(typeOfMaterial))
                && productionCountingQuantity
                .getBelongsToField(ProductionCountingQuantityFieldsPFTD.PRODUCTS_INPUT_LOCATION) == null) {
            productionCountingQuantity.addError(
                    dataDefinition.getField(ProductionCountingQuantityFieldsPFTD.PRODUCTS_INPUT_LOCATION),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
        }
        if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(typeOfMaterial)
                && ProductionFlowComponent.WAREHOUSE.getStringValue().equals(
                productionCountingQuantity.getStringField(ProductionCountingQuantityFieldsPFTD.PRODUCTION_FLOW))
                && productionCountingQuantity
                .getBelongsToField(ProductionCountingQuantityFieldsPFTD.PRODUCTS_FLOW_LOCATION) == null) {
            productionCountingQuantity.addError(
                    dataDefinition.getField(ProductionCountingQuantityFieldsPFTD.PRODUCTS_FLOW_LOCATION),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
        }
        checkComponentsWarehouses(productionCountingQuantity);
        checkWastesWarehouse(productionCountingQuantity);
        return productionCountingQuantity.isValid();
    }

    private void checkWastesWarehouse(Entity pcq) {
        String role = pcq.getStringField(ProductionCountingQuantityFields.ROLE);
        String typeOfMaterial = pcq.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
        if (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role)
                && ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(typeOfMaterial)) {
            Entity wasteReceptionWarehouse = pcq.getBelongsToField(ProductionCountingQuantityFieldsPFTD.WASTE_RECEPTION_WAREHOUSE);
            Entity product = pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            Entity basicProductionCounting = pcq.getBelongsToField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING);
            Long id = pcq.getId();
            if (basicProductionCounting != null) {
                boolean cumulated = TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                        basicProductionCounting.getBelongsToField(BasicProductionCountingFields.ORDER).getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));
                if (cumulated) {
                    if (wasteReceptionWarehouse == null) {
                        if (basicProductionCounting.getHasManyField(BasicProductionCountingFields.PRODUCTION_COUNTING_QUANTITIES)
                                .stream().anyMatch(e -> ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(e.getStringField(ProductionCountingQuantityFields.ROLE))
                                        && ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(e.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))
                                        && product.getId().equals(e.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId())
                                        && e.getBelongsToField(ProductionCountingQuantityFieldsPFTD.WASTE_RECEPTION_WAREHOUSE) != null
                                        && (id == null || !id.equals(e.getId())))) {
                            pcq.addGlobalError("productFlowThruDivision.location.wastes.locationsAreDifferent", false);
                        }
                    } else if (basicProductionCounting.getHasManyField(BasicProductionCountingFields.PRODUCTION_COUNTING_QUANTITIES)
                            .stream().anyMatch(e -> ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(e.getStringField(ProductionCountingQuantityFields.ROLE))
                                    && ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(e.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))
                                    && product.getId().equals(e.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId())
                                    && (e.getBelongsToField(ProductionCountingQuantityFieldsPFTD.WASTE_RECEPTION_WAREHOUSE) == null
                                    || !wasteReceptionWarehouse.getId().equals(e.getBelongsToField(ProductionCountingQuantityFieldsPFTD.WASTE_RECEPTION_WAREHOUSE).getId()))
                                    && (id == null || !id.equals(e.getId())))) {
                        pcq.addGlobalError("productFlowThruDivision.location.wastes.locationsAreDifferent", false);
                    }
                }
            }
        }
    }

    private void checkComponentsWarehouses(final Entity pcq) {
        Entity componentsLocation = pcq.getBelongsToField(ProductionCountingQuantityFieldsPFTD.COMPONENTS_LOCATION);
        Entity componentsOutputLocation = pcq.getBelongsToField(ProductionCountingQuantityFieldsPFTD.COMPONENTS_OUTPUT_LOCATION);
        if (componentsLocation != null && componentsOutputLocation != null
                && componentsLocation.getId().equals(componentsOutputLocation.getId())) {
            pcq.addGlobalError("technologies.technology.error.componentsLocationsAreSame", false);
        }
    }
}
