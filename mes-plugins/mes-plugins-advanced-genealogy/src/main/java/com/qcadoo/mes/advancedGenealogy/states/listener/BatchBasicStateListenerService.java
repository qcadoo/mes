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
package com.qcadoo.mes.advancedGenealogy.states.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields;
import com.qcadoo.mes.advancedGenealogy.constants.UsedBatchSimpleFields;
import com.qcadoo.mes.advancedGenealogy.states.constants.TrackingRecordState;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class BatchBasicStateListenerService {

    private static final String BLOCKED_ERR_MSG = "advancedGenealogy.batch.message.batchCanNotBeBlocked";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void checkIfOccursAsProducedBatch(final StateChangeContext stateChangeContext) {
        final Entity batch = stateChangeContext.getOwner();

        final SearchResult occursAsProducedBatch = dataDefinitionService
                .get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_TRACKING_RECORD).find()
                .add(SearchRestrictions.eq(TrackingRecordFields.STATE, TrackingRecordState.DRAFT.getStringValue()))
                .add(SearchRestrictions.belongsTo(TrackingRecordFields.PRODUCED_BATCH, batch)).list();

        for (Entity trackingRecord : occursAsProducedBatch.getEntities()) {
            final String number = trackingRecord.getStringField(TrackingRecordFields.NUMBER);
            stateChangeContext.addMessage(BLOCKED_ERR_MSG, StateMessageType.FAILURE, number);
        }
    }

    public void checkIfOccursAsUsedBatch(final StateChangeContext stateChangeContext) {
        final Entity batch = stateChangeContext.getOwner();

        final SearchResult occursAsUsedBatch = dataDefinitionService
                .get(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER, AdvancedGenealogyConstants.MODEL_USED_BATCH_SIMPLE)
                .find()
                .add(SearchRestrictions.belongsTo(UsedBatchSimpleFields.BATCH, batch))
                .createAlias(AdvancedGenealogyConstants.MODEL_TRACKING_RECORD, AdvancedGenealogyConstants.MODEL_TRACKING_RECORD)
                .add(SearchRestrictions.eq(AdvancedGenealogyConstants.MODEL_TRACKING_RECORD + '.' + TrackingRecordFields.STATE,
                        TrackingRecordState.DRAFT.getStringValue())).list();

        for (Entity usedBatch : occursAsUsedBatch.getEntities()) {
            final Entity producedBatch = usedBatch.getBelongsToField(UsedBatchSimpleFields.TRACKING_RECORD).getBelongsToField(
                    TrackingRecordFields.PRODUCED_BATCH);

            final String producedBatchNumber = producedBatch.getStringField(BatchFields.NUMBER);
            final Entity producedBatchProduct = producedBatch.getBelongsToField(BatchFields.PRODUCT);
            final String producedBatchProductName = producedBatchProduct.getStringField(ProductFields.NAME);
            final String producedBatchProductNumber = producedBatchProduct.getStringField(ProductFields.NUMBER);
            final String producedBatchProductNameNumber = " - " + producedBatchProductName + " (" + producedBatchProductNumber
                    + ")";
            final Entity producedBatchSupplierEntity = producedBatch.getBelongsToField(BatchFields.SUPPLIER);
            String producedBatchSupplier = "";
            if (producedBatchSupplierEntity != null) {
                producedBatchSupplier = " - " + producedBatchSupplierEntity.getStringField("name");
            }
            final String message = producedBatchNumber + producedBatchProductNameNumber + producedBatchSupplier;

            stateChangeContext.addMessage(BLOCKED_ERR_MSG, StateMessageType.FAILURE, message);
        }
    }

}
