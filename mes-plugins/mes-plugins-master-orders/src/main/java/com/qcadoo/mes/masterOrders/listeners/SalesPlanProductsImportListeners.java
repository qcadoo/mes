/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.masterOrders.listeners;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.SalesPlanProductFields;
import com.qcadoo.mes.masterOrders.imports.salesPlanProduct.SalesPlanProductCellBinderRegistry;
import com.qcadoo.mes.masterOrders.imports.salesPlanProduct.SalesPlanProductXlsxImportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class SalesPlanProductsImportListeners {

    @Autowired
    private SalesPlanProductXlsxImportService salesPlanProductXlsxImportService;

    @Autowired
    private SalesPlanProductCellBinderRegistry salesPlanProductCellBinderRegistry;

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        salesPlanProductXlsxImportService.downloadImportSchema(view, MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT, XlsxImportService.L_XLSX);
    }

    public void processImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws IOException {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long salesPlanId = form.getEntityId();
        Entity salesPlan = form.getEntity().getDataDefinition().get(salesPlanId);

        salesPlanProductXlsxImportService.processImportFile(view, salesPlanProductCellBinderRegistry.getCellBinderRegistry(),
                true, MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT, salesPlan,
                SalesPlanProductFields.SALES_PLAN, SalesPlanProductsImportListeners::createRestrictionForProduct);
    }

    private static SearchCriterion createRestrictionForProduct(final Entity product) {
        return SearchRestrictions.belongsTo(SalesPlanProductFields.PRODUCT,
                product.getBelongsToField(SalesPlanProductFields.PRODUCT));
    }

    public void redirectToLogs(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        salesPlanProductXlsxImportService.redirectToLogs(view, MasterOrdersConstants.MODEL_SALES_PLAN_PRODUCT);
    }

    public void onInputChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        salesPlanProductXlsxImportService.changeButtonsState(view, false);
    }

}
