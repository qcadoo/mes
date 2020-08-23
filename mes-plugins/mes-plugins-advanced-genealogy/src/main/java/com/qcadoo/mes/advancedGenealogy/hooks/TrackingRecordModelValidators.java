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

import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordType;
import com.qcadoo.mes.advancedGenealogy.states.constants.BatchState;
import com.qcadoo.mes.advancedGenealogy.tree.AdvancedGenealogyTreeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.advancedGenealogy.states.constants.BatchState.BLOCKED;

@Service
public class TrackingRecordModelValidators {

    private static final String L_BATCH = "batch";

    private static final String L_USED_BATCHES_SIMPLE = "usedBatchesSimple";

    private static final String L_TRACKING_RECORD = "trackingRecord";

    private static final String L_ENTITY_TYPE = "entityType";

    private static final String L_STATE = "state";

    @Autowired
    private AdvancedGenealogyTreeService treeService;

    public final boolean checkIfProducedBatchIsNotBlocked(final DataDefinition trackingRecordDD, final Entity trackingRecord) {
        Entity producedBatch = trackingRecord.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH);

        if (BatchState.BLOCKED.getStringValue().equals(producedBatch.getStringField(L_STATE))) {
            trackingRecord.addError(trackingRecordDD.getField(TrackingRecordFields.PRODUCED_BATCH),
                    "advancedGenealogy.trackingRecord.message.producedBatchIsBlocked");

            return false;
        }

        return true;
    }

    public final boolean checkIfItHasAnyBlockedBatchesf(final DataDefinition trackingRecordDD, final Entity trackingRecord) {
        if (!isTrackingRecordSimple(trackingRecord)) {
            return true;
        }

        Entity producedBatch = trackingRecord.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH);
        if (BLOCKED.getStringValue().equals(producedBatch.getStringField(L_STATE))) {
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

        Entity producedBatch = trackingRecord.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH);
        if (BLOCKED.getStringValue().equals(producedBatch.getStringField(L_STATE))) {
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

        Entity producedBatch = trackingRecord.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH);
        List<Entity> usedBatches = trackingRecord.getHasManyField(L_USED_BATCHES_SIMPLE);

        if (usedBatches == null) {
            return true;
        } else {
            for (Entity usedBatch : usedBatches) {
                Entity batch = usedBatch.getBelongsToField(L_BATCH);

                List<Entity> tree = treeService.getProducedFromTree(batch, true, false);

                if (tree.contains(producedBatch)) {
                    trackingRecord.addError(usedBatchDD.getField(TrackingRecordFields.PRODUCED_BATCH),
                            "advancedGenealogy.trackingRecord.message.usedBatchTrackingRecordContainsProducedBatch");

                    return false;
                }
            }
        }

        return true;
    }

    public static final boolean isTrackingRecordSimple(final Entity trackingRecord) {
        checkArgument(L_TRACKING_RECORD.equals(trackingRecord.getDataDefinition().getName()));
        return TrackingRecordType.SIMPLE.equals(trackingRecord.getStringField(L_ENTITY_TYPE));
    }

    public static final boolean isTrackingRecordForOrder(final Entity trackingRecord) {
        checkArgument(L_TRACKING_RECORD.equals(trackingRecord.getDataDefinition().getName()));
        return TrackingRecordType.FOR_ORDER.equals(trackingRecord.getStringField(L_ENTITY_TYPE));
    }

}
