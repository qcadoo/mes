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
package com.qcadoo.mes.basic.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.WorkstationAttachmentFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class WorkstationDetailsListeners {

    private static final Logger LOG = LoggerFactory.getLogger(WorkstationDetailsListeners.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private FileService fileService;

    public void downloadAtachment(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(WorkstationFields.ATTACHMENTS);

        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().size() == 0) {
            state.addMessage("basic.workstationDetails.window.ribbon.atachments.nonSelectedAtachment",
                    ComponentState.MessageType.INFO);

            return;
        }

        DataDefinition workstationAttachmentDD = getWorkstationAttachmentDD();

        List<File> attachements = Lists.newArrayList();

        for (Long workstationAttachmentId : grid.getSelectedEntitiesIds()) {
            Entity workstationAttachment = workstationAttachmentDD.get(workstationAttachmentId);

            File attachment = new File(workstationAttachment.getStringField(WorkstationAttachmentFields.ATTACHMENT));

            attachements.add(attachment);
        }

        File zipFile = null;

        try {
            zipFile = fileService.compressToZipFile(attachements, false);
        } catch (IOException e) {
            LOG.error("Unable to compress documents to zip file.", e);
            return;
        }

        view.redirectTo(fileService.getUrl(zipFile.getAbsolutePath()) + "?clean", true, false);
    }

    private DataDefinition getWorkstationAttachmentDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_WORKSTATION_ATTACHMENT);
    }

}
