/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.timeNormsForOperations.hooks;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_WORKSTATION_TIMES;
import static com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants.FIELDS_OPERATION;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.OperationFieldsTFNO;
import com.qcadoo.mes.timeNormsForOperations.constants.OperationWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyOperationComponentHooksTNFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UnitService unitService;

    public void onCreate(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        setDefaultUnitOnCreate(technologyOperationComponent);
    }

    private void setDefaultUnitOnCreate(final Entity technologyOperationComponent) {
        if (StringUtils.isEmpty(technologyOperationComponent
                .getStringField(TechnologyOperationComponentFieldsTNFO.PRODUCTION_IN_ONE_CYCLE_UNIT))) {
            String defaultUnit = unitService.getDefaultUnitFromSystemParameters();
            technologyOperationComponent.setField(TechnologyOperationComponentFieldsTNFO.PRODUCTION_IN_ONE_CYCLE_UNIT,
                    defaultUnit);
        }
    }

    public void createTechOperCompTimeCalculations(final DataDefinition dd, final Entity technologyOperationComponent) {
        DataDefinition techOperCompTimeCalculationDD = dataDefinitionService.get(TimeNormsConstants.PLUGIN_IDENTIFIER,
                TimeNormsConstants.MODEL_TECH_OPER_COMP_TIME_CALCULATION);
        Entity techOperCompTimeCalculation = techOperCompTimeCalculationDD.create();
        techOperCompTimeCalculation = techOperCompTimeCalculationDD.save(techOperCompTimeCalculation);
        technologyOperationComponent.setField(TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_TIME_CALCULATION,
                techOperCompTimeCalculation);
    }

    public void copyTimeNormsToTechnologyOperationComponent(final DataDefinition dd, final Entity technologyOperationComponent) {
        if (technologyOperationComponent.getBelongsToField(OPERATION) == null) {
            return;
        }
        copyTimeValuesFromGivenOperation(technologyOperationComponent, technologyOperationComponent.getBelongsToField(OPERATION));
    }

    private void copyTimeValuesFromGivenOperation(final Entity target, final Entity source) {
        checkArgument(target != null, "given target is null");
        checkArgument(source != null, "given source is null");

        copyOperationWorkstationTimes(target, source);
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

    private void copyOperationWorkstationTimes(Entity target, Entity source) {
        if (target.getHasManyField(TECH_OPER_COMP_WORKSTATION_TIMES).isEmpty() && !target.isCopied()) {
            DataDefinition techOperCompWorkstationTimeDD = dataDefinitionService.get(TimeNormsConstants.PLUGIN_IDENTIFIER,
                    TimeNormsConstants.TECH_OPER_COMP_WORKSTATION_TIME);
            List<Entity> techOperCompWorkstationTimes = Lists.newArrayList();
            for (Entity operationWorkstationTime : source.getHasManyField(OperationFieldsTFNO.OPERATION_WORKSTATION_TIMES)) {
                Entity techOperCompWorkstationTime = techOperCompWorkstationTimeDD.create();
                techOperCompWorkstationTime.setField(TechOperCompWorkstationTimeFields.WORKSTATION,
                        operationWorkstationTime.getField(OperationWorkstationTimeFields.WORKSTATION));
                techOperCompWorkstationTime.setField(TechOperCompWorkstationTimeFields.TPZ,
                        operationWorkstationTime.getField(OperationWorkstationTimeFields.TPZ));
                techOperCompWorkstationTime.setField(TechOperCompWorkstationTimeFields.TJ,
                        operationWorkstationTime.getField(OperationWorkstationTimeFields.TJ));
                techOperCompWorkstationTime.setField(TechOperCompWorkstationTimeFields.TIME_NEXT_OPERATION,
                        operationWorkstationTime.getField(OperationWorkstationTimeFields.TIME_NEXT_OPERATION));
                techOperCompWorkstationTimes.add(techOperCompWorkstationTime);
            }
            target.setField(TECH_OPER_COMP_WORKSTATION_TIMES, techOperCompWorkstationTimes);
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

    public boolean onDelete(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        boolean isDeleted = true;

        Entity techOperCompTimeCalculation = technologyOperationComponent
                .getBelongsToField(TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_TIME_CALCULATION);

        if (techOperCompTimeCalculation != null) {
            isDeleted = techOperCompTimeCalculation.getDataDefinition().delete(techOperCompTimeCalculation.getId())
                    .isSuccessfull();
        }

        return isDeleted;
    }

    public void onSave(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        if (technologyOperationComponent.getId() != null) {
            Set<Long> tocWorkstationsIds = technologyOperationComponent
                    .getManyToManyField(TechnologyOperationComponentFields.WORKSTATIONS).stream().map(Entity::getId)
                    .collect(Collectors.toSet());
            Set<Long> oldTocWorkstationsIds = technologyOperationComponentDD.get(technologyOperationComponent.getId())
                    .getManyToManyField(TechnologyOperationComponentFields.WORKSTATIONS).stream().map(Entity::getId)
                    .collect(Collectors.toSet());

            Set<Long> removedWorkstationsIds = Sets.difference(oldTocWorkstationsIds, tocWorkstationsIds);
            List<Long> techOperCompWorkstationTimesIdsToRemove = Lists.newArrayList();
            for (Long id : removedWorkstationsIds) {
                for (Entity techOperCompWorkstationTime : technologyOperationComponent
                        .getHasManyField(TECH_OPER_COMP_WORKSTATION_TIMES)) {
                    if (techOperCompWorkstationTime.getBelongsToField(TechOperCompWorkstationTimeFields.WORKSTATION).getId()
                            .equals(id)) {
                        techOperCompWorkstationTimesIdsToRemove.add(techOperCompWorkstationTime.getId());
                        break;
                    }
                }
            }

            if (!techOperCompWorkstationTimesIdsToRemove.isEmpty()) {
                DataDefinition techOperCompWorkstationTimeDD = dataDefinitionService.get(TimeNormsConstants.PLUGIN_IDENTIFIER,
                        TimeNormsConstants.TECH_OPER_COMP_WORKSTATION_TIME);
                techOperCompWorkstationTimeDD.delete(techOperCompWorkstationTimesIdsToRemove.toArray(new Long[0]));
            }
        }
    }

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        Integer minStaff = entity.getIntegerField(TechnologyOperationComponentFieldsTNFO.MIN_STAFF);
        Integer optimalStaff = entity.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF);
        if (minStaff == null) {
            entity.addError(dataDefinition.getField(TechnologyOperationComponentFieldsTNFO.MIN_STAFF),
                    "qcadooView.validate.field.error.missing");
            return false;
        }
        if (optimalStaff == null) {
            entity.addError(dataDefinition.getField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF),
                    "qcadooView.validate.field.error.missing");
            return false;
        }
        if (minStaff > optimalStaff) {
            entity.addError(dataDefinition.getField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF),
                    "technologies.technologyOperationComponent.validation.error.optimalStaffMustNotBeLessThanMinimumStaff");
            return false;
        }
        if (optimalStaff % minStaff != 0) {
            entity.addError(dataDefinition.getField(OperationFieldsTFNO.OPTIMAL_STAFF),
                    "technologies.technologyOperationComponent.validation.error.optimalStaffMustBeMultipleMinStaff", String.valueOf(minStaff));
            return false;
        }
        return true;
    }
}
