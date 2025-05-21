/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentDtoFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.listeners.TrackingOperationProductComponentDetailsListeners;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class TrackingOperationProductOutComponentDetailsHooks {

    private static final String L_ID = ".id";

    public static final String L_LOCATION_ID = "locationId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TrackingOperationProductComponentDetailsListeners trackingOperationProductComponentDetailsListeners;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void onBeforeRender(final ViewDefinitionState view) {
        trackingOperationProductComponentDetailsListeners.onBeforeRender(view);

        fillStorageLocation(view);

        FormComponent trackingOperationProductOutComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity trackingOperationProductInComponentDto = getTrackingOperationProductOutComponentDtoDD().get(
                trackingOperationProductOutComponentForm.getEntityId());

        FieldComponent plannedQuantity = (FieldComponent) view
                .getComponentByReference(TrackingOperationProductOutComponentDtoFields.PLANNED_QUANTITY);
        plannedQuantity.setFieldValue(trackingOperationProductInComponentDto
                .getDecimalField(TrackingOperationProductOutComponentDtoFields.PLANNED_QUANTITY));
        FieldComponent remainingQuantity = (FieldComponent) view
                .getComponentByReference(TrackingOperationProductOutComponentDtoFields.REMAINING_QUANTITY);
        remainingQuantity.setFieldValue(trackingOperationProductInComponentDto
                .getDecimalField(TrackingOperationProductOutComponentDtoFields.REMAINING_QUANTITY));
        FieldComponent producedSum = (FieldComponent) view
                .getComponentByReference(TrackingOperationProductOutComponentDtoFields.PRODUCED_SUM);
        producedSum.setFieldValue(trackingOperationProductInComponentDto
                .getDecimalField(TrackingOperationProductOutComponentDtoFields.PRODUCED_SUM));
        FieldComponent wastesSum = (FieldComponent) view
                .getComponentByReference(TrackingOperationProductOutComponentDtoFields.WASTES_SUM);
        wastesSum.setFieldValue(trackingOperationProductInComponentDto
                .getDecimalField(TrackingOperationProductOutComponentDtoFields.WASTES_SUM));

        FieldComponent wastesQuantity = (FieldComponent) view
                .getComponentByReference(TrackingOperationProductOutComponentFields.WASTES_QUANTITY);

        FieldComponent causeOfWastes = (FieldComponent) view
                .getComponentByReference(TrackingOperationProductOutComponentFields.CAUSE_OF_WASTES);

        CheckBoxComponent manyReasonsForLacks = (CheckBoxComponent) view.getComponentByReference(TrackingOperationProductOutComponentFields.MANY_REASONS_FOR_LACKS);

        GridComponent lacks = (GridComponent) view.getComponentByReference("lacks");

        if (manyReasonsForLacks.isChecked()) {
            wastesQuantity.setEnabled(false);
            causeOfWastes.setEnabled(false);
            causeOfWastes.setFieldValue(null);
            causeOfWastes.requestComponentUpdateState();
            wastesQuantity.requestComponentUpdateState();
            lacks.setEnabled(true);
        } else {
            wastesQuantity.setEnabled(true);
            causeOfWastes.setEnabled(true);
            lacks.setEnabled(false);
        }
    }

    private void fillStorageLocation(final ViewDefinitionState view) {
        FormComponent trackingOperationProductOutComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity trackingOperationProductOutComponent = trackingOperationProductOutComponentForm.getEntity();

        if (view.isViewAfterRedirect()) {
            LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(TrackingOperationProductOutComponentFields.STORAGE_LOCATION);

            Entity productionTracking = trackingOperationProductOutComponent
                    .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
            Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
            Entity technologyOperationComponent = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
            Entity product = trackingOperationProductOutComponent
                    .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

            FilterValueHolder storageLocationFilterValueHolder = storageLocationLookup.getFilterValue();

            SearchCriteriaBuilder scb = basicProductionCountingService
                    .getProductionCountingQuantityDD()
                    .find()
                    .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ORDER + L_ID, order.getId()))
                    .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                            ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                    .add(SearchRestrictions.eq(ProductionCountingQuantityFields.PRODUCT + L_ID, product.getId()));

            if (technologyOperationComponent != null) {
                scb.add(SearchRestrictions.eq(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT + L_ID, technologyOperationComponent.getId()));
            }

            Entity productionCountingQuantity = scb.setMaxResults(1).uniqueResult();
            String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);

            Entity warehouse = null;
            if (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(typeOfMaterial) ||
                    ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue().equals(typeOfMaterial)) {
                warehouse = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_INPUT_LOCATION);
            } else if (ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue().equals(typeOfMaterial)) {
                warehouse = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION);
            } else if (ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(typeOfMaterial)) {
                warehouse = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.WASTE_RECEPTION_WAREHOUSE);
            }
            if (Objects.nonNull(warehouse)) {
                storageLocationFilterValueHolder.put(L_LOCATION_ID, warehouse.getId());

                if (Objects.isNull(storageLocationLookup.getEntity())
                        && productionTracking.getStringField(ProductionTrackingFields.STATE).equals(ProductionTrackingStateStringValues.DRAFT)) {
                    Optional<Entity> mayBeStorageLocation = materialFlowResourcesService.findStorageLocationForProduct(warehouse,
                            product.getId());

                    if (mayBeStorageLocation.isPresent()) {
                        storageLocationLookup.setFieldValue(mayBeStorageLocation.get().getId());
                        storageLocationLookup.requestComponentUpdateState();
                    }
                }
            } else if (storageLocationFilterValueHolder.has(L_LOCATION_ID)) {
                storageLocationFilterValueHolder.remove(L_LOCATION_ID);
            }

            storageLocationLookup.setFilterValue(storageLocationFilterValueHolder);
        }
    }

    private DataDefinition getTrackingOperationProductOutComponentDtoDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT_DTO);
    }

}
