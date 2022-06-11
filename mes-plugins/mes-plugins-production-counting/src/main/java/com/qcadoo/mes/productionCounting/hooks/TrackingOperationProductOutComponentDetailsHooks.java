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
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentDtoFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.listeners.TrackingOperationProductComponentDetailsListeners;
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

    private static final String L_LOCATION_ID = "locationId";

    private static final String L_STORAGE_LOCATION = "storageLocation";

    private static final String L_PRODUCT_ID = "productId";

    @Autowired
    private TrackingOperationProductComponentDetailsListeners trackingOperationProductComponentDetailsListeners;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    public void onBeforeRender(final ViewDefinitionState view) {
        trackingOperationProductComponentDetailsListeners.onBeforeRender(view);
        fillStorageLocation(view);

        FormComponent trackingOperationProductOutComponentForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity trackingOperationProductOutComponent = trackingOperationProductOutComponentForm
                .getPersistedEntityWithIncludedFormValues();

        Entity trackingOperationProductInComponentDto = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT_DTO).get(
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
        if(manyReasonsForLacks.isChecked()) {
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
            LookupComponent storageLocationLookup = (LookupComponent) view.getComponentByReference(L_STORAGE_LOCATION);

            Entity productionTracking = trackingOperationProductOutComponent
                    .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
            Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
            Entity product = trackingOperationProductOutComponent
                    .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

            FilterValueHolder storageLocationFilterValueHolder = storageLocationLookup.getFilterValue();

            Entity bpcq = basicProductionCountingService
                    .getProductionCountingQuantityDD()
                    .find()
                    .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ORDER + L_ID, order.getId()))
                    .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                            ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                    .add(SearchRestrictions.eq(ProductionCountingQuantityFields.PRODUCT + L_ID, product.getId()))
                    .setMaxResults(1).uniqueResult();

            if (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(
                    bpcq.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL))) {
                if (Objects.nonNull(bpcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_INPUT_LOCATION))) {
                    storageLocationFilterValueHolder.put(L_LOCATION_ID,
                            bpcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_INPUT_LOCATION).getId());
                    storageLocationFilterValueHolder.put(L_PRODUCT_ID, order.getBelongsToField(OrderFields.PRODUCT).getId());
                    Optional<Entity> option = findStorageLocationForProduct(order.getBelongsToField(OrderFields.PRODUCT),
                            bpcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_INPUT_LOCATION));
                    if (option.isPresent()) {
                        if (Objects.isNull(storageLocationLookup.getEntity())) {
                            storageLocationLookup.setFieldValue(option.get().getId());
                            storageLocationLookup.requestComponentUpdateState();
                        }
                        storageLocationLookup.setEnabled(false);
                    }

                } else if (storageLocationFilterValueHolder.has(L_LOCATION_ID)) {
                    storageLocationFilterValueHolder.remove(L_LOCATION_ID);
                }

            } else {
                if (Objects.nonNull(bpcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION))) {
                    storageLocationFilterValueHolder.put(L_LOCATION_ID,
                            bpcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION).getId());
                    storageLocationFilterValueHolder.put(L_PRODUCT_ID, bpcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId());
                        Optional<Entity> option = findStorageLocationForProduct(bpcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT),
                                bpcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCTS_FLOW_LOCATION));
                    if (option.isPresent()) {
                        if (Objects.isNull(storageLocationLookup.getEntity())) {
                            storageLocationLookup.setFieldValue(option.get().getId());
                            storageLocationLookup.requestComponentUpdateState();
                        }
                        storageLocationLookup.setEnabled(false);
                    }
                } else if (storageLocationFilterValueHolder.has(L_LOCATION_ID)) {
                    storageLocationFilterValueHolder.remove(L_LOCATION_ID);
                }
            }
            storageLocationLookup.setFilterValue(storageLocationFilterValueHolder);

        }
    }

    private DataDefinition getStorageLocationDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION);
    }

    private Optional<Entity> findStorageLocationForProduct(final Entity product, final Entity location) {
        SearchCriteriaBuilder scb = getStorageLocationDD().find();
        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.PRODUCT, product));
        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.LOCATION, location));
        scb.add(SearchRestrictions.eq(StorageLocationFields.ACTIVE, true));
        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }
}
