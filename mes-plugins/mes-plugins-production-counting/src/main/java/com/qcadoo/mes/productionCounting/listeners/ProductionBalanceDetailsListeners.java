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
package com.qcadoo.mes.productionCounting.listeners;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.xls.ProductionBalanceXlsService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductionBalanceDetailsListeners {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductionBalanceXlsService productionBalanceXlsService;

    @Transactional
    public void generateProductionBalance(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        state.performEvent(view, "save");

        if (!state.isHasError()) {
            Long productionBalanceId = (Long) state.getFieldValue();

            Entity productionBalance = productionCountingService.getProductionBalance(productionBalanceId);

            if (productionBalance == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);

                return;
            } else if (StringUtils.isNotEmpty(productionBalance.getStringField(ProductionBalanceFields.FILE_NAME))) {
                state.addMessage("productionCounting.productionBalance.report.error.documentsWasGenerated", MessageType.FAILURE);

                return;
            }

            if (!productionBalance.getBooleanField(ProductionBalanceFields.GENERATED)) {
                fillReportValues(productionBalance);
            }

            if (!productionBalance.getHasManyField(ProductionBalanceFields.ORDERS).isEmpty()) {
                generateProductionBalanceDocumentXls(productionBalance, state.getLocale());
            } else {
                state.addMessage("productionCounting.productionBalance.report.error.noOrders", MessageType.FAILURE);

                return;
            }
            state.performEvent(view, "reset");

            state.addMessage(
                    "productionCounting.productionBalanceDetails.window.mainTab.productionBalanceDetails.generatedMessage",
                    MessageType.SUCCESS);
        }
    }

    private void fillReportValues(final Entity productionBalance) {
        productionBalance.setField(ProductionBalanceFields.GENERATED, true);
        productionBalance.setField(ProductionBalanceFields.DATE, new Date());
        productionBalance.setField(ProductionBalanceFields.WORKER, securityService.getCurrentUserName());
    }

    private void generateProductionBalanceDocumentXls(final Entity productionBalance, final Locale locale) {
        String localePrefix = "productionCounting.productionBalance.report.fileName";

        Entity productionBalanceWithFileName = fileService.updateReportFileName(productionBalance, ProductionBalanceFields.DATE,
                localePrefix);

        try {
            productionBalanceXlsService.generateDocument(productionBalanceWithFileName, locale);

        } catch (IOException e) {
            throw new IllegalStateException("Problem with saving productionBalance report", e);
        }
    }

    public void printProductionBalance(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printGeneratedReport(view, state, new String[] { args[0], ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_BALANCE });
    }

    public final void addAllRelatedOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(ProductionBalanceFields.ORDERS);
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity balance = form.getPersistedEntityWithIncludedFormValues();
        List<Entity> orders = Lists.newArrayList(balance.getHasManyField(ProductionBalanceFields.ORDERS));
        for (Entity entity : ordersGrid.getSelectedEntities()) {
            Entity root = entity.getBelongsToField("root");
            if (root == null) {
                root = entity;
            }
            orders.add(root);
            List<Entity> children = entity.getDataDefinition().find().add(SearchRestrictions.belongsTo("root",
                    OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER, root.getId())).list().getEntities();
            orders.addAll(children);
        }
        balance.setField(ProductionBalanceFields.ORDERS, orders);
        balance = balance.getDataDefinition().save(balance);
        form.setEntity(balance);
    }
}
