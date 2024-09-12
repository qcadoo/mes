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
package com.qcadoo.mes.timeNormsForOperations;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class NormService {

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private NumberService numberService;

    public List<String> checkOperationOutputQuantities(final Entity technology) {
        List<String> messages = Lists.newArrayList();

        List<Entity> operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        for (Entity operationComponent : operationComponents) {
            BigDecimal timeNormsQuantity = getProductionInOneCycle(operationComponent);

            BigDecimal currentQuantity;

            try {
                currentQuantity = technologyService.getProductCountForOperationComponent(operationComponent);
            } catch (IllegalStateException e) {
                continue;
            }

            if (timeNormsQuantity == null || timeNormsQuantity.compareTo(currentQuantity) != 0) {
                String nodeNumber = operationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER);

                if (nodeNumber == null) {
                    Entity operation = operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

                    if (operation != null) {
                        String name = operation.getStringField(OperationFields.NAME);

                        if (name != null) {
                            messages.add(name);
                        }
                    }
                } else {
                    messages.add(nodeNumber);
                }
            }
        }

        return messages;
    }

    private BigDecimal getProductionInOneCycle(final Entity operationComponent) {
        return operationComponent.getDecimalField(TechnologyOperationComponentFieldsTNFO.PRODUCTION_IN_ONE_CYCLE);
    }

    public Optional<Entity> getTechOperCompWorkstationTime(Entity technologyOperationComponent, long workstationId) {
        List<Entity> techOperCompWorkstationTimes = technologyOperationComponent
                .getHasManyField(TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_WORKSTATION_TIMES);
        for (Entity techOperCompWorkstationTime : techOperCompWorkstationTimes) {
            if (techOperCompWorkstationTime.getBelongsToField(TechOperCompWorkstationTimeFields.WORKSTATION).getId()
                    .equals(workstationId)) {
                return Optional.of(techOperCompWorkstationTime);
            }
        }
        return Optional.empty();
    }

    public BigDecimal getStaffFactor(Entity operationComponent, Integer actualStaff) {
        int minStaff = operationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.MIN_STAFF);
        if (operationComponent
                .getBooleanField(TechnologyOperationComponentFieldsTNFO.TJ_DECREASES_FOR_ENLARGED_STAFF) && actualStaff != null && actualStaff != minStaff) {
            return BigDecimal.valueOf(minStaff).divide(BigDecimal.valueOf(actualStaff), numberService.getMathContext());
        }
        return BigDecimal.ONE;
    }

}
