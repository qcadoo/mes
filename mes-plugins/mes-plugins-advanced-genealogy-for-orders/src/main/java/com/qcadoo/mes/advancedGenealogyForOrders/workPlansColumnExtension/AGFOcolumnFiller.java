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
package com.qcadoo.mes.advancedGenealogyForOrders.workPlansColumnExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.qcadoo.mes.advancedGenealogy.constants.TrackingRecordFields;
import com.qcadoo.mes.workPlans.print.ColumnFiller;
import com.qcadoo.model.api.Entity;

@Component
public class AGFOcolumnFiller implements ColumnFiller {

    private static final String L_BATCH_NUMBERS = "batchNumbers";

    private static final String PRODUCED_BATCHES_COLUMN = "producedBatches";

    @Override
    public Map<Entity, Map<String, String>> getOrderValues(final List<Entity> orders) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity order : orders) {
            getProducedBatches(order, values);
        }

        return values;
    }

    private void getProducedBatches(final Entity order, final Map<Entity, Map<String, String>> valuesMap) {
        if (valuesMap.get(order) == null) {
            valuesMap.put(order, new HashMap<String, String>());
        }

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

        valuesMap.get(order).put(PRODUCED_BATCHES_COLUMN, batches.toString());
    }

    @Override
    public Map<Entity, Map<String, String>> getValues(final List<Entity> orders) {
        Map<Entity, Map<String, String>> valuesMap = new HashMap<Entity, Map<String, String>>();

        for (Entity order : orders) {

            List<Entity> trackingRecords = order.getHasManyField("trackingRecords");

            for (Entity trackingRecord : trackingRecords) {
                List<Entity> genProdInComps = trackingRecord.getHasManyField("genealogyProductInComponents");

                for (Entity genProdInComp : genProdInComps) {
                    Entity operProdInComp = genProdInComp.getBelongsToField("productInComponent");

                    if (valuesMap.get(operProdInComp) == null) {
                        valuesMap.put(operProdInComp, new HashMap<String, String>());
                    }

                    List<Entity> batchComps = genProdInComp.getHasManyField("productInBatches");

                    StringBuilder batchNumbers = new StringBuilder();

                    String previousValue = valuesMap.get(operProdInComp).get(L_BATCH_NUMBERS);

                    if (previousValue != null) {
                        batchNumbers.append(previousValue);
                    }

                    for (Entity batchComp : batchComps) {
                        if (batchNumbers.length() > 0) {
                            batchNumbers.append(", ");
                        }
                        batchNumbers.append(batchComp.getBelongsToField("batch").getStringField("number"));
                    }

                    valuesMap.get(operProdInComp).put(L_BATCH_NUMBERS, batchNumbers.toString());
                }
            }
        }

        return valuesMap;
    }
}
