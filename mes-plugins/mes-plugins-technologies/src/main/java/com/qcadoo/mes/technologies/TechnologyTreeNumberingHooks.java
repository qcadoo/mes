package com.qcadoo.mes.technologies;

import static com.qcadoo.mes.technologies.constants.TechnologyState.DRAFT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.utils.TreeNumberingService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyTreeNumberingHooks {

    @Autowired
    private TreeNumberingService treeNumberingService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final Logger LOG = LoggerFactory.getLogger(TechnologyTreeNumberingHooks.class);

    public void rebuildTreeNumbering(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Long technologyId = form.getEntityId();
        if (technologyId == null) {
            return;
        }

        Entity technology = getTechnologyById(technologyId);
        if (!isDraftTechnology(technology)) {
            return;
        }

        EntityTree technologyTree = technology.getTreeField("operationComponents");
        if (technologyTree == null || technologyTree.getRoot() == null) {
            return;
        }

        debug("Fire tree node number generator for tecnology with id = " + technologyId);
        treeNumberingService.generateNumbersAndUpdateTree(technologyTree);
    }

    private Entity getTechnologyById(final Long id) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).get(id);
    }

    private boolean isDraftTechnology(final Entity technology) {
        return DRAFT.getStringValue().equals(technology.getStringField("state"));
    }

    private void debug(final String message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(message);
        }
    }
}
