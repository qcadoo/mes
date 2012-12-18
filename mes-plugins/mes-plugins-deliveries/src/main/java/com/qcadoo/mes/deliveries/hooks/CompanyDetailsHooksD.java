package com.qcadoo.mes.deliveries.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class CompanyDetailsHooksD {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_SUPPLIERS = "suppliers";

    private static final String L_REDIRECT_TO_FILTERED_DELIVERIES_LIST = "redirectToFilteredDeliveriesList";

    @Autowired
    private CompanyService companyService;

    public void disabledGridWhenCompanyIsAnOwner(final ViewDefinitionState state) {
        companyService.disabledGridWhenCompanyIsAnOwner(state, "productsFamilies", "products");
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity company = companyForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

        RibbonGroup suppliers = window.getRibbon().getGroupByName(L_SUPPLIERS);

        RibbonActionItem redirectToFilteredDeliveriesList = suppliers.getItemByName(L_REDIRECT_TO_FILTERED_DELIVERIES_LIST);

        if (company.getId() == null) {
            updateButtonState(redirectToFilteredDeliveriesList, false);
        } else {
            updateButtonState(redirectToFilteredDeliveriesList, true);
        }
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

}
