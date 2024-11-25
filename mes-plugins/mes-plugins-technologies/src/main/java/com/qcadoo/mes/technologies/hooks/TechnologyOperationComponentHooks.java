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
package com.qcadoo.mes.technologies.hooks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class TechnologyOperationComponentHooks {

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onCreate(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        copyCommentAndAttachmentFromOperation(technologyOperationComponent);
        setParentIfRootNodeAlreadyExists(technologyOperationComponent);
        setOperationOutProduct(technologyOperationComponent);
        copyReferencedTechnology(technologyOperationComponentDD, technologyOperationComponent);
        copyWorkstationsSettingsFromOperation(technologyOperationComponent);
    }

    public void copyWorkstationsSettingsFromOperation(final Entity technologyOperationComponent) {
        Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

        if (Objects.nonNull(operation)) {
            technologyOperationComponent.setField(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS,
                    operation.getIntegerField(OperationFields.QUANTITY_OF_WORKSTATIONS));
            technologyOperationComponent.setField(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION,
                    operation.getField(OperationFields.ASSIGNED_TO_OPERATION));
            technologyOperationComponent.setField(TechnologyOperationComponentFields.WORKSTATION_TYPE,
                    operation.getBelongsToField(OperationFields.WORKSTATION_TYPE));
            if (!technologyOperationComponent.isCopied()) {
                technologyOperationComponent.setField(TechnologyOperationComponentFields.WORKSTATIONS,
                        operation.getManyToManyField(OperationFields.WORKSTATIONS));
            }
            technologyOperationComponent.setField(TechnologyOperationComponentFields.DIVISION,
                    operation.getBelongsToField(OperationFields.DIVISION));
        }
    }

    private void copyCommentAndAttachmentFromOperation(final Entity technologyOperationComponent) {
        technologyService.copyCommentAndAttachmentFromLowerInstance(technologyOperationComponent,
                TechnologyOperationComponentFields.OPERATION);
    }

    private void setParentIfRootNodeAlreadyExists(final Entity technologyOperationComponent) {
        Entity technology = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        EntityTree tree = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        if (Objects.isNull(tree) || tree.isEmpty()) {
            return;
        }

        EntityTreeNode rootNode = tree.getRoot();

        if (Objects.isNull(rootNode)
                || Objects.nonNull(technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.PARENT))) {
            return;
        }

        technologyOperationComponent.setField(TechnologyOperationComponentFields.PARENT, rootNode);
    }

    private void setOperationOutProduct(final Entity technologyOperationComponent) {
        if (Objects.nonNull(
                technologyOperationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS))
                && technologyOperationComponent
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).isEmpty()) {
            Entity technology = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
            EntityTree tree = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

            Entity operationProductOutComponent = getOperationProductOutComponentDD().create();

            operationProductOutComponent.setField(OperationProductOutComponentFields.QUANTITY, 1);

            if (Objects.isNull(tree) || tree.isEmpty()) {
                operationProductOutComponent.setField(OperationProductOutComponentFields.PRODUCT,
                        technology.getBelongsToField(TechnologyFields.PRODUCT));

                technologyOperationComponent.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS,
                        Collections.singletonList(operationProductOutComponent));
            } else {
                Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

                if (Objects.nonNull(operation)) {
                    Entity product = operation.getBelongsToField(OperationFields.PRODUCT);

                    if (Objects.nonNull(product)) {
                        operationProductOutComponent.setField(OperationProductOutComponentFields.PRODUCT, product);

                        technologyOperationComponent.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS,
                                Collections.singletonList(operationProductOutComponent));
                    }
                }
            }
        }
    }

    private void copyReferencedTechnology(final DataDefinition technologyOperationComponentDD,
                                          final Entity technologyOperationComponent) {
        if (Objects.isNull(technologyOperationComponent.getField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY))) {
            return;
        }

        Entity technology = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        Entity referencedTechnology = technologyOperationComponent
                .getBelongsToField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY);

        Set<Long> technologies = Sets.newHashSet();
        technologies.add(technology.getId());

        boolean isCyclic = checkForCyclicReferences(technologies, referencedTechnology);

        if (isCyclic) {
            technologyOperationComponent.addError(
                    technologyOperationComponentDD.getField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY),
                    "technologies.technologyReferenceTechnologyComponent.error.cyclicDependency");

            return;
        }

        EntityTreeNode root = referencedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

        if (Objects.isNull(root)) {
            technologyOperationComponent.addError(
                    technologyOperationComponentDD.getField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY),
                    "technologies.technologyReferenceTechnologyComponent.error.operationComponentsEmpty");

            return;
        }

        Entity copiedRoot = copyReferencedTechnologyOperations(root,
                technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY));

        for (Entry<String, Object> entry : copiedRoot.getFields().entrySet()) {
            if (!(entry.getKey().equals("id") || entry.getKey().equals(TechnologyOperationComponentFields.PARENT))) {
                technologyOperationComponent.setField(entry.getKey(), entry.getValue());
            }
        }

        technologyOperationComponent.setField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY, null);
        technologyOperationComponent.setCopied(true);
    }

    private Entity copyReferencedTechnologyOperations(final Entity node, final Entity technology) {
        Entity copy = node.copy();

        copy.setId(null);
        copy.setField(TechnologyOperationComponentFields.PARENT, null);
        copy.setField(TechnologyOperationComponentFields.TECHNOLOGY, technology);

        for (Entry<String, Object> entry : node.getFields().entrySet()) {
            Object value = entry.getValue();

            if (value instanceof EntityList) {
                EntityList entities = (EntityList) value;

                List<Entity> copies = Lists.newArrayList();

                if (entry.getKey().equals(TechnologyOperationComponentFields.CHILDREN)) {
                    for (Entity entity : entities) {
                        copies.add(copyReferencedTechnologyOperations(entity, technology));
                    }
                } else if (entry.getKey().equals(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS)) {
                    for (Entity entity : entities) {
                        Entity fieldCopy = entity.copy();

                        fieldCopy.setId(null);
                        fieldCopy.setField(OperationProductInComponentFields.SECTIONS, copySections(entity.getHasManyField(OperationProductInComponentFields.SECTIONS)));

                        copies.add(fieldCopy);
                    }
                } else {
                    for (Entity entity : entities) {
                        Entity fieldCopy = entity.copy();

                        fieldCopy.setId(null);

                        copies.add(fieldCopy);
                    }
                }

                copy.setField(entry.getKey(), copies);
            }
        }

        copy.setField("productionCountingQuantities", null);
        copy.setField("productionCountingOperationRuns", null);
        copy.setField("operationalTasks", null);
        copy.setField("operCompTimeCalculations", null);
        copy.setField("barcodeOperationComponents", null);
        copy.setCopied(true);

        return copy;
    }

    private List<Entity> copySections(final List<Entity> entities) {
        List<Entity> copies = Lists.newArrayList();

        for (Entity entity : entities) {
            Entity fieldCopy = entity.copy();

            fieldCopy.setId(null);

            copies.add(fieldCopy);
        }

        return copies;
    }

    private boolean checkForCyclicReferences(final Set<Long> technologies, final Entity referencedTechnology) {
        return technologies.contains(referencedTechnology.getId());

    }

    public void onSave(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        clearField(technologyOperationComponentDD, technologyOperationComponent);

        Long technologyOperationComponentId = technologyOperationComponent.getId();

        if (Objects.nonNull(technologyOperationComponentId)) {
            copyWorkstations(technologyOperationComponentDD, technologyOperationComponent);

            setTechnologicalProcessListAssignDate(technologyOperationComponentDD, technologyOperationComponent,
                    technologyOperationComponentId);
        }
        fillRangeAndDivision(technologyOperationComponentDD, technologyOperationComponent);
    }

    private void fillRangeAndDivision(DataDefinition technologyOperationComponentDD, Entity technologyOperationComponent) {
        if(technologyOperationComponent.isCopied()) {
            return;
        }
        Entity division = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.DIVISION);
        if (division != null) {
            Long technologyOperationComponentId = technologyOperationComponent.getId();
            Entity technologyOperationComponentDB = null;
            if (technologyOperationComponentId != null) {
                technologyOperationComponentDB = technologyOperationComponentDD.get(technologyOperationComponentId);
            }
            if (technologyOperationComponentId == null || technologyOperationComponentDB.getBelongsToField(TechnologyOperationComponentFields.DIVISION) == null
                    || !division.equals(technologyOperationComponentDB.getBelongsToField(TechnologyOperationComponentFields.DIVISION))) {
                Entity technology = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
                technology = technology.getDataDefinition().get(technology.getId());
                List<Entity> tocs = getTechnologyOperationComponents(technologyOperationComponentDD, technology);
                Set<Long> divisionIds = tocs.stream()
                        .filter(e -> technologyOperationComponentId == null || !e.getId().equals(technologyOperationComponentId))
                        .filter(e -> e.getBelongsToField(TechnologyOperationComponentFields.DIVISION) != null)
                        .map(e -> e.getBelongsToField(TechnologyOperationComponentFields.DIVISION).getId()).collect(Collectors.toSet());
                if (divisionIds.size() > 1 || divisionIds.size() == 1 && !divisionIds.contains(division.getId())) {
                    technology.setField(TechnologyFields.RANGE, Range.MANY_DIVISIONS.getStringValue());
                    technology.setField(TechnologyFields.DIVISION, null);
                    technology.getDataDefinition().save(technology);
                } else if (!Range.ONE_DIVISION.getStringValue().equals(technology.getField(TechnologyFields.RANGE))
                        || !Objects.equals(technology.getField(TechnologyFields.DIVISION), division)) {
                    technology.setField(TechnologyFields.RANGE, Range.ONE_DIVISION.getStringValue());
                    technology.setField(TechnologyFields.DIVISION, division);
                    Long[] productionLinesIds = technology.getHasManyField(TechnologyFields.PRODUCTION_LINES).stream().map(Entity::getId).toArray(Long[]::new);
                    if (productionLinesIds.length > 0) {
                        getTechnologyProductionLineDD().delete(productionLinesIds);
                    }
                    technology.getDataDefinition().fastSave(technology);
                }
            }
        }
    }

    private void setTechnologicalProcessListAssignDate(final DataDefinition technologyOperationComponentDD,
                                                       final Entity technologyOperationComponent, final Long technologyOperationComponentId) {
        Entity technologyOperationComponentFromDB = technologyOperationComponentDD.get(technologyOperationComponentId);

        Date technologicalProcessListAssignmentDate = technologyOperationComponent
                .getDateField(TechnologyOperationComponentFields.TECHNOLOGICAL_PROCESS_LIST_ASSIGNMENT_DATE);
        Entity technologicalProcessList = technologyOperationComponent
                .getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGICAL_PROCESS_LIST);
        Entity technologicalProcessListFromDB = technologyOperationComponentFromDB
                .getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGICAL_PROCESS_LIST);

        boolean areSame = (Objects.isNull(technologicalProcessList) ? Objects.isNull(technologicalProcessListFromDB)
                : (Objects.nonNull(technologicalProcessListFromDB)
                && technologicalProcessList.getId().equals(technologicalProcessListFromDB.getId())));

        if (Objects.nonNull(technologicalProcessList)) {
            if (Objects.isNull(technologicalProcessListAssignmentDate) || !areSame) {
                technologyOperationComponent.setField(
                        TechnologyOperationComponentFields.TECHNOLOGICAL_PROCESS_LIST_ASSIGNMENT_DATE, DateTime.now().toDate());
            }
        } else {
            technologyOperationComponent.setField(TechnologyOperationComponentFields.TECHNOLOGICAL_PROCESS_LIST_ASSIGNMENT_DATE,
                    null);
        }
    }

    private void copyWorkstations(final DataDefinition technologyOperationComponentDD,
                                  final Entity technologyOperationComponent) {
        Entity oldToc = technologyOperationComponentDD.get(technologyOperationComponent.getId());
        Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

        if (Objects.nonNull(operation)
                && !operation.getId().equals(oldToc.getBelongsToField(TechnologyOperationComponentFields.OPERATION).getId())) {

            technologyOperationComponent.setField(TechnologyOperationComponentFields.DIVISION,
                    operation.getBelongsToField(OperationFields.DIVISION));
            technologyOperationComponent.setField(TechnologyOperationComponentFields.WORKSTATIONS,
                    operation.getManyToManyField(TechnologyOperationComponentFields.WORKSTATIONS));
        }
    }

    private void clearField(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        String assignedToOperation = technologyOperationComponent
                .getStringField(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION);

        if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperation)) {
            technologyOperationComponent.setField(TechnologyOperationComponentFields.WORKSTATIONS, null);
        }
        Long technologyOperationComponentId = technologyOperationComponent.getId();

        if (Objects.nonNull(technologyOperationComponentId)) {
            Entity technologyOperationComponentDB = technologyOperationComponentDD.get(technologyOperationComponentId);
            if (technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.DIVISION) != null
                    && technologyOperationComponentDB.getBelongsToField(TechnologyOperationComponentFields.DIVISION) == null
                    || technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.DIVISION) == null
                    && technologyOperationComponentDB.getBelongsToField(TechnologyOperationComponentFields.DIVISION) != null
                    || technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.DIVISION) != null
                    && !technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.DIVISION).equals(technologyOperationComponentDB.getBelongsToField(TechnologyOperationComponentFields.DIVISION))) {
                technologyOperationComponent.setField(TechnologyOperationComponentFields.WORKSTATIONS, null);
            }
        }
    }

    public boolean onDelete(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        List<Entity> usageInProductStructureTree = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_PRODUCT_STRUCTURE_TREE_NODE).find()
                .add(SearchRestrictions.belongsTo(ProductStructureTreeNodeFields.OPERATION, technologyOperationComponent)).list()
                .getEntities();

        if (!usageInProductStructureTree.isEmpty()) {
            technologyOperationComponent.addGlobalError(
                    "technologies.technologyDetails.window.treeTab.technologyTree.error.cannotDeleteOperationUsedInProductStructureTree",
                    false,
                    usageInProductStructureTree.stream()
                            .map(e -> e.getBelongsToField(ProductStructureTreeNodeFields.MAIN_TECHNOLOGY)
                                    .getStringField(TechnologyFields.NUMBER))
                            .distinct().collect(Collectors.joining(", ")));

            return false;
        }

        return true;
    }

    private DataDefinition getOperationProductOutComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);
    }

    private List<Entity> getTechnologyOperationComponents(DataDefinition technologyOperationComponentDD, final Entity technology) {
        return technologyOperationComponentDD.find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology)).list()
                .getEntities();
    }

    private DataDefinition getTechnologyProductionLineDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_PRODUCTION_LINE);
    }
}
