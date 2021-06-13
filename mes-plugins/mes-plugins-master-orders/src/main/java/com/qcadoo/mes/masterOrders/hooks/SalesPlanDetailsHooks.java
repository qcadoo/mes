package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.masterOrders.constants.SalesPlanFields;
import com.qcadoo.mes.masterOrders.states.constants.SalesPlanStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SalesPlanDetailsHooks {

    private static final String L_SALES_PLAN_MATERIAL_REQUIREMENT = "salesPlanMaterialRequirement";

    private static final String L_CREATE_SALES_PLAN_MATERIAL_REQUIREMENT = "createSalesPlanMaterialRequirement";

    private static final String L_PRODUCTS = "products";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setRibbonEnabled(view);
        disableForm(view);
    }

    private void disableForm(final ViewDefinitionState view) {
        FormComponent salesPlanForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent productsGrid = (GridComponent) view.getComponentByReference(L_PRODUCTS);
        Entity salesPlan = salesPlanForm.getEntity();
        String salesPlanState = salesPlan.getStringField(SalesPlanFields.STATE);

        if (SalesPlanStateStringValues.COMPLETED.equals(salesPlanState)
                || SalesPlanStateStringValues.REJECTED.equals(salesPlanState)) {
            salesPlanForm.setFormEnabled(false);
            productsGrid.setEnabled(false);
        } else {
            salesPlanForm.setFormEnabled(true);
            productsGrid.setEnabled(true);
        }
    }

    private void setRibbonEnabled(final ViewDefinitionState view) {
        FormComponent salesPlanForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity salesPlan = salesPlanForm.getEntity();
        String state = salesPlan.getStringField(SalesPlanFields.STATE);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup salesPlanMaterialRequirementRibbonGroup = ribbon.getGroupByName(L_SALES_PLAN_MATERIAL_REQUIREMENT);

        RibbonActionItem createSalesPlanMaterialRequirementRibbonActionItem = salesPlanMaterialRequirementRibbonGroup
                .getItemByName(L_CREATE_SALES_PLAN_MATERIAL_REQUIREMENT);

        Long salesPlanId = salesPlanForm.getEntityId();

        boolean isEnabled = Objects.nonNull(salesPlanId);

        if (Objects.nonNull(salesPlanId)
                && (state.equals(SalesPlanStateStringValues.REJECTED) || state.equals(SalesPlanStateStringValues.COMPLETED))) {
            isEnabled = false;
        }

        createSalesPlanMaterialRequirementRibbonActionItem.setEnabled(isEnabled);
        createSalesPlanMaterialRequirementRibbonActionItem.requestUpdate(true);
    }

}
