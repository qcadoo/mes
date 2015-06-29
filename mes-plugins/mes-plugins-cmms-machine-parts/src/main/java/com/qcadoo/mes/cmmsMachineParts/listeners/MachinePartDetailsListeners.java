package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyAttachmentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.view.api.components.GridComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.cmmsMachineParts.hooks.MachinePartDetailsHooks;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class MachinePartDetailsListeners {

    private static final Logger LOG = LoggerFactory.getLogger(MachinePartDetailsListeners.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private FileService fileService;

    @Autowired
    private MachinePartDetailsHooks machinePartDetailsHooks;

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    public void redirectToWarehouseStateList(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity product = form.getEntity();

        if (product.getId() == null) {
            return;
        }

        String productNumber = product.getStringField("number");

        if (productNumber == null) {
            return;
        }

        Map<String, String> filters = Maps.newHashMap();
        filters.put("productNumber", applyInOperator(productNumber));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "materialFlow.warehouseStock");

        String url = "../page/materialFlowResources/warehouseStocksList.html";
        view.redirectTo(url, false, true, parameters);
    }

    private String applyInOperator(final String value) {
        StringBuilder builder = new StringBuilder();
        return builder.append("[").append(value).append("]").toString();
    }

    public void toggleSuppliersGrids(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        machinePartDetailsHooks.toggleSuppliersGrids(view);
    }

    public void downloadAtachment(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("machinePartAttachments");
        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().size() == 0) {
            state.addMessage("technologies.technologyDetails.window.ribbon.atachments.nonSelectedAtachment", ComponentState.MessageType.INFO);
            return;
        }
        DataDefinition attachmentDD = dataDefinitionService.get("cmmsMachineParts",
                "machinePartAttachment");
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
}
