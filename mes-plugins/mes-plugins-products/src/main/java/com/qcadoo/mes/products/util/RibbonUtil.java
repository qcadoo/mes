package com.qcadoo.mes.products.util;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.grid.GridComponentState;
import com.qcadoo.mes.view.components.window.WindowComponentState;
import com.qcadoo.mes.view.ribbon.RibbonActionItem;

@Service
public class RibbonUtil {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @SuppressWarnings("unchecked")
    public void setGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String entityName) {
        WindowComponentState window = (WindowComponentState) state.getComponentByReference("window");
        FormComponentState form = (FormComponentState) state.getComponentByReference("form");
        RibbonActionItem generateButton = window.getRibbon().getGroupByName("generate").getItemByName("generate");
        RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");

        if (form.getEntityId() == null) {
            generateButton.setMessage("recordNotCreated");
            generateButton.setEnabled(false);
            deleteButton.setMessage(null);
            deleteButton.setEnabled(false);
        } else {

            Entity materialRequirementEntity = dataDefinitionService.get("products", entityName).get(form.getEntityId());
            List<Entity> orderComponents = (List<Entity>) materialRequirementEntity.getField("orders");

            if (orderComponents.size() == 0) {
                generateButton.setMessage("products.ribbon.message.noOrders");
                generateButton.setEnabled(false);
                deleteButton.setMessage(null);
                deleteButton.setEnabled(true);
            } else {
                boolean isAnyOrderClosed = false;
                for (Entity orderComponent : orderComponents) {
                    Entity order = orderComponent.getBelongsToField("order");
                    if (order.getField("state").equals("03done")) {
                        isAnyOrderClosed = true;
                        break;
                    }
                }
                if (isAnyOrderClosed && (Boolean) materialRequirementEntity.getField("generated")) {
                    generateButton.setMessage("products.ribbon.message.recordAlreadyGenerated");
                    generateButton.setEnabled(false);
                    deleteButton.setMessage("products.ribbon.message.recordAlreadyGenerated");
                    deleteButton.setEnabled(false);
                } else {
                    generateButton.setMessage(null);
                    generateButton.setEnabled(true);
                    deleteButton.setMessage(null);
                    deleteButton.setEnabled(true);
                }
            }
        }
        generateButton.setShouldBeUpdated(true);
        deleteButton.setShouldBeUpdated(true);
        window.requestRibbonRender();
    }

    @SuppressWarnings("unchecked")
    public void setGridGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String entityName) {
        WindowComponentState window = (WindowComponentState) state.getComponentByReference("window");
        GridComponentState grid = (GridComponentState) state.getComponentByReference("grid");
        RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");

        if (grid.getSelectedEntitiesId() == null || grid.getSelectedEntitiesId().size() == 0) {
            deleteButton.setMessage(null);
            deleteButton.setEnabled(false);
        } else {
            boolean canDelete = true;
            for (Long entityId : grid.getSelectedEntitiesId()) {
                Entity materialRequirementEntity = dataDefinitionService.get("products", entityName).get(entityId);
                List<Entity> orderComponents = (List<Entity>) materialRequirementEntity.getField("orders");
                boolean isAnyOrderClosed = false;
                for (Entity orderComponent : orderComponents) {
                    Entity order = orderComponent.getBelongsToField("order");
                    if (order.getField("state").equals("03done")) {
                        isAnyOrderClosed = true;
                        break;
                    }
                }
                if (isAnyOrderClosed && (Boolean) materialRequirementEntity.getField("generated")) {
                    canDelete = false;
                    break;
                }
            }
            if (canDelete) {
                deleteButton.setMessage(null);
                deleteButton.setEnabled(true);
            } else {
                deleteButton.setMessage("products.ribbon.message.selectedRecordAlreadyGenerated");
                deleteButton.setEnabled(false);
            }
        }

        deleteButton.setShouldBeUpdated(true);
        window.requestRibbonRender();
    }
}
