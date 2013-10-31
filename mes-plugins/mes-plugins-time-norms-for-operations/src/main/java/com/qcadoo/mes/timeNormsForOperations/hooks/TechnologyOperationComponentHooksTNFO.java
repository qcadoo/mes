/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.timeNormsForOperations.hooks;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION;
import static com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants.FIELDS_OPERATION;

<<<<<<< HEAD
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields;
=======
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants;
>>>>>>> dev
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyOperationComponentHooksTNFO {

    @Autowired
<<<<<<< HEAD
    private UnitService unitService;

    public void onCreate(final DataDefinition dd, final Entity technologyOperationComponent) {
        setDefaultUnitOnCreate(dd, technologyOperationComponent);
    }

    private void setDefaultUnitOnCreate(final DataDefinition dd, final Entity technologyOperationComponent) {
        if (StringUtils.isEmpty(technologyOperationComponent
                .getStringField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT))) {
            String defaultUnit = unitService.getDefaultUnitFromSystemParameters();
            technologyOperationComponent.setField(TechnologyOperCompTNFOFields.PRODUCTION_IN_ONE_CYCLE_UNIT, defaultUnit);
        }
=======
    private DataDefinitionService dataDefinitionService;

    public void createTechOperCompTimeCalculations(final DataDefinition dd, final Entity technologyOperationComponent) {
        DataDefinition techOperCompTimeCalculationsDD = dataDefinitionService.get(TimeNormsConstants.PLUGIN_IDENTIFIER,
                TimeNormsConstants.MODEL_TECH_OPER_COMP_TIME_CALCULATIONS);
        Entity techOperCompTimeCalculations = techOperCompTimeCalculationsDD.create();
        techOperCompTimeCalculations = techOperCompTimeCalculationsDD.save(techOperCompTimeCalculations);
        technologyOperationComponent.setField("techOperCompTimeCalculations", techOperCompTimeCalculations);
        // technologyOperationComponent.getDataDefinition().save(technologyOperationComponent);
>>>>>>> dev
    }

    public void copyTimeNormsToTechnologyOperationComponent(final DataDefinition dd, final Entity technologyOperationComponent) {
        if ("referenceTechnology".equals(technologyOperationComponent.getField("entityType"))) {
            return;
        }
        if (technologyOperationComponent.getBelongsToField(OPERATION) == null) {
            return;
        }
        copyTimeValuesFromGivenOperation(technologyOperationComponent, technologyOperationComponent.getBelongsToField(OPERATION));
    }

    private void copyTimeValuesFromGivenOperation(final Entity target, final Entity source) {
        checkArgument(target != null, "given target is null");
        checkArgument(source != null, "given source is null");

        if (!shouldPropagateValuesFromLowerInstance(target)) {
            return;
        }

        for (String fieldName : FIELDS_OPERATION) {
            if (source.getField(fieldName) == null) {
                continue;
            }
            target.setField(fieldName, source.getField(fieldName));
        }
    }

    private boolean shouldPropagateValuesFromLowerInstance(final Entity technologyOperationComponent) {
        for (String fieldName : FIELDS_OPERATION) {
            if (technologyOperationComponent.getField(fieldName) != null) {
                return false;
            }
        }
        return true;
    }
}
