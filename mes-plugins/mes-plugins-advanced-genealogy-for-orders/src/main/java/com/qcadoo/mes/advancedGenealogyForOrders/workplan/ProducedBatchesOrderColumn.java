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
package com.qcadoo.mes.advancedGenealogyForOrders.workplan;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component("producedBatchesOrderColumn")
public class ProducedBatchesOrderColumn implements OrderColumn {

    private TranslationService translationService;

    @Autowired
    public ProducedBatchesOrderColumn(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public String getIdentifier() {
        return "producedBatchesOrderColumn";
    }

    @Override
    public String getName(Locale locale) {
        return translationService.translate("workPlans.columnForOrders.name.value.producedBatches", locale);
    }

    @Override
    public String getDescription(Locale locale) {
        return translationService.translate("workPlans.columnForOrders.description.value.producedBatches", locale);
    }

    @Override
    public String getColumnValue(Entity order) {
        List<Entity> trackingRecords = order.getHasManyField("trackingRecords");
        StringBuilder batches = new StringBuilder();
        for (Entity trackingRecord : trackingRecords) {
            if (batches.length() > 0) {
                batches.append(", ");
            }
            if (trackingRecord.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH) != null) {
                batches.append(trackingRecord.getBelongsToField(TrackingRecordFields.PRODUCED_BATCH).getStringField("number"));
            }
        }
        return batches.toString();
    }
}
