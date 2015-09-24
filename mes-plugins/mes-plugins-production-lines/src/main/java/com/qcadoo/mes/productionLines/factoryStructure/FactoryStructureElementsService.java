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
package com.qcadoo.mes.productionLines.factoryStructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.productionLines.constants.FactoryStructureElementFields;
import com.qcadoo.mes.productionLines.constants.FactoryStructureElementType;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class FactoryStructureElementsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity getRelatedEntity(final Entity factoryStructureElement) {
        if (factoryStructureElement == null) {
            return null;
        }
        FactoryStructureElementType type = FactoryStructureElementType.of(factoryStructureElement);
        Long id = (Long) factoryStructureElement.getField(FactoryStructureElementFields.ENTITY_ID);
        if (type.compareTo(FactoryStructureElementType.COMPANY) == 0) {
            return getEntityById(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY, id);
        } else if (type.compareTo(FactoryStructureElementType.FACTORY) == 0) {
            return getEntityById(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_FACTORY, id);
        } else if (type.compareTo(FactoryStructureElementType.DIVISION) == 0) {
            return getEntityById(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_DIVISION, id);
        } else if (type.compareTo(FactoryStructureElementType.PRODUCTION_LINE) == 0) {
            return getEntityById(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE, id);
        } else if (type.compareTo(FactoryStructureElementType.WORKSTATION) == 0) {
            return getEntityById(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION, id);
        } else if (type.compareTo(FactoryStructureElementType.SUBASSEMBLY) == 0) {
            return getEntityById(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBASSEMBLY, id);
        }
        return null;
    }

    public Entity getEntityById(final String pluginIdentifier, final String modelName, final Long id) {
        return dataDefinitionService.get(pluginIdentifier, modelName).get(id);
    }
}
