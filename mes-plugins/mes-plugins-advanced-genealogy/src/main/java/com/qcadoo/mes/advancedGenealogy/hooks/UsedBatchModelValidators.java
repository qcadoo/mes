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
import com.qcadoo.mes.advancedGenealogy.states.constants.BatchState;
import com.qcadoo.mes.advancedGenealogy.tree.AdvancedGenealogyTreeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsedBatchModelValidators {

    private static final String L_USED_BATCHES_SIMPLE = "usedBatchesSimple";

    private static final String L_TRACKING_RECORD = "trackingRecord";

    private static final String L_STATE = "state";

    private static final String L_BATCH = "batch";

    @Autowired
    private AdvancedGenealogyTreeService treeService;

    public final boolean checkIfUsedBatchIsNotBlocked(final DataDefinition usedBatchDD, final Entity usedBatch) {
        Entity batch = usedBatch.getBelongsToField(L_BATCH);
        if (batch == null) {
            return true;
        } else {
            if (BatchState.BLOCKED.getStringValue().equals(batch.getStringField(L_STATE))) {
                usedBatch.addError(usedBatchDD.getField(L_BATCH), "advancedGenealogy.usedBatchSimple.message.usedBatchIsBlocked");

                return false;
            }
        }

        return true;
    }

    public final boolean checkIfUsedBatchIsNotAlreadyUsed(final DataDefinition usedBatchDD, final Entity usedBatch) {
        Entity batch = usedBatch.getBelongsToField(L_BATCH);
        Entity trackingRecord = usedBatch.getBelongsToField(L_TRACKING_RECORD);
        EntityList usedBatches = trackingRecord.getHasManyField(L_USED_BATCHES_SIMPLE);

        if (usedBatch.getId() == null) {
            if (batch == null) {
                return true;
            } else {
                if (usedBatches == null) {
                    return true;
                } else {
                    for (Entity ub : usedBatches) {
                        Entity b = ub.getBelongsToField(L_BATCH);
                        if (b.getId().equals(batch.getId())) {
                            usedBatch.addError(usedBatchDD.getField(L_BATCH),
                                    "advancedGenealogy.usedBatchSimple.message.usedBatchIsAlreadyUsed");

                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public final boolean checkIfUsedBatchIsNotProducedBatch(final DataDefinition usedBatchDD, final Entity usedBatch) {
        Entity batch = usedBatch.getBelongsToField(L_BATCH);
        Entity trackingRecord = usedBatch.getBelongsToField(L_TRACKING_RECORD);
        Entity producedBatch = trackingRecord.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH);

        if (batch == null) {
            return true;
        } else {
            if (producedBatch.getId().equals(batch.getId())) {
                usedBatch.addError(usedBatchDD.getField(L_BATCH),
                        "advancedGenealogy.usedBatchSimple.message.usedBatchIsProducedBatch");

                return false;
            }
        }

        return true;
    }

    public final boolean checkIfUsedBatchTrackingRecordContainsNoProducedBatch(final DataDefinition usedBatchDD,
            final Entity usedBatch) {
        Entity batch = usedBatch.getBelongsToField(L_BATCH);
        Entity trackingRecord = usedBatch.getBelongsToField(L_TRACKING_RECORD);
        Entity producedBatch = trackingRecord.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH);


        List<Entity> tree = treeService.getProducedFromTree(batch, true, false);

        if (tree.contains(producedBatch)) {
            usedBatch.addError(usedBatchDD.getField(L_BATCH),
                    "advancedGenealogy.usedBatchSimple.message.usedBatchTrackingRecordContainsProducedBatch");

            return false;
        }

        return true;
    }
}
