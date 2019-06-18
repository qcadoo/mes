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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.advancedGenealogy.states.constants.BatchState;
import com.qcadoo.mes.advancedGenealogy.states.constants.BatchStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class BatchModelHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private BatchStateChangeDescriber batchStateChangeDescriber;

    public void setInitialState(final DataDefinition dataDefinition, final Entity batch) {
        stateChangeEntityBuilder.buildInitial(batchStateChangeDescriber, batch, BatchState.TRACKED);
    }

    public void changeBatchNumber(final DataDefinition dataDefinition, final Entity batch) {
        String batchNumber = batch.getStringField("number");
        Matcher matcher = Pattern.compile("(.+)\\((\\d+)\\)").matcher(batchNumber);

        String oldNumber = batchNumber;
        int index = 1;

        if (matcher.matches()) {
            oldNumber = matcher.group(1);
            index = Integer.valueOf(matcher.group(2)) + 1;
        }

        while (true) {
            String newNumber = oldNumber + "(" + (index++) + ")";

            int matches = dataDefinition.find().setMaxResults(1)
                    .add(SearchRestrictions.eq(dataDefinition.getField("number").getName(), newNumber)).list()
                    .getTotalNumberOfEntities();

            if (matches == 0) {
                batch.setField("number", newNumber);
                break;
            }
        }
    }

    public void clearExternalIdOnCopy(final DataDefinition batchDD, final Entity batch) {
        batch.setField("externalNumber", null);
    }
}
