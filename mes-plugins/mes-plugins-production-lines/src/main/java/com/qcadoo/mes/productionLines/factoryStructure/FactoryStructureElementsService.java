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
