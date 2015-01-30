/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.technologies.hooks;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentReferenceMode;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

@Service
public class TechnologyOperationComponentHooks {

    @Autowired
    private TechnologyService technologyService;

    public void onCreate(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        copyCommentAndAttachmentFromOperation(technologyOperationComponent);
        setParentIfRootNodeAlreadyExists(technologyOperationComponent);
        copyReferencedTechnology(technologyOperationComponentDD, technologyOperationComponent);
        copyWorkstationsSettingsFromOperation(technologyOperationComponent);
    }

    private void copyWorkstationsSettingsFromOperation(final Entity technologyOperationComponent) {
        Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

        if (operation != null) {
            technologyOperationComponent.setField(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS,
                    operation.getIntegerField(OperationFields.QUANTITY_OF_WORKSTATIONS));
            technologyOperationComponent.setField(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION,
                    operation.getField(OperationFields.ASSIGNED_TO_OPERATION));
            technologyOperationComponent.setField(TechnologyOperationComponentFields.WORKSTATION_TYPE,
                    operation.getBelongsToField(OperationFields.WORKSTATION_TYPE));
            technologyOperationComponent.setField(TechnologyOperationComponentFields.WORKSTATIONS,
                    operation.getManyToManyField(OperationFields.WORKSTATIONS));
            technologyOperationComponent.setField(TechnologyOperationComponentFields.DIVISION,
                    operation.getBelongsToField(OperationFields.DIVISION));
            technologyOperationComponent.setField(TechnologyOperationComponentFields.PRODUCTION_LINE,
                    operation.getBelongsToField(OperationFields.PRODUCTION_LINE));
        }
    }

    private void copyCommentAndAttachmentFromOperation(final Entity technologyOperationComponent) {
        technologyService.copyCommentAndAttachmentFromLowerInstance(technologyOperationComponent,
                TechnologyOperationComponentFields.OPERATION);
    }

    private void setParentIfRootNodeAlreadyExists(final Entity technologyOperationComponent) {
        Entity technology = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        EntityTree tree = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

        if (tree == null) {
            return;
        }

        if (tree.isEmpty()) {
            return;
        }

        EntityTreeNode rootNode = tree.getRoot();

        if ((rootNode == null)
                || (technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.PARENT) != null)) {
            return;
        }

        technologyOperationComponent.setField(TechnologyOperationComponentFields.PARENT, rootNode);
    }

    private void copyReferencedTechnology(final DataDefinition technologyOperationComponentDD,
            final Entity technologyOperationComponent) {
        if (!TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY.equals(technologyOperationComponent
                .getField(TechnologyOperationComponentFields.ENTITY_TYPE))
                && (technologyOperationComponent.getField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY) == null)) {
            return;
        }

        boolean isCopy = TechnologyOperationComponentReferenceMode.COPY.getStringValue().equals(
                technologyOperationComponent.getStringField(TechnologyOperationComponentFields.REFERENCE_MODE));

        Entity technology = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        Entity referencedTechnology = technologyOperationComponent
                .getBelongsToField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY);

        Set<Long> technologies = Sets.newHashSet();
        technologies.add(technology.getId());

        boolean isCyclic = checkForCyclicReferences(technologies, referencedTechnology, isCopy);

        if (isCyclic) {
            technologyOperationComponent.addError(
                    technologyOperationComponentDD.getField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY),
                    "technologies.technologyReferenceTechnologyComponent.error.cyclicDependency");

            return;
        }

        if (isCopy) {
            EntityTreeNode root = referencedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

            if (root == null) {
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

            technologyOperationComponent.setField(TechnologyOperationComponentFields.ENTITY_TYPE,
                    TechnologyOperationComponentType.OPERATION.getStringValue());
            technologyOperationComponent.setField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY, null);
        }
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

                for (Entity entity : entities) {
                    copies.add(copyReferencedTechnologyOperations(entity, technology));
                }

                copy.setField(entry.getKey(), copies);
            }
        }

        return copy;
    }

    private boolean checkForCyclicReferences(final Set<Long> technologies, final Entity referencedTechnology,
            final boolean copy) {
        if (!copy && technologies.contains(referencedTechnology.getId())) {
            return true;
        }

        technologies.add(referencedTechnology.getId());

        for (Entity technologyOperationComponent : referencedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS)) {
            if (TechnologyOperationComponentType.REFERENCE_TECHNOLOGY.getStringValue().equals(
                    technologyOperationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
                boolean isCyclic = checkForCyclicReferences(technologies,
                        technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY),
                        false);

                if (isCyclic) {
                    return true;
                }
            }
        }

        return false;
    }

    public void onSave(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        clearField(technologyOperationComponent);

    }

    private void clearField(final Entity technologyOperationComponent) {
        String assignedToOperation = technologyOperationComponent
                .getStringField(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION);
        if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperation)) {
            technologyOperationComponent.setField(TechnologyOperationComponentFields.WORKSTATIONS, null);
        }
    }
}
