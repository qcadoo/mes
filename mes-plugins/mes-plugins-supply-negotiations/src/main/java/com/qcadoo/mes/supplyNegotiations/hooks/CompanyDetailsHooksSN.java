/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.supplyNegotiations.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class CompanyDetailsHooksSN {

    



    private static final String L_REDIRECT_TO_FILTERED_REQUESTS_LIST = "redirectToFilteredRequestsList";

    private static final String L_REDIRECT_TO_FILTERED_OFFERS_LIST = "redirectToFilteredOffersList";

    private static final String L_SUPPLIERS_GROUP = "suppliersGroup";

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity company = companyForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        RibbonGroup suppliersGroup = (RibbonGroup) window.getRibbon().getGroupByName(L_SUPPLIERS_GROUP);

        RibbonActionItem redirectToFilteredOffersList = (RibbonActionItem) suppliersGroup
                .getItemByName(L_REDIRECT_TO_FILTERED_OFFERS_LIST);

        RibbonActionItem redirectToFilteredRequestsList = (RibbonActionItem) suppliersGroup
                .getItemByName(L_REDIRECT_TO_FILTERED_REQUESTS_LIST);

        boolean isEnabled = (company.getId() != null);

        updateButtonState(redirectToFilteredOffersList, isEnabled);
        updateButtonState(redirectToFilteredRequestsList, isEnabled);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

}
