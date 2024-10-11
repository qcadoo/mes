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
package com.qcadoo.mes.technologies.validators;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyProductionLineFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyProductionLineModelValidators {

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        return checkIfLineIsNotAlreadyAdded(dataDefinition, entity);
    }

    private boolean checkIfLineIsNotAlreadyAdded(final DataDefinition dataDefinition, final Entity entity) {
        boolean isNotAlreadyAdded = true;

        Long entityId = entity.getId();
        Entity technology = entity.getBelongsToField(TechnologyProductionLineFields.TECHNOLOGY);
        Entity productionLine = entity.getBelongsToField(TechnologyProductionLineFields.PRODUCTION_LINE);

        if (!Objects.isNull(productionLine) && !Objects.isNull(technology)) {
            Entity technologyFromDB = technology.getDataDefinition().get(technology.getId());

            List<Entity> productionLines = technologyFromDB.getHasManyField(TechnologyFields.PRODUCTION_LINES);

            if (!Objects.isNull(entityId)) {
                productionLines = filterCurrentProductionLine(entity, productionLines);
            }

            if (checkIfProductionLineIsAlreadyAdded(productionLine, productionLines)) {
                entity.addError(dataDefinition.getField(TechnologyProductionLineFields.PRODUCTION_LINE),
                        "technologies.technologyProductionLine.error.productionLine.alreadyAdded");

                isNotAlreadyAdded = false;
            }
        }

        return isNotAlreadyAdded;
    }

    private List<Entity> filterCurrentProductionLine(final Entity entity, final List<Entity> productionLines) {
        return productionLines.stream().filter(addedProductionLine -> !addedProductionLine.getId().equals(entity.getId())).collect(Collectors.toList());
    }

    private boolean checkIfProductionLineIsAlreadyAdded(final Entity productionLine, final List<Entity> productionLines) {
        return productionLines.stream().anyMatch(addedProductionLine -> addedProductionLine.getBelongsToField(TechnologyProductionLineFields.PRODUCTION_LINE).getId().equals(productionLine.getId()));
    }

}
