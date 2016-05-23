/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.productionLines.hooks;

import java.util.Date;
import java.util.Optional;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.WorkstationTypeComponentsService;
import com.qcadoo.mes.productionLines.constants.WorkstationTypeComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class WorkstationTypeComponentHooks {

    @Autowired
    private WorkstationTypeComponentsService workstationTypeComponentsService;

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        Optional<Entity> maybePreviousComponent = workstationTypeComponentsService.findPreviousWorkstationTypeComponent(entity);
        if (maybePreviousComponent.isPresent()) {
            DateTime dateFrom = new DateTime(entity.getDateField(WorkstationTypeComponentFields.DATE_FROM));
            Entity previousComponent = maybePreviousComponent.get();
            Date originalDateTo = previousComponent.getDateField(WorkstationTypeComponentFields.DATE_TO);
            if (originalDateTo == null || originalDateTo.compareTo(dateFrom.toDate()) != 0) {
                previousComponent.setField(WorkstationTypeComponentFields.DATE_TO, dateFrom.toDate());
                Entity savedPrevious = dataDefinition.save(previousComponent);
                if (!savedPrevious.isValid()) {
                    savedPrevious.getErrors().entrySet().stream()
                            .forEach(entry -> entity.addGlobalError(entry.getValue().getMessage()));
                }
            }

        }

        if (shouldValidateDates(dataDefinition, entity)) {
            boolean componentExists = workstationTypeComponentsService.checkIfComponentForGivenTimeExists(entity);
            if (componentExists) {
                entity.addError(dataDefinition.getField(WorkstationTypeComponentFields.DATE_FROM),
                        "productionLines.workstationTypeComponent.validation.otherComponentsExist");
            }
        }

    }

    public boolean onDelete(final DataDefinition dataDefinition, final Entity entity) {
        boolean cannotDelete = entity.getDateField(WorkstationTypeComponentFields.DATE_FROM) != null
                && entity.getDateField(WorkstationTypeComponentFields.DATE_TO) != null;
        if (cannotDelete) {
            entity.addGlobalError("productionLines.workstationTypeComponent.onDelete.hasDates");
            return false;
        }

        Optional<Entity> maybePreviousComponent = workstationTypeComponentsService.findPreviousWorkstationTypeComponent(entity);
        if (maybePreviousComponent.isPresent()) {
            Entity previousComponent = maybePreviousComponent.get();
            previousComponent.setField(WorkstationTypeComponentFields.DATE_TO, null);
            dataDefinition.save(previousComponent);
        }
        return true;
    }

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = entity.getDateField(WorkstationTypeComponentFields.DATE_FROM);
        Date dateTo = entity.getDateField(WorkstationTypeComponentFields.DATE_TO);
        if (dateTo != null && dateTo.compareTo(dateFrom) <= 0) {
            entity.addError(dataDefinition.getField(WorkstationTypeComponentFields.DATE_FROM),
                    "productionLines.workstationTypeComponent.validation.datesInvalid");
            return false;
        }
        return true;
    }

    private boolean shouldValidateDates(final DataDefinition dataDefinition, final Entity originalEntity) {
        if (originalEntity.getId() == null) {
            return true;
        }
        Entity dbEntity = dataDefinition.get(originalEntity.getId());
        Date originalDateFrom = originalEntity.getDateField(WorkstationTypeComponentFields.DATE_FROM);
        Date dbDateFrom = dbEntity.getDateField(WorkstationTypeComponentFields.DATE_FROM);
        return originalDateFrom.compareTo(dbDateFrom) != 0;
    }
}
