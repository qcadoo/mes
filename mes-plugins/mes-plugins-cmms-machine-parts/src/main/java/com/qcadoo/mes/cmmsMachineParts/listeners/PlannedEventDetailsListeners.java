package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventAttachmentFields;
import com.qcadoo.mes.cmmsMachineParts.hooks.EventHooks;
import com.qcadoo.mes.cmmsMachineParts.hooks.PlannedEventDetailsHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class PlannedEventDetailsListeners {

    private static final Logger LOG = LoggerFactory.getLogger(PlannedEventDetailsListeners.class);

    @Autowired
    private PlannedEventDetailsHooks plannedEventDetailsHooks;

    @Autowired
    private EventHooks eventHooks;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private FileService fileService;

    public void toggleEnabledFromBasedOn(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        eventHooks.toggleEnabledFromBasedOn(view);
    }

    public void toggleFieldsVisible(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        plannedEventDetailsHooks.toggleFieldsVisible(view);
    }

    public void downloadAttachment(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("attachments");
        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().size() == 0) {
            state.addMessage("cmmsMachineParts.plannedEventDetails.window.ribbon.attachments.nonSelectedAttachment",
                    ComponentState.MessageType.INFO);
            return;
        }
        DataDefinition attachmentDD = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_PLANNED_EVENT_ATTACHMENT);
        List<File> atachments = Lists.newArrayList();
        for (Long attachmentId : grid.getSelectedEntitiesIds()) {
            Entity attachment = attachmentDD.get(attachmentId);
            File file = new File(attachment.getStringField(PlannedEventAttachmentFields.ATTACHMENT));
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
}
