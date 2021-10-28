package com.qcadoo.mes.masterOrders.hooks;

import java.util.Objects;

import org.springframework.stereotype.Service;

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
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class SalesPlanDetailsHooks {

    private static final String L_SALES_PLAN_MATERIAL_REQUIREMENT = "salesPlanMaterialRequirement";

    private static final String L_CREATE_SALES_PLAN_MATERIAL_REQUIREMENT = "createSalesPlanMaterialRequirement";

    private static final String L_PRODUCTS = "products";

    private static final String L_IMPORT = "import";

    private static final String L_OPEN_POSITIONS_IMPORT_PAGE = "openPositionsImportPage";

    private static final String TECHNOLOGY_NUMBER = "technologyNumber";

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
        GridComponent productsGrid = (GridComponent) view.getComponentByReference(SalesPlanFields.PRODUCTS);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();

        RibbonGroup salesPlanMaterialRequirementRibbonGroup = ribbon.getGroupByName(L_SALES_PLAN_MATERIAL_REQUIREMENT);

        RibbonActionItem createSalesPlanMaterialRequirementRibbonActionItem = salesPlanMaterialRequirementRibbonGroup
                .getItemByName(L_CREATE_SALES_PLAN_MATERIAL_REQUIREMENT);
        RibbonActionItem openPositionsImportPageRibbonActionItem = window.getRibbon().getGroupByName(L_IMPORT)
                .getItemByName(L_OPEN_POSITIONS_IMPORT_PAGE);

        RibbonGroup technologiesRibbonGroup = ribbon.getGroupByName("technologies");
        RibbonActionItem useOtherTechnologyActionItem = technologiesRibbonGroup.getItemByName("useOtherTechnology");
        RibbonActionItem fillTechnologyActionItem = technologiesRibbonGroup.getItemByName("fillTechnology");

        boolean isEnabled = Objects.nonNull(salesPlanForm.getEntityId()) && state.equals(SalesPlanStateStringValues.DRAFT);

        createSalesPlanMaterialRequirementRibbonActionItem.setEnabled(isEnabled);
        createSalesPlanMaterialRequirementRibbonActionItem.requestUpdate(true);
        openPositionsImportPageRibbonActionItem.setEnabled(isEnabled);
        openPositionsImportPageRibbonActionItem.requestUpdate(true);
        useOtherTechnologyActionItem.setEnabled(isEnabled && !productsGrid.getSelectedEntitiesIds().isEmpty()
                && productsGrid.getSelectedEntities().stream().noneMatch(e -> e.getStringField(TECHNOLOGY_NUMBER) == null)
                && productsGrid.getSelectedEntities().stream().map(e -> e.getStringField(TECHNOLOGY_NUMBER)).distinct()
                        .count() == 1L);
        useOtherTechnologyActionItem.requestUpdate(true);
        fillTechnologyActionItem.setEnabled(isEnabled && !productsGrid.getSelectedEntitiesIds().isEmpty()
                && productsGrid.getSelectedEntities().stream()
                        .allMatch(e -> e.getStringField(TECHNOLOGY_NUMBER) == null && e.getStringField("productFamily") != null)
                && productsGrid.getSelectedEntities().stream().map(e -> e.getStringField("productFamily")).distinct()
                        .count() == 1L);
        fillTechnologyActionItem.requestUpdate(true);
    }

}
