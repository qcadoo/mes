package com.qcadoo.mes.productionLines.factoryStructure;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.DivisionFields;
import com.qcadoo.mes.basic.constants.FactoryFields;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.productionLines.constants.DivisionFieldsPL;
import com.qcadoo.mes.productionLines.constants.FactoryStructureElementFields;
import com.qcadoo.mes.productionLines.constants.FactoryStructureElementType;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;

@Service
public class FactoryStructureGenerationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public EntityTree generateFactoryStructureForWorkstation(final Entity workstationEntity) {
        Entity workstation = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION).get(
                workstationEntity.getId());
        Entity division = workstation.getBelongsToField(WorkstationFields.DIVISION);
        if (workstation.getId() == null || division == null) {
            return null;
        }
        Entity factory = division.getBelongsToField(DivisionFields.FACTORY);
        if (factory == null) {
            return null;
        }

        List<Entity> factoryStructureList = Lists.newArrayList();
        Entity root = addRoot(factoryStructureList, workstation, FactoryStructureElementFields.WORKSTATION);
        generateFactoryStructure(factoryStructureList, root, workstation, FactoryStructureElementFields.WORKSTATION);
        EntityTree factoryStructure = EntityTreeUtilsService.getDetachedEntityTree(factoryStructureList);

        return factoryStructure;
    }

    public EntityTree generateFactoryStructureForSubassembly(final Entity subassemblyEntity) {
        Entity subassembly = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBASSEMBLY).get(
                subassemblyEntity.getId());
        Entity workstation = subassembly.getBelongsToField(SubassemblyFields.WORKSTATION);
        if (subassembly.getId() == null || workstation == null) {
            return null;
        }
        Entity division = workstation.getBelongsToField(WorkstationFields.DIVISION);
        if (division == null) {
            return null;
        }
        Entity factory = division.getBelongsToField(DivisionFields.FACTORY);
        if (factory == null) {
            return null;
        }

        List<Entity> factoryStructureList = Lists.newArrayList();
        Entity root = addRoot(factoryStructureList, subassembly, FactoryStructureElementFields.SUBASSEMBLY);
        generateFactoryStructure(factoryStructureList, root, subassembly, FactoryStructureElementFields.SUBASSEMBLY);
        EntityTree factoryStructure = EntityTreeUtilsService.getDetachedEntityTree(factoryStructureList);

        return factoryStructure;
    }

    private void generateFactoryStructure(List<Entity> tree, final Entity root, final Entity belongsToEntity,
            final String belongsToField) {
        List<Entity> factories = getFactories();
        for (Entity factory : factories) {
            if (!factory.isActive()) {
                continue;
            }
            Entity factoryNode = createNode(belongsToEntity, belongsToField, factory.getStringField(FactoryFields.NUMBER),
                    factory.getStringField(FactoryFields.NAME), FactoryStructureElementType.FACTORY);
            addChild(tree, factoryNode, root);

            List<Entity> divisions = getDivisionsForFactory(factory);
            for (Entity division : divisions) {
                if (!division.isActive()) {
                    continue;
                }
                Entity divisionNode = createNode(belongsToEntity, belongsToField, division.getStringField(DivisionFields.NUMBER),
                        division.getStringField(DivisionFields.NAME), FactoryStructureElementType.DIVISION);
                addChild(tree, divisionNode, factoryNode);

                List<Entity> productionLines = getProductionLinesForDivision(division);

                for (Entity productionLine : productionLines) {
                    if (!productionLine.isActive()) {
                        continue;
                    }
                    Entity productionLineNode = createNode(belongsToEntity, belongsToField,
                            productionLine.getStringField(ProductionLineFields.NUMBER),
                            productionLine.getStringField(ProductionLineFields.NAME), FactoryStructureElementType.PRODUCTION_LINE);
                    addChild(tree, productionLineNode, divisionNode);

                    List<Entity> workstations = getWorkstationsForProductionLineAndDivision(productionLine, division);

                    for (Entity workstation : workstations) {
                        if (!workstation.isActive()) {
                            continue;
                        }
                        Entity workstationNode = createNode(belongsToEntity, belongsToField,
                                workstation.getStringField(WorkstationFields.NUMBER),
                                workstation.getStringField(WorkstationFields.NAME), FactoryStructureElementType.WORKSTATION);
                        if (areEntitiesEqual(workstation, belongsToEntity)) {
                            workstationNode.setField(FactoryStructureElementFields.CURRENT, true);
                        }

                        addChild(tree, workstationNode, productionLineNode);

                        List<Entity> subassemblies = getSubassembliesForWorkstation(workstation);
                        for (Entity subassembly : subassemblies) {
                            if (!subassembly.isActive()) {
                                continue;
                            }
                            Entity subassemblyNode = createNode(belongsToEntity, belongsToField,
                                    subassembly.getStringField(SubassemblyFields.NUMBER),
                                    subassembly.getStringField(SubassemblyFields.NAME), FactoryStructureElementType.SUBASSEMBLY);
                            if (areEntitiesEqual(subassembly, belongsToEntity)) {
                                subassemblyNode.setField(FactoryStructureElementFields.CURRENT, true);
                            }

                            addChild(tree, subassemblyNode, workstationNode);

                        }

                    }
                }
            }
        }
    }

    private Entity addRoot(List<Entity> tree, final Entity belongsToEntity, final String belongsToField) {

        Entity company = parameterService.getParameter().getBelongsToField(ParameterFields.COMPANY);

        Entity root = createNode(belongsToEntity, belongsToField, company.getStringField(CompanyFields.NUMBER),
                company.getStringField(CompanyFields.NAME), FactoryStructureElementType.COMPANY);

        addChild(tree, root, null);
        return root;
    }

    private void addChild(List<Entity> tree, final Entity child, final Entity parent) {
        child.setField(FactoryStructureElementFields.PARENT, parent);
        child.setId((long) tree.size() + 1);
        child.setField(FactoryStructureElementFields.NODE_NUMBER, child.getId());
        child.setField(FactoryStructureElementFields.PRIORITY, 1);
        tree.add(child);
    }

    private Entity createNode(final Entity belongsToEntity, final String belongsToField, final String number, final String name,
            final FactoryStructureElementType entityType) {

        DataDefinition elementDD = dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_FACTORY_STRUCTURE_ELEMENT);
        Entity node = elementDD.create();

        node.setField(belongsToField, belongsToEntity);
        node.setField(FactoryStructureElementFields.NUMBER, number);
        node.setField(FactoryStructureElementFields.NAME, name);
        node.setField(FactoryStructureElementFields.ENTITY_TYPE, entityType.getStringValue());
        node.setField(FactoryStructureElementFields.CURRENT, false);
        return node;
    }

    private List<Entity> getFactories() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_FACTORY).find().list()
                .getEntities();
    }

    private List<Entity> getDivisionsForFactory(final Entity factory) {
        return factory.getHasManyField(FactoryFields.DIVISIONS);
    }

    private List<Entity> getProductionLinesForDivision(final Entity division) {
        return division.getManyToManyField(DivisionFieldsPL.PRODUCTION_LINES);
    }

    private List<Entity> getWorkstationsForProductionLineAndDivision(final Entity productionLine, final Entity division) {
        return productionLine.getHasManyField(ProductionLineFields.WORKSTATIONS).find()
                .add(SearchRestrictions.belongsTo(WorkstationFields.DIVISION, division)).list().getEntities();
    }

    private List<Entity> getSubassembliesForWorkstation(final Entity workstation) {
        return workstation.getHasManyField(WorkstationFields.SUBASSEMBLIES);
    }

    private boolean areEntitiesEqual(final Entity firstEntity, final Entity secondEntity) {
        return firstEntity.getDataDefinition().getName().equals(secondEntity.getDataDefinition().getName())
                && firstEntity.getDataDefinition().getPluginIdentifier()
                        .equals(secondEntity.getDataDefinition().getPluginIdentifier())
                && firstEntity.getId().equals(secondEntity.getId());
    }
}
