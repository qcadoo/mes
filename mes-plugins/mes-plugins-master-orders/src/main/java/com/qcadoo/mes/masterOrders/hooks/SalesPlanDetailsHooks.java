package com.qcadoo.mes.masterOrders.hooks;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class SalesPlanDetailsHooks {

    private static final String L_SALES_PLAN_MATERIAL_REQUIREMENT = "salesPlanMaterialRequirement";

    private static final String L_CREATE_SALES_PLAN_MATERIAL_REQUIREMENT = "createSalesPlanMaterialRequirement";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setRibbonEnabled(view);
    }

    private void setRibbonEnabled(final ViewDefinitionState view) {
        FormComponent salesPlanForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup salesPlanMaterialRequirementRibbonGroup = ribbon.getGroupByName(L_SALES_PLAN_MATERIAL_REQUIREMENT);

        RibbonActionItem createSalesPlanMaterialRequirementRibbonActionItem = salesPlanMaterialRequirementRibbonGroup
                .getItemByName(L_CREATE_SALES_PLAN_MATERIAL_REQUIREMENT);

        Long salesPlanId = salesPlanForm.getEntityId();

        boolean isEnabled = Objects.nonNull(salesPlanId);

        createSalesPlanMaterialRequirementRibbonActionItem.setEnabled(isEnabled);
        createSalesPlanMaterialRequirementRibbonActionItem.requestUpdate(true);
    }

}
