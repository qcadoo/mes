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

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.timeNormsForOperations.constants.OperationFieldsTFNO;
import com.qcadoo.mes.timeNormsForOperations.constants.OperationWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.qcadoo.mes.timeNormsForOperations.constants.OperationFieldsTFNO.OPERATION_WORKSTATION_TIMES;

@Service
public class OperationModelHooksTNFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setProductionInOneCycleUNIT(final DataDefinition operationDD, final Entity operation) {
        Entity product = operation.getBelongsToField(OperationFields.PRODUCT);

        if (Objects.nonNull(product)) {
            operation.setField(TechnologyOperationComponentFieldsTNFO.PRODUCTION_IN_ONE_CYCLE_UNIT,
                    product.getField(ProductFields.UNIT));
        }
    }

    public void onSave(final DataDefinition operationDD, final Entity operation) {
        if (Objects.nonNull(operation.getId())) {
            Set<Long> tocWorkstationsIds = operation.getManyToManyField(OperationFields.WORKSTATIONS).stream().map(Entity::getId)
                    .collect(Collectors.toSet());
            Set<Long> oldTocWorkstationsIds = operationDD.get(operation.getId())
                    .getManyToManyField(OperationFields.WORKSTATIONS).stream().map(Entity::getId).collect(Collectors.toSet());

            Set<Long> removedWorkstationsIds = Sets.difference(oldTocWorkstationsIds, tocWorkstationsIds);
            List<Long> operWorkstationTimesIdsToRemove = Lists.newArrayList();

            for (Long id : removedWorkstationsIds) {
                for (Entity operWorkstationTime : operation.getHasManyField(OPERATION_WORKSTATION_TIMES)) {
                    if (operWorkstationTime.getBelongsToField(OperationWorkstationTimeFields.WORKSTATION).getId().equals(id)) {
                        operWorkstationTimesIdsToRemove.add(operWorkstationTime.getId());
                        break;
                    }
                }
            }

            if (!operWorkstationTimesIdsToRemove.isEmpty()) {
                getOperationWorkstationTimeDD().delete(operWorkstationTimesIdsToRemove.toArray(new Long[0]));
            }
        }
    }

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        Integer minStaff = entity.getIntegerField(OperationFieldsTFNO.MIN_STAFF);
        Integer optimalStaff = entity.getIntegerField(OperationFieldsTFNO.OPTIMAL_STAFF);

        if (Objects.isNull(minStaff)) {
            entity.addError(dataDefinition.getField(OperationFieldsTFNO.MIN_STAFF),
                    "qcadooView.validate.field.error.missing");

            return false;
        }
        if (Objects.isNull(optimalStaff)) {
            entity.addError(dataDefinition.getField(OperationFieldsTFNO.OPTIMAL_STAFF),
                    "qcadooView.validate.field.error.missing");

            return false;
        }
        if (minStaff > optimalStaff) {
            entity.addError(dataDefinition.getField(OperationFieldsTFNO.OPTIMAL_STAFF),
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

    private DataDefinition getOperationWorkstationTimeDD() {
        return dataDefinitionService.get(TimeNormsConstants.PLUGIN_IDENTIFIER,
                TimeNormsConstants.OPERATION_WORKSTATION_TIME);
    }

}
