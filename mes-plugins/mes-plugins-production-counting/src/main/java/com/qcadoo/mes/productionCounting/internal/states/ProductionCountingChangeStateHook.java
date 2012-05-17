/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState.MessageType;

@Service
public class ProductionCountingChangeStateHook {

    @Autowired
    private ProductionCountingStatesChangingService changingService;

    public void changedState(final DataDefinition dataDefinition, final Entity entity) {

        if (entity != null && entity.getId() != null) {

            final Entity oldEntity = dataDefinition.get(entity.getId());
            if (oldEntity != null) {
                List<ChangeRecordStateMessage> errors = changingService.performChangeState(entity, oldEntity);
                if (errors != null && !errors.isEmpty()) {
                    if (errors.size() == 1 && errors.get(0).getType().equals(MessageType.INFO)) {
                        return;
                    }
                    entity.setField("state", oldEntity.getField("state"));
                    for (ChangeRecordStateMessage error : errors) {
                        if (error.getReferenceToField() == null) {
                            entity.addGlobalError(error.getMessage(), error.getVars());
                        } else {
                            entity.addError(dataDefinition.getField(error.getReferenceToField()), error.getMessage(),
                                    error.getVars());
                        }
                    }
                }
            }
        }
    }
}
