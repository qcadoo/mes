/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
