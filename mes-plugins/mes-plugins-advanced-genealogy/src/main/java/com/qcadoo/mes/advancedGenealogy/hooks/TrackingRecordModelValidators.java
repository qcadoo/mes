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
package com.qcadoo.mes.advancedGenealogy.hooks;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.advancedGenealogy.states.constants.BatchState.BLOCKED;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogy.states.constants.BatchState;
import com.qcadoo.mes.advancedGenealogy.states.constants.TrackingRecordState;
import com.qcadoo.mes.advancedGenealogy.tree.AdvancedGenealogyTreeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TrackingRecordModelValidators {

    private static final String L_BATCH = "batch";

    private static final String L_USED_BATCHES_SIMPLE = "usedBatchesSimple";

    private static final String L_TECHNOLOGY_BATCH_REQUIRED = "technologyBatchRequired";

    private static final String L_TECHNOLOGY = "technology";

    private static final String L_ORDER = "order";

    private static final String L_TRACKING_RECORD = "trackingRecord";

    private static final String L_ENTITY_TYPE = "entityType";

    private static final String L_STATE = "state";

    private static final String L_PRODUCED_BATCH = "producedBatch";

    @Autowired
    private AdvancedGenealogyTreeService treeService;

    public final boolean checkIfTheresOrderTechnologyAndProducedBatchIsNotNull(final DataDefinition trackingRecordDD,
            final Entity trackingRecord) {
        Entity producedBatch = trackingRecord.getBelongsToField(L_PRODUCED_BATCH);
        if (isTrackingRecordSimple(trackingRecord)) {
            if (producedBatch == null) {
                trackingRecord.addError(trackingRecordDD.getField(L_PRODUCED_BATCH),
                        "advancedGenealogy.trackingRecord.message.producedBatchIsNull");

                return false;
            }
        }
        if (!TrackingRecordState.ACCEPTED.getStringValue().equals(trackingRecord.getStringField("state"))) {
            return true;
        }
        if (isTrackingRecordForOrder(trackingRecord)) {
            Entity order = trackingRecord.getBelongsToField(L_ORDER);

            if (order == null) {
                trackingRecord.addGlobalError("advancedGenealogy.trackingRecord.message.noOrder");
                return false;
            }

            Entity technology = order.getBelongsToField(L_TECHNOLOGY);
            if (technology == null) {
                trackingRecord.addGlobalError("advancedGenealogy.trackingRecord.message.orderDoesntContainTechnology");
                return false;
            }

            if (isProducedBatchRequiredForOrder(order) && producedBatch == null) {
                trackingRecord.addError(trackingRecordDD.getField(L_PRODUCED_BATCH),
                        "advancedGenealogy.trackingRecord.message.producedBatchIsNull");
                return false;
            }
        }
        return true;
    }

    private boolean isProducedBatchRequiredForOrder(final Entity order) {
        if (order == null) {
            return false;
        }
        Entity technology = order.getBelongsToField(L_TECHNOLOGY);
        return technology.getBooleanField(L_TECHNOLOGY_BATCH_REQUIRED);
    }

    public final boolean checkIfProducedBatchIsNotBlocked(final DataDefinition trackingRecordDD, final Entity trackingRecord) {
        Entity producedBatch = trackingRecord.getBelongsToField(L_PRODUCED_BATCH);

        if (producedBatch == null) {
            return true;
        } else {
            if (BatchState.BLOCKED.getStringValue().equals(producedBatch.getStringField(L_STATE))) {
                trackingRecord.addError(trackingRecordDD.getField(L_PRODUCED_BATCH),
                        "advancedGenealogy.trackingRecord.message.producedBatchIsBlocked");

                return false;
            }
        }

        return true;
    }

    public final boolean checkIfItHasAnyBlockedBatchesf(final DataDefinition trackingRecordDD, final Entity trackingRecord) {
        if (!isTrackingRecordSimple(trackingRecord)) {
            return true;
        }

        Entity producedBatch = trackingRecord.getBelongsToField(L_PRODUCED_BATCH);
        if (producedBatch != null && BLOCKED.getStringValue().equals(producedBatch.getStringField(L_STATE))) {
            trackingRecord.addGlobalError("advancedGenealogy.trackingRecord.message.producedBatchIsBlocked");
            return false;
        }

        List<Entity> usedBatches = trackingRecord.getHasManyField(L_USED_BATCHES_SIMPLE);

        if (usedBatches != null) {
            for (Entity usedBatch : usedBatches) {
                if (BLOCKED.getStringValue().equals(usedBatch.getBelongsToField(L_BATCH).getStringField(L_STATE))) {
                    trackingRecord.addGlobalError("advancedGenealogy.trackingRecord.message.usesBlockedBatches");
                    return false;
                }
            }
        }

        return true;
    }

    public final boolean checkIfItHasAnyBlockedBatches(final DataDefinition trackingRecordDD, final Entity trackingRecord) {
        if (!isTrackingRecordSimple(trackingRecord)) {
            return true;
        }

        Entity producedBatch = trackingRecord.getBelongsToField(L_PRODUCED_BATCH);
        if (producedBatch != null && BLOCKED.getStringValue().equals(producedBatch.getStringField(L_STATE))) {
            trackingRecord.addGlobalError("advancedGenealogy.trackingRecord.message.producedBatchIsBlocked");
            return false;
        }

        List<Entity> usedBatches = trackingRecord.getHasManyField(L_USED_BATCHES_SIMPLE);

        if (usedBatches != null) {
            for (Entity usedBatch : usedBatches) {
                if (BLOCKED.getStringValue().equals(usedBatch.getBelongsToField(L_BATCH).getStringField(L_STATE))) {
                    trackingRecord.addGlobalError("advancedGenealogy.trackingRecord.message.usesBlockedBatches");
                    return false;
                }
            }
        }

        return true;
    }

    public final boolean checkIfUsedBatchTrackingRecordContainsNoProducedBatch(final DataDefinition usedBatchDD,
            final Entity trackingRecord) {
        if (!isTrackingRecordSimple(trackingRecord)) {
            return true;
        }

        Entity producedBatch = trackingRecord.getBelongsToField(L_PRODUCED_BATCH);
        List<Entity> usedBatches = trackingRecord.getHasManyField(L_USED_BATCHES_SIMPLE);

        if (producedBatch == null) {
            return true;
        } else {
            if (usedBatches == null) {
                return true;
            } else {
                for (Entity usedBatch : usedBatches) {
                    Entity batch = usedBatch.getBelongsToField(L_BATCH);

                    List<Entity> tree = treeService.getProducedFromTree(batch, true, false);

                    if (tree.contains(producedBatch)) {
                        trackingRecord.addError(usedBatchDD.getField(L_PRODUCED_BATCH),
                                "advancedGenealogy.trackingRecord.message.usedBatchTrackingRecordContainsProducedBatch");

                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static final boolean isTrackingRecordSimple(final Entity trackingRecord) {
        checkArgument(L_TRACKING_RECORD.equals(trackingRecord.getDataDefinition().getName()));
        return "01simple".equals(trackingRecord.getStringField(L_ENTITY_TYPE));
    }

    public static final boolean isTrackingRecordForOrder(final Entity trackingRecord) {
        checkArgument(L_TRACKING_RECORD.equals(trackingRecord.getDataDefinition().getName()));
        return "02forOrder".equals(trackingRecord.getStringField(L_ENTITY_TYPE));
    }

}
