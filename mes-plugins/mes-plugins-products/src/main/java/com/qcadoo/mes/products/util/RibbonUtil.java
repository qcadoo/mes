package com.qcadoo.mes.products.util;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.form.FormComponentState;
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
        RibbonActionItem generateButton = window.getRibbon().getGroupByName("actions").getItemByName("generate");

        if (form.getEntityId() == null) {
            generateButton.setMessage("recordNotCreated");
            generateButton.setEnabled(false);
        } else {

            Entity materialRequirementEntity = dataDefinitionService.get("products", entityName).get(form.getEntityId());

            if ((Boolean) materialRequirementEntity.getField("generated")) {
                generateButton.setMessage("products.ribbon.message.recordAlreadyGenerated");
                generateButton.setEnabled(false);
            } else {

                List<Entity> orderComponents = (List<Entity>) materialRequirementEntity.getField("orders");

                if (orderComponents.size() == 0) {
                    generateButton.setMessage("products.ribbon.message.noOrders");
                    generateButton.setEnabled(false);
                } else {
                    boolean isAnyOrderClosed = false;
                    for (Entity orderComponent : orderComponents) {
                        Entity order = orderComponent.getBelongsToField("order");
                        if (order.getField("state").equals("03done")) {
                            isAnyOrderClosed = true;
                            break;
                        }
                    }
                    if (isAnyOrderClosed) {
                        generateButton.setMessage("products.ribbon.message.existClosedOrder");
                        generateButton.setEnabled(false);

                    } else {
                        generateButton.setMessage(null);
                        generateButton.setEnabled(true);
                    }
                }

            }
        }
        generateButton.setShouldBeUpdated(true);
        window.requestRibbonRender();
    }

}
