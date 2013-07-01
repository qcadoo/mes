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
package com.qcadoo.mes.technologies.validators;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductValidatorsT {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void checkIfProductUnitChange(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        Long productID = (Long) componentState.getFieldValue();

        if (productID == null) {
            return;
        }

        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
        List<Entity> technolgies = technologyDD.find()
                .add(SearchRestrictions.in("state", Lists.newArrayList("02accepted", "05checked"))).list().getEntities();

        boolean isValid = true;
        Map<Long, String> wrongTechnlgies = Maps.newHashMap();

        for (Entity technology : technolgies) {
            for (Entity operationComponent : technology.getTreeField("operationComponents")) {
                if (!operationComponent.getDataDefinition().callValidators(operationComponent)) {
                    isValid = false;
                    wrongTechnlgies.put(technology.getId(), technology.getStringField(TechnologyFields.NUMBER));
                }
            }
        }

        if (!isValid) {
            ComponentState form = (ComponentState) viewDefinitionState.getComponentByReference("form");
            form.addMessage("technologies.technology.validate.product.OperationTreeNotValid", MessageType.INFO, false,
                    buildArgs(wrongTechnlgies));
        }

    }

    private String buildArgs(final Map<Long, String> wrongTechnlgies) {
        StringBuilder builder = new StringBuilder();
        for (String technologyNumber : wrongTechnlgies.values()) {
            builder.append(technologyNumber);
            builder.append(", ");
        }
        return builder.toString();
    }
}
