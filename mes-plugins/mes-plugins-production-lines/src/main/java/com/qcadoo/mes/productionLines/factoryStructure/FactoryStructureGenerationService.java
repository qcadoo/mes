package com.qcadoo.mes.productionLines.factoryStructure;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.DivisionFields;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.productionLines.constants.FactoryStructureElementFields;
import com.qcadoo.mes.productionLines.constants.FactoryStructureElementType;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;

@Service
public class FactoryStructureGenerationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public EntityTree generateFactoryStructureForWorkstation(final Entity workstation) {
        Entity division = workstation.getBelongsToField(WorkstationFields.DIVISION);
        if (workstation.getId() == null || division == null) {
            return null;
        }
        Entity factory = division.getBelongsToField(DivisionFields.FACTORY);
        if (factory == null) {
            return null;
        }

        List<Entity> factoryStructureList = Lists.newArrayList();
        addRoot(factoryStructureList, workstation, FactoryStructureElementFields.WORKSTATION);
        EntityTree factoryStructure = EntityTreeUtilsService.getDetachedEntityTree(factoryStructureList);

        return factoryStructure;
    }

    private void addRoot(List<Entity> tree, final Entity belongsToEntity, final String belongsToField) {

        Entity company = parameterService.getParameter().getBelongsToField(ParameterFields.COMPANY);
        DataDefinition elementDD = dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_FACTORY_STRUCTURE_ELEMENT);

        Entity root = elementDD.create();

        root.setField(belongsToField, belongsToEntity);
        root.setField(FactoryStructureElementFields.NUMBER, company.getStringField(CompanyFields.NUMBER));
        root.setField(FactoryStructureElementFields.NAME, company.getStringField(CompanyFields.NAME));
        addChild(tree, root, null, FactoryStructureElementType.COMPANY.getStringValue());
    }

    private void addChild(final List<Entity> tree, final Entity child, final Entity parent, final String entityType) {
        child.setField(FactoryStructureElementFields.PARENT, parent);
        child.setId((long) tree.size() + 1);
        child.setField(FactoryStructureElementFields.NODE_NUMBER, child.getId());
        child.setField(FactoryStructureElementFields.PRIORITY, 1);
        child.setField(FactoryStructureElementFields.ENTITY_TYPE, entityType);

        tree.add(child);
    }
}
