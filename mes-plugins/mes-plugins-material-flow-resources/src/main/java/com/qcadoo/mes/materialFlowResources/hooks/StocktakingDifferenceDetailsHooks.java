package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingDifferenceFields;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingDifferenceType;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class StocktakingDifferenceDetailsHooks {

    private static final String L_ACTIONS = "actions";

    private static final List<String> L_ACTIONS_ITEMS = Arrays.asList("saveBack", "save", "cancel");

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void onBeforeRender(final ViewDefinitionState view) {
        materialFlowResourcesService.fillUnitFieldValues(view);
        FieldComponent typeField = (FieldComponent) view
                .getComponentByReference(StocktakingDifferenceFields.TYPE);
        FieldComponent priceField = (FieldComponent) view
                .getComponentByReference(StocktakingDifferenceFields.PRICE);
        if (StocktakingDifferenceType.SHORTAGE.getStringValue().equals(
                typeField.getFieldValue())) {
            priceField.setEnabled(false);
        }

        FormComponent stocktakingDifferenceForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity stocktakingDifference = stocktakingDifferenceForm.getPersistedEntityWithIncludedFormValues();

        Entity stocktaking = stocktakingDifference.getBelongsToField(StocktakingDifferenceFields.STOCKTAKING);
        String state = stocktaking.getStringField(StocktakingFields.STATE);
        if (StocktakingStateStringValues.REJECTED.equals(state) || StocktakingStateStringValues.FINISHED.equals(state)) {
            stocktakingDifferenceForm.setFormEnabled(false);
            WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
            for (String actionItem : L_ACTIONS_ITEMS) {
                RibbonActionItem ribbonActionItem = window.getRibbon().getGroupByName(L_ACTIONS).getItemByName(actionItem);
                ribbonActionItem.setEnabled(false);
                ribbonActionItem.requestUpdate(true);
            }
        }
    }
}
