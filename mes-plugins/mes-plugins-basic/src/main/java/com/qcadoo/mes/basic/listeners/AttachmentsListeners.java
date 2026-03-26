package com.qcadoo.mes.basic.listeners;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductAttachmentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class AttachmentsListeners {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentsListeners.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

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

            File zipFile;

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

        state.performEvent(view, "reset");
    }

    public void setPictureAsMain(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("attachmentsGrid");
        Entity selectedAttachment = grid.getSelectedEntities().get(0);

        DataDefinition productAttachmentDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT_ATTACHMENT);
        Entity mainAttachment = productAttachmentDD.find().add(SearchRestrictions.belongsTo(ProductAttachmentFields.PRODUCT,
                selectedAttachment.getBelongsToField(ProductAttachmentFields.PRODUCT))).add(SearchRestrictions.eq(ProductAttachmentFields.MAIN, true)).uniqueResult();
        if (mainAttachment == null || !selectedAttachment.getId().equals(mainAttachment.getId())) {
            if (mainAttachment != null) {
                mainAttachment.setField(ProductAttachmentFields.MAIN, false);
                productAttachmentDD.save(mainAttachment);
            }
            selectedAttachment.setField(ProductAttachmentFields.MAIN, true);
            productAttachmentDD.save(selectedAttachment);
        }
    }

}
