package com.qcadoo.mes.technologies.hooks;

import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.REFERENCE_TECHNOLOGY;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

@Service
public class TechnologyOperationComponentHooks {

    @Autowired
    private TechnologyService technologyService;

    private static final String L_OPERATION = "operation";

    public void onCreate(final DataDefinition technologyOperationComponentDD, final Entity technologyOperationComponent) {
        copyCommentAndAttachmentFromOperation(technologyOperationComponent);
        setParentIfRootNodeAlreadyExists(technologyOperationComponent);
        copyReferencedTechnology(technologyOperationComponentDD, technologyOperationComponent);
    }

    private void copyCommentAndAttachmentFromOperation(final Entity technologyOperationComponent) {
        technologyService.copyCommentAndAttachmentFromLowerInstance(technologyOperationComponent, OPERATION);
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
        if (rootNode == null || technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.PARENT) != null) {
            return;
        }
        technologyOperationComponent.setField(TechnologyOperationComponentFields.PARENT, rootNode);
    }

    private void copyReferencedTechnology(final DataDefinition technologyOperationComponentDD,
            final Entity technologyOperationComponent) {
        if (!TechnologyOperationComponentFields.REFERENCETECHNOLOGY.equals(technologyOperationComponent
                .getField(TechnologyOperationComponentFields.ENTITY_TYPE))
                && technologyOperationComponent.getField(TechnologyOperationComponentFields.REFERENCETECHNOLOGY) == null) {
            return;
        }

        boolean copy = "02copy".equals(technologyOperationComponent.getField(TechnologyOperationComponentFields.REFERENCEMODE));

        Entity technology = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        Entity referencedTechnology = technologyOperationComponent
                .getBelongsToField(TechnologyOperationComponentFields.REFERENCETECHNOLOGY);

        Set<Long> technologies = new HashSet<Long>();
        technologies.add(technology.getId());

        boolean cyclic = checkForCyclicReferences(technologies, referencedTechnology, copy);

        if (cyclic) {
            technologyOperationComponent.addError(
                    technologyOperationComponentDD.getField(TechnologyOperationComponentFields.REFERENCETECHNOLOGY),
                    "technologies.technologyReferenceTechnologyComponent.error.cyclicDependency");
            return;
        }
        if (copy) {
            EntityTreeNode root = referencedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();
            Entity copiedRoot = copyReferencedTechnologyOperations(root,
                    technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY));

            for (Entry<String, Object> entry : copiedRoot.getFields().entrySet()) {
                if (!(entry.getKey().equals("id") || entry.getKey().equals(TechnologyOperationComponentFields.PARENT))) {
                    technologyOperationComponent.setField(entry.getKey(), entry.getValue());
                }
            }
            technologyOperationComponent.setField(TechnologyOperationComponentFields.ENTITY_TYPE, L_OPERATION);
            technologyOperationComponent.setField(REFERENCE_TECHNOLOGY, null);
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
                List<Entity> copies = new ArrayList<Entity>();
                for (Entity entity : entities) {
                    copies.add(copyReferencedTechnologyOperations(entity, technology));
                }
                copy.setField(entry.getKey(), copies);
            }
        }
        return copy;
    }

    private boolean checkForCyclicReferences(final Set<Long> technologies, final Entity referencedTechnology, final boolean copy) {

        if (!copy && technologies.contains(referencedTechnology.getId())) {
            return true;
        }
        technologies.add(referencedTechnology.getId());

        for (Entity operationComponent : referencedTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS)) {
            if (REFERENCE_TECHNOLOGY.equals(operationComponent.getField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
                boolean cyclic = checkForCyclicReferences(technologies,
                        operationComponent.getBelongsToField(REFERENCE_TECHNOLOGY), false);
                if (cyclic) {
                    return true;
                }
            }
        }
        return false;
    }

}
