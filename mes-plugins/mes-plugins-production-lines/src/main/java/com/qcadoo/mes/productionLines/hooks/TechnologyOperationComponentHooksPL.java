/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.1
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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionLines.constants.TechOperCompWorkstationFields;
import com.qcadoo.mes.productionLines.constants.TechnologyOperationComponentFieldsPL;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyOperationComponentHooksPL {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void setQuantityOfWorkstationTypes(final ViewDefinitionState view) {
        FormComponent technologyOperationComponentForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent quantityOfWorkstationTypesComponent = (FieldComponent) view
                .getComponentByReference(TechnologyOperationComponentFieldsPL.QUANTITY_OF_WORKSTATION_TYPES);

        Long techologyOperationComponentId = technologyOperationComponentForm.getEntityId();

        if (techologyOperationComponentId == null) {
            return;
        }

        Entity technologyOperationComponent = getTechnologyOperationComponentDD().get(techologyOperationComponentId);

        Entity technology = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);

        if (checkIfTechnologyIsNotNullAndTechnologyTypeIsEmpty(technology)) {
            quantityOfWorkstationTypesComponent.setVisible(false);

            return;
        }

        Entity techOperCompWorkstation = technologyOperationComponent
                .getBelongsToField(TechnologyOperationComponentFieldsPL.TECH_OPER_COMP_WORKSTATION);

        if (techOperCompWorkstation == null) {
            quantityOfWorkstationTypesComponent.setVisible(false);

            return;
        }

        int quantityOfWorkstationTypesDB = techOperCompWorkstation
                .getIntegerField(TechOperCompWorkstationFields.QUANTITY_OF_WORKSTATIONTYPES);

        quantityOfWorkstationTypesComponent.setVisible(true);

        quantityOfWorkstationTypesComponent.setFieldValue(quantityOfWorkstationTypesDB);
        quantityOfWorkstationTypesComponent.requestComponentUpdateState();
    }

    public void save(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        Entity technology = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);

        if (checkIfTechnologyIsNotNullAndTechnologyTypeIsEmpty(technology)) {
            return;
        }

        Integer quantityOfWorkstationTypes = technologyOperationComponent
                .getIntegerField(TechnologyOperationComponentFieldsPL.QUANTITY_OF_WORKSTATION_TYPES);

        if (quantityOfWorkstationTypes == null) {
            return;
        }

        Entity techOperCompWorkstation = technologyOperationComponent
                .getBelongsToField(TechnologyOperationComponentFieldsPL.TECH_OPER_COMP_WORKSTATION);

        if (techOperCompWorkstation == null) {
            techOperCompWorkstation = getTechOperCompWorkstationDD().create();

            techOperCompWorkstation.setField(TechOperCompWorkstationFields.QUANTITY_OF_WORKSTATIONTYPES,
                    quantityOfWorkstationTypes);
        } else {
            techOperCompWorkstation.setField(TechOperCompWorkstationFields.QUANTITY_OF_WORKSTATIONTYPES,
                    quantityOfWorkstationTypes);
        }

        techOperCompWorkstation = techOperCompWorkstation.getDataDefinition().save(techOperCompWorkstation);

        technologyOperationComponent.setField(TechnologyOperationComponentFieldsPL.TECH_OPER_COMP_WORKSTATION,
                techOperCompWorkstation);
    }

    private boolean checkIfTechnologyIsNotNullAndTechnologyTypeIsEmpty(Entity technology) {
        return (technology != null) && StringUtils.isEmpty(technology.getStringField(TechnologyFields.TECHNOLOGY_TYPE));
    }

    private DataDefinition getTechnologyOperationComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

    private DataDefinition getTechOperCompWorkstationDD() {
        return dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_TECH_OPER_COMP_WORKSTATION);
    }

}
