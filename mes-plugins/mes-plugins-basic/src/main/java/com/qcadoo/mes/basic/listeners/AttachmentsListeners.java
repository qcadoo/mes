package com.qcadoo.mes.basic.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class AttachmentsListeners {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentsListeners.class);

    @Autowired
    private FileService fileService;

    public void downloadAttachment(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("attachmentsGrid");

        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().size() == 0) {
            state.addMessage("basic.productDetails.window.ribbon.attachments.nonSelectedAttachment",
                    ComponentState.MessageType.INFO);
            return;
        }

        if (grid.getSelectedEntitiesIds().size() > 1) {
            List<File> attachments = Lists.newArrayList();

            for (Entity attachment : grid.getSelectedEntities()) {
                File file = new File(attachment.getStringField("attachment"));
                attachments.add(file);
            }

            File zipFile = null;

            try {
                zipFile = fileService.compressToZipFile(attachments, false);
            } catch (IOException e) {
                LOG.error("Unable to compress documents to zip file.", e);
                return;
            }

            view.redirectTo(fileService.getUrl(zipFile.getAbsolutePath()) + "?clean", true, false);
        } else {
            File file = new File(grid.getSelectedEntities().stream().findFirst().get().getStringField("attachment"));
            view.redirectTo(fileService.getUrl(file.getAbsolutePath()), true, false);
        }

        state.performEvent(view, "reset", new String[0]);
    }

}
