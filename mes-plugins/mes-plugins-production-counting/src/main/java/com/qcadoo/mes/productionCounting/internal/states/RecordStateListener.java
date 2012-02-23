/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.productionCounting.internal.states;

import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_FOREACH;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class RecordStateListener {

    private static final String ORDER = "order";

    public List<ChangeRecordStateMessage> onAccepted(final Entity productionRecord, final Entity prevState) {

        return checkIfExistsFinalRecord(productionRecord);
    }

    public List<ChangeRecordStateMessage> onDeclined(final Entity productionRecord, final Entity prevState) {
        return new ArrayList<ChangeRecordStateMessage>();
    }

    public List<ChangeRecordStateMessage> checkIfExistsFinalRecord(final Entity productionRecord) {
        List<ChangeRecordStateMessage> errorList = new ArrayList<ChangeRecordStateMessage>();
        final Entity order = productionRecord.getBelongsToField(ORDER);
        final String typeOfProductionRecording = order.getStringField("typeOfProductionRecording");

        final SearchCriteriaBuilder searchBuilder = productionRecord.getDataDefinition().find();
        searchBuilder.add(SearchRestrictions.eq("state", ProductionCountingStates.ACCEPTED.getStringValue()));
        searchBuilder.add(SearchRestrictions.belongsTo(ORDER, order));
        searchBuilder.add(SearchRestrictions.eq("lastRecord", true));

        if (PARAM_RECORDING_TYPE_FOREACH.equals(typeOfProductionRecording)) {
            searchBuilder.add(SearchRestrictions.belongsTo("orderOperationComponent",
                    productionRecord.getBelongsToField("orderOperationComponent")));
        }
        if (searchBuilder.list().getTotalNumberOfEntities() != 0) {
            errorList.add(ChangeRecordStateMessage.error("productionCounting.record.messages.error.finalExists"));
        }
        return errorList;
    }

}
