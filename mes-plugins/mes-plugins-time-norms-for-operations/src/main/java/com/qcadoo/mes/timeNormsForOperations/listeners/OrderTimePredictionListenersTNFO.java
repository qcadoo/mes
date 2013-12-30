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
package com.qcadoo.mes.timeNormsForOperations.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompTimeCalculationsFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderTimePredictionListenersTNFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public static final String L_TECHNOLOGY = "technology";

    public void clearValueOnTechnologyChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(L_TECHNOLOGY);
        Entity technology = technologyLookup.getEntity();
        if (technology == null) {
            return;
        }

        List<Entity> operationsComponentsEntities = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);

        for (Entity operationEntity : operationsComponentsEntities) {
            Entity toctc = operationEntity.getBelongsToField(TechnologyOperCompTNFOFields.TECH_OPER_COMP_TIME_CALCULATIONS);
            toctc.setField(TechOperCompTimeCalculationsFields.DURATION, null);
            toctc.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM, null);
            toctc.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_TO, null);
            toctc.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_OPERATION_REALIZATION_TIME, null);
            toctc.setField(TechOperCompTimeCalculationsFields.LABOR_WORK_TIME, null);
            toctc.setField(TechOperCompTimeCalculationsFields.MACHINE_WORK_TIME, null);
            toctc.setField(TechOperCompTimeCalculationsFields.OPERATION_OFF_SET, null);

            toctc = toctc.getDataDefinition().save(toctc);
        }

    }

}
