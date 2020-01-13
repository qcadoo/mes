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
package com.qcadoo.mes.technologies.imports.operationProductInComponent;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OperationProductInComponentXlsxImportService extends XlsxImportService {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void validateEntity(final Entity operationProductInComponent, final DataDefinition operationProductInComponentDD) {
        validateOperationComponent(operationProductInComponent, operationProductInComponentDD);
    }

    private void validateOperationComponent(final Entity operationProductInComponent,
            final DataDefinition operationProductInComponentDD) {
        Entity technology = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.TECHNOLOGY);

        Entity operationComponent = operationProductInComponent
                .getBelongsToField(OperationProductInComponentFields.OPERATION_COMPONENT);

        if (Objects.nonNull(technology) && Objects.nonNull(operationComponent)) {
            Entity operationComponentTechnology = operationComponent
                    .getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
            String nodeNumber = operationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER);

            if (Objects.nonNull(operationComponentTechnology) && !technology.equals(operationComponentTechnology)) {
                operationComponent = getTechnologyOperationComponentFromTechnologyByNodeNumber(technology, nodeNumber);

                if (Objects.isNull(operationComponent)) {
                    operationProductInComponent.addError(
                            operationProductInComponentDD.getField(OperationProductInComponentFields.OPERATION_COMPONENT),
                            L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
                } else {
                    operationProductInComponent.setField(OperationProductInComponentFields.OPERATION_COMPONENT,
                            operationComponent);
                }
            }
        }
    }

    private Entity getTechnologyOperationComponentFromTechnologyByNodeNumber(final Entity technology, final String nodeNumber) {
        return getTechnologyOperationComponentDD().find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology))
                .add(SearchRestrictions.eq(TechnologyOperationComponentFields.NODE_NUMBER, nodeNumber)).setMaxResults(1)
                .uniqueResult();
    }

    private DataDefinition getTechnologyOperationComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

}
