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
package com.qcadoo.mes.masterOrders.hooks;

import com.beust.jcommander.internal.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.SalesVolumeFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SalesVolumeDetailsHooks {

    private static final String L_ORDERS = "orders";

    private static final String L_CREATE_ORDER = "createOrder";

    @Autowired
    private TranslationService translationService;

    public void onBeforeRender(final ViewDefinitionState view) {
        setRibbonEnabled(view);
        setUnitFields(view);
    }

    private void setRibbonEnabled(final ViewDefinitionState view) {
        FormComponent salesVolumeForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        Ribbon ribbon = window.getRibbon();

        RibbonGroup ordersRibbonGroup = ribbon.getGroupByName(L_ORDERS);

        RibbonActionItem createOrdersRibbonActionItem = ordersRibbonGroup.getItemByName(L_CREATE_ORDER);

        Long salesVolumeId = salesVolumeForm.getEntityId();

        boolean isSaved = Objects.nonNull(salesVolumeId);

        createOrdersRibbonActionItem.setEnabled(isSaved);
        createOrdersRibbonActionItem.requestUpdate(true);
    }

    private void setUnitFields(final ViewDefinitionState view) {
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(SalesVolumeFields.PRODUCT);

        Entity product = productLookup.getEntity();

        String unit = null;

        if (Objects.nonNull(product)) {
            unit = product.getStringField(ProductFields.UNIT);
        }

        for (String fieldName : Lists.newArrayList("dailySalesVolumeUnit", "optimalStockUnit", "currentStockUnit", "stockForDaysUnit")) {
            FieldComponent unitField = (FieldComponent) view.getComponentByReference(fieldName);

            if ("stockForDaysUnit".equals(fieldName)) {
                unitField.setFieldValue(translationService.translate("masterOrders.salesVolumeDetails.window.mainTab.salesVolume.stockForDaysUnit", LocaleContextHolder.getLocale()));
            } else {
                unitField.setFieldValue(unit);
            }

            unitField.requestComponentUpdateState();
        }
    }

}
