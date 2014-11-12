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
package com.qcadoo.mes.technologies.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyAttachmentFields;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.TreeComponent;
import com.qcadoo.view.api.components.WindowComponent;

@Service
public class TechnologyDetailsListeners {

    private static final Logger LOG = LoggerFactory.getLogger(TechnologyDetailsListeners.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    private static final String OUT_PRODUCTS_REFERENCE = "outProducts";

    private static final String IN_PRODUCTS_REFERENCE = "inProducts";

    private static final String TECHNOLOGY_TREE_REFERENCE = "technologyTree";

    public void setGridEditable(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        setGridEditable(view);
    }

    public void setGridEditable(final ViewDefinitionState view) {
        final TreeComponent technologyTree = (TreeComponent) view.getComponentByReference(TECHNOLOGY_TREE_REFERENCE);
        final boolean gridsShouldBeEnabled = technologyTree.getSelectedEntityId() != null;
        for (String componentReference : Sets.newHashSet(OUT_PRODUCTS_REFERENCE, IN_PRODUCTS_REFERENCE)) {
            view.getComponentByReference(componentReference).setEnabled(gridsShouldBeEnabled);
        }
    }

    public void downloadAtachment(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("technologyAttachments");
        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().size() == 0) {
            state.addMessage("technologies.technologyDetails.window.ribbon.atachments.nonSelectedAtachment", MessageType.INFO);
            return;
        }
        DataDefinition attachmentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_ATTACHMENT);
        List<File> atachments = Lists.newArrayList();
        for (Long confectionProtocolId : grid.getSelectedEntitiesIds()) {
            Entity attachment = attachmentDD.get(confectionProtocolId);
            File file = new File(attachment.getStringField(TechnologyAttachmentFields.ATTACHMENT));
            atachments.add(file);
        }

        File zipFile = null;
        try {
            zipFile = fileService.compressToZipFile(atachments, false);
        } catch (IOException e) {
            LOG.error("Unable to compress documents to zip file.", e);
            return;
        }

        view.redirectTo(fileService.getUrl(zipFile.getAbsolutePath()) + "?clean", true, false);
    }

    public void generateProductStructure(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        FormComponent productStructureForm = (FormComponent) view.getComponentByReference("productStructureForm");
        Entity technology = form.getEntity();
        Entity productTechnology = technology.copy();
        EntityTree generatedTree = productStructureTreeService.generateProductStructureTree(view, technology);

        productTechnology.setField("productStructureTree", generatedTree);
        productStructureForm.setEntity(productTechnology);
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        window.setActiveTab("productStructure");
    }
}
