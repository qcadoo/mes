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
package com.qcadoo.mes.advancedGenealogyForOrders.hooks;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordType;
import com.qcadoo.mes.advancedGenealogy.states.constants.BatchState;
import com.qcadoo.mes.advancedGenealogy.states.constants.TrackingRecordState;
import com.qcadoo.mes.advancedGenealogy.tree.AdvancedGenealogyTreeService;
import com.qcadoo.mes.advancedGenealogyForOrders.constants.TrackingRecordForOrderTreatment;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class TrackingForOrdersModelValidators {

    private static final String L_TRACKING_RECORD_TREATMENT = "trackingRecordTreatment";

    private static final String L_PRODUCT_BATCH_REQUIRED = "productBatchRequired";

    private static final String L_PRODUCT_IN_COMPONENT = "productInComponent";

    private static final String L_PRODUCT_IN_BATCHES = "productInBatches";

    private static final String L_BATCH = "batch";

    private static final String L_PRODUCT = "product";

    private static final String L_PRODUCED_BATCH = "producedBatch";

    private static final String L_GENEALOGY_PRODUCT_IN_COMPONENTS = "genealogyProductInComponents";

    private static final String L_ENTITY_TYPE = "entityType";

    private static final String L_STATE = "state";

    private static final String L_ORDER = "order";

    @Autowired
    private AdvancedGenealogyTreeService treeService;

    public final boolean checkRequiredFields(final DataDefinition trackingRecordDD, final Entity trackingRecord) {
        if (!isTrackingRecordForOrder(trackingRecord)) {
            return true;
        }

        if (trackingRecord.getBelongsToField(L_ORDER) == null) {
            appendErrorToModelField(trackingRecord, L_ORDER, "qcadooView.validate.field.error.missing");
            return false;
        }

        return true;
    }

    public final boolean checkChoosenOrderState(final DataDefinition dataDefinition, final Entity trackingRecord) {
        Entity order = trackingRecord.getBelongsToField(L_ORDER);
        if (!isTrackingRecordForOrder(trackingRecord) || order == null) {
            return true;
        }

        if (hasUnchangeablePlanAfterOrderAccept(order) && isOrderAccepted(order)) {
            trackingRecord.addError(dataDefinition.getField(L_ORDER), "advancedGenealogyForOrders.error.orderAcceptedError");
            return false;
        }

        if (hasUnchangeablePlanAfterOrderStart(order) && isOrderInProgress(order)) {
            trackingRecord.addError(dataDefinition.getField(L_ORDER), "advancedGenealogyForOrders.error.orderInProgressError");
            return false;
        }

        return true;
    }

    private boolean isTrackingRecordForOrder(final Entity trackingRecord) {
        return TrackingRecordType.FOR_ORDER.equals(trackingRecord.getStringField(L_ENTITY_TYPE));
    }

    private boolean isOrderAccepted(final Entity order) {
        return OrderState.ACCEPTED.getStringValue().equals(order.getStringField(L_STATE));
    }

    private boolean isOrderInProgress(final Entity order) {
        return OrderState.IN_PROGRESS.getStringValue().equals(order.getStringField(L_STATE));
    }

    private boolean hasUnchangeablePlanAfterOrderAccept(final Entity order) {
        return TrackingRecordForOrderTreatment.UNCHANGABLE_PLAN_AFTER_ORDER_ACCEPT.getStringValue().equals(
                order.getStringField(L_TRACKING_RECORD_TREATMENT));
    }

    private boolean hasUnchangeablePlanAfterOrderStart(final Entity order) {
        return TrackingRecordForOrderTreatment.UNCHANGABLE_PLAN_AFTER_ORDER_START.getStringValue().equals(
                order.getStringField(L_TRACKING_RECORD_TREATMENT));
    }

    public final boolean checkRequiredBatchesForInputProducts(final DataDefinition trackingRecordDD, final Entity trackingRecord) {
        if (!shouldValidateProductInBatches(trackingRecord) || !isTrackingRecordForOrder(trackingRecord)) {
            return true;
        }

        boolean isValid = true;
        for (Entity genealogyProductInComponent : getProductInComponents(trackingRecord)) {
            if (!hasRequiredBatches(genealogyProductInComponent)) {
                appendErrorToModelField(genealogyProductInComponent, L_PRODUCT_IN_COMPONENT,
                        "advancedGenealogyForOrders.error.missingRequiredBatchForProduct");
                isValid = false;
            }
        }
        return isValid;
    }

    public final boolean checkIfProducedBatchProductIsFinalProduct(final DataDefinition trackingRecordDD,
            final Entity trackingRecord) {
        Entity producedBatch = trackingRecord.getBelongsToField(L_PRODUCED_BATCH);
        if (producedBatch == null || !isTrackingRecordForOrder(trackingRecord)) {
            return true;
        }

        Entity order = trackingRecord.getBelongsToField(L_ORDER);
        if (order == null) {
            return true;
        }

        if (!belongsToTheSameProduct(order, producedBatch)) {
            appendErrorToModelField(trackingRecord, L_PRODUCED_BATCH,
                    "advancedGenealogyForOrders.trackingRecord.message.producedBatchIsNotFinalProduct");
            return false;
        }

        return true;
    }

    private boolean belongsToTheSameProduct(final Entity order, final Entity producedBatch) {
        Entity orderProduct = order.getBelongsToField(L_PRODUCT);
        Entity producedBatchProduct = producedBatch.getBelongsToField(L_PRODUCT);
        return producedBatchProduct.equals(orderProduct);
    }

    public final boolean checkIfProductInBatchBatchProductIsProductInComponentProduct(final DataDefinition trackingRecordDD,
            final Entity trackingRecord) {
        boolean isValid = true;
        for (Entity genealogyProductInComponent : getProductInComponents(trackingRecord)) {
            Entity productInComponent = getTechnologyProductInComponent(genealogyProductInComponent);
            Entity productInComponentProduct = productInComponent.getBelongsToField(L_PRODUCT);

            for (Entity productInBatch : getProductInBatches(genealogyProductInComponent)) {
                Entity batchProduct = getBatchProductFromProductInBatch(productInBatch);
                if (batchProduct == null || batchProduct.getId().equals(productInComponentProduct.getId())) {
                    continue;
                }
                appendErrorToModelField(productInBatch, L_BATCH,
                        "advancedGenealogyForOrders.error.productInBatchBatchProductIsNotProductInComponentProduct");
                isValid = false;
            }
        }
        return isValid;
    }

    private Entity getBatchProductFromProductInBatch(final Entity productInBatch) {
        Entity batch = productInBatch.getBelongsToField(L_BATCH);
        if (batch == null) {
            return null;
        }
        return batch.getBelongsToField(L_PRODUCT);
    }

    private Entity getTechnologyProductInComponent(final Entity genealogyProductInComponent) {
        return genealogyProductInComponent.getBelongsToField("productInComponent");
    }

    public final boolean checkIfProductInBatchBatchIsAlreadyUsed(final DataDefinition trackingRecordDD,
            final Entity trackingRecord) {
        boolean isValid = true;
        for (Entity genealogyProductInComponent : getProductInComponents(trackingRecord)) {
            List<Entity> productInBatches = getProductInBatches(genealogyProductInComponent);
            for (Entity productInBatch : productInBatches) {
                Entity batch = productInBatch.getBelongsToField(L_BATCH);
                if (haveMoreThanOneSameBatch(productInBatches, batch)) {
                    appendErrorToModelField(productInBatch, L_BATCH,
                            "advancedGenealogyForOrders.error.productInBatchBatchIsAlreadyUsed");
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    private List<Entity> getProductInComponents(final Entity trackingRecord) {
        List<Entity> productInComponents = Lists.newArrayList();
        if (!isTrackingRecordForOrder(trackingRecord)) {
            return productInComponents;
        }

        @SuppressWarnings("unchecked")
        List<Entity> genealogyProductInComponents = (List<Entity>) trackingRecord.getField(L_GENEALOGY_PRODUCT_IN_COMPONENTS);
        if (genealogyProductInComponents != null) {
            productInComponents.addAll(genealogyProductInComponents);
        }

        return productInComponents;
    }

    private List<Entity> getProductInBatches(final Entity genealogyProductInComponent) {
        @SuppressWarnings("unchecked")
        List<Entity> productInBatches = (List<Entity>) genealogyProductInComponent.getField(L_PRODUCT_IN_BATCHES);
        if (productInBatches == null) {
            Lists.newArrayList();
        }
        return productInBatches;
    }

    public final boolean checkIfProductInBatchBatchIsNotBlocked(final DataDefinition trackingRecordDD, final Entity trackingRecord) {
        boolean isValid = true;
        for (Entity genealogyProductInComponent : getProductInComponents(trackingRecord)) {
            for (Entity productInBatch : getProductInBatches(genealogyProductInComponent)) {
                if (isProductInComponentBatchBlocked(productInBatch)) {
                    appendErrorToModelField(productInBatch, L_BATCH,
                            "advancedGenealogyForOrders.error.productInBatchBatchIsBlocked");
                }
            }
        }
        return isValid;
    }

    private boolean isProductInComponentBatchBlocked(final Entity productInBatch) {
        Entity batch = productInBatch.getBelongsToField(L_BATCH);
        if (batch == null) {
            return false;
        }
        String batchState = batch.getStringField(L_STATE);
        return BatchState.BLOCKED.getStringValue().equals(batchState);
    }

    public final boolean checkIfProductInBatchTrackingRecordContainsNoProducedBatch(final DataDefinition trackingRecordDD,
            final Entity trackingRecord) {
        Entity producedBatch = trackingRecord.getBelongsToField(L_PRODUCED_BATCH);
        if (producedBatch == null) {
            return true;
        }

        boolean isValid = true;
        for (Entity genealogyProductInComponent : getProductInComponents(trackingRecord)) {
            for (Entity productInBatch : getProductInBatches(genealogyProductInComponent)) {
                Entity batch = productInBatch.getBelongsToField(L_BATCH);
                if (batch == null) {
                    continue;
                }

                List<Entity> tree = treeService.getProducedFromTree(batch, true, false);
                if (tree.contains(producedBatch)) {
                    appendErrorToModelField(productInBatch, L_BATCH,
                            "advancedGenealogyForOrders.error.productInBatchBatchTrackingRecordContainsProducedBatch");
                    appendErrorToModelField(trackingRecord, L_PRODUCED_BATCH,
                            "advancedGenealogyForOrders.error.ProducedBatchContainsProductInBatchBatchTrackingRecord");
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    private boolean shouldValidateProductInBatches(final Entity trackingRecord) {
        String state = trackingRecord.getStringField(L_STATE);
        return trackingRecord.getId() != null && TrackingRecordState.ACCEPTED.getStringValue().equals(state);
    }

    private boolean hasRequiredBatches(final Entity genealogyProductInComponent) {
        if (isBatchesRequired(genealogyProductInComponent)) {
            return haveAtLeastOneBatch(genealogyProductInComponent);
        }
        return true;
    }

    private boolean haveMoreThanOneSameBatch(final List<Entity> productInBatches, final Entity batch) {
        if (batch == null) {
            return false;
        }
        int count = 0;
        for (Entity productInBatch : productInBatches) {
            Entity productInBatchEntity = productInBatch.getBelongsToField(L_BATCH);
            if (batch.equals(productInBatchEntity)) {
                count++;
                if (count > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean haveAtLeastOneBatch(final Entity genealogyProductInComponent) {
        for (Entity productInBatch : getProductInBatches(genealogyProductInComponent)) {
            if (productInBatch.getField(L_BATCH) != null) {
                return true;
            }
        }
        return false;
    }

    private boolean isBatchesRequired(final Entity genealogyProductInComponent) {
        Entity productInComponent = genealogyProductInComponent.getBelongsToField(L_PRODUCT_IN_COMPONENT);
        return productInComponent.getBooleanField(L_PRODUCT_BATCH_REQUIRED);
    }

    private void appendErrorToModelField(final Entity entity, final String fieldName, final String messageKey) {
        FieldDefinition productInFieldDef = entity.getDataDefinition().getField(fieldName);
        entity.addError(productInFieldDef, messageKey);
    }

}
