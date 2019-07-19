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
package com.qcadoo.mes.productFlowThruDivision.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.SubstituteComponentFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.productFlowThruDivision.constants.MaterialAvailabilityFields;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MaterialAvailabilityListHooks {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillInReplacementsAvailableQuantity(final ViewDefinitionState state) {
        FormComponent formComponent = (FormComponent) state.getComponentByReference("product");
        GridComponent grid = (GridComponent) state.getComponentByReference("grid");

        Entity product = formComponent.getEntity().getDataDefinition().get(formComponent.getEntityId());
        List<Entity> replacements = product.getDataDefinition().get(product.getId())
                .getHasManyField(ProductFields.SUBSTITUTE_COMPONENTS).stream()
                .map(sc -> sc.getBelongsToField(SubstituteComponentFields.PRODUCT)).collect(Collectors.toList());
        List<Entity> warehouses = materialFlowResourcesService.getWarehouseLocationsFromDB();
        List<Entity> materialAvailabilityList = Lists.newArrayList();

        DataDefinition orderMaterialAvailabilityDD = dataDefinitionService.get(
                ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_MATERIAL_AVAILABILITY);

        for (Entity warehouse : warehouses) {
            Map<Long, BigDecimal> availableQuantities = materialFlowResourcesService.getQuantitiesForProductsAndLocation(
                    replacements, warehouse);
            for(Entity replacement : replacements) {
                if (Objects.nonNull(availableQuantities.get(replacement.getId()))
                        && BigDecimal.ZERO.compareTo(availableQuantities.get(replacement.getId())) < 0) {
                    Entity materialAvailability = orderMaterialAvailabilityDD.create();
                    materialAvailability.setField(MaterialAvailabilityFields.PRODUCT, replacement);
                    materialAvailability.setField(MaterialAvailabilityFields.UNIT, replacement.getField(ProductFields.UNIT));
                    materialAvailability.setField(MaterialAvailabilityFields.AVAILABLE_QUANTITY, availableQuantities.get(replacement.getId()));
                    materialAvailability.setField(MaterialAvailabilityFields.LOCATION, warehouse);
                    materialAvailabilityList.add(materialAvailability);
                }
            }
        }
        grid.setEntities(materialAvailabilityList
                .stream()
                .sorted(Comparator.comparing(e -> e.getBelongsToField(MaterialAvailabilityFields.PRODUCT).getStringField(
                        LocationFields.NUMBER))).collect(Collectors.toList()));
    }

    public void fillInAvailableQuantity(final ViewDefinitionState state) {
        FormComponent formComponent = (FormComponent) state.getComponentByReference("product");
        GridComponent grid = (GridComponent) state.getComponentByReference("grid");

        Entity product = formComponent.getEntity().getDataDefinition().get(formComponent.getEntityId());

        List<Entity> warehouses = materialFlowResourcesService.getWarehouseLocationsFromDB();
        List<Entity> materialAvailabilityList = Lists.newArrayList();

        DataDefinition orderMaterialAvailabilityDD = dataDefinitionService.get(
                ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ProductFlowThruDivisionConstants.MODEL_MATERIAL_AVAILABILITY);

        for (Entity warehouse : warehouses) {
            Map<Long, BigDecimal> availableQuantities = materialFlowResourcesService.getQuantitiesForProductsAndLocation(
                    Collections.singletonList(product), warehouse);
            if (Objects.nonNull(availableQuantities.get(product.getId()))
                    && BigDecimal.ZERO.compareTo(availableQuantities.get(product.getId())) < 0) {
                Entity materialAvailability = orderMaterialAvailabilityDD.create();

                materialAvailability.setField(MaterialAvailabilityFields.UNIT, product.getField(ProductFields.UNIT));
                materialAvailability.setField(MaterialAvailabilityFields.AVAILABLE_QUANTITY,
                        availableQuantities.get(product.getId()));
                materialAvailability.setField(MaterialAvailabilityFields.LOCATION, warehouse);
                materialAvailabilityList.add(materialAvailability);
            }
        }
        grid.setEntities(materialAvailabilityList
                .stream()
                .sorted(Comparator.comparing(e -> e.getBelongsToField(MaterialAvailabilityFields.LOCATION).getStringField(
                        LocationFields.NAME))).collect(Collectors.toList()));
    }
}
