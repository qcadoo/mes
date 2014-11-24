/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionBalanceService;
import com.qcadoo.mes.productionCounting.ProductionCountingGenerateProductionBalance;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.print.ProductionBalancePdfService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductionBalanceDetailsListeners {

    private static final String L_EMPTY_NUMBER = "";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductionBalancePdfService productionBalancePdfService;

    @Autowired
    private ProductionCountingGenerateProductionBalance generateProductionBalance;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductionBalanceService productionBalanceService;

    @Autowired
    private OrderService orderService;

    @Transactional
    public void generateProductionBalance(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        state.performEvent(view, "save", new String[0]);

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

                productionBalanceService.fillFieldsAndGrids(productionBalance);
            }

            checkOrderDoneQuantity(state, productionBalance);

            try {
                generateProductionBalanceDocuments(productionBalance, state.getLocale());

                state.performEvent(view, "reset", new String[0]);

                state.addMessage(
                        "productionCounting.productionBalanceDetails.window.mainTab.productionBalanceDetails.generatedMessage",
                        MessageType.SUCCESS);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private void fillReportValues(final Entity productionBalance) {
        productionBalance.setField(ProductionBalanceFields.GENERATED, true);
        productionBalance.setField(ProductionBalanceFields.DATE, new Date());
        productionBalance.setField(ProductionBalanceFields.WORKER, securityService.getCurrentUserName());
    }

    private void checkOrderDoneQuantity(final ComponentState componentState, final Entity productionBalance) {
        final Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);
        final BigDecimal doneQuantityFromOrder = order.getDecimalField(OrderFields.DONE_QUANTITY);

        if (doneQuantityFromOrder == null || BigDecimal.ZERO.compareTo(doneQuantityFromOrder) == 0) {
            componentState.addMessage("productionCounting.productionBalance.report.info.orderWithoutDoneQuantity",
                    MessageType.INFO);
        }
    }

    private void generateProductionBalanceDocuments(final Entity productionBalance, final Locale locale) throws IOException,
            DocumentException {
        String localePrefix = "productionCounting.productionBalance.report.fileName";

        Entity productionBalanceWithFileName = fileService.updateReportFileName(productionBalance, ProductionBalanceFields.DATE,
                localePrefix);

        try {
            productionBalancePdfService.generateDocument(productionBalanceWithFileName, locale);

            generateProductionBalance.notifyObserversThatTheBalanceIsBeingGenerated(productionBalance);
        } catch (IOException e) {
            throw new IllegalStateException("Problem with saving productionBalance report");
        } catch (DocumentException e) {
            throw new IllegalStateException("Problem with generating productionBalance report");
        }
    }

    public void printProductionBalance(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printGeneratedReport(view, state, new String[] { args[0], ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, args[1] });
    }

    public void fillProductAndTrackingsNumber(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent orderLookup = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.ORDER);

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            clearProductAndTrackingsNumber(view);

            return;
        }

        Entity order = orderService.getOrder(orderId);

        if (order == null) {
            clearProductAndTrackingsNumber(view);
            return;
        }

        if (productionCountingService.isTypeOfProductionRecordingBasic(order
                .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            clearProductAndTrackingsNumber(view);

            orderLookup.addMessage("productionCounting.productionBalance.report.error.orderWithoutRecordingType",
                    ComponentState.MessageType.FAILURE);

            return;
        }

        fillProductAndTrackingsNumber(view, order);
    }

    private void fillProductAndTrackingsNumber(final ViewDefinitionState view, final Entity order) {
        FieldComponent productField = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.PRODUCT);
        FieldComponent trackingsNumberField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.TRACKINGS_NUMBER);

        Entity product = order.getBelongsToField(OrderFields.PRODUCT);

        Integer trackingsNumber = productionCountingService.getProductionTrackingsForOrder(order).size();

        productField.setFieldValue(product.getId());
        trackingsNumberField.setFieldValue(trackingsNumber);
    }

    private void clearProductAndTrackingsNumber(final ViewDefinitionState view) {
        FieldComponent productField = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.PRODUCT);
        FieldComponent trackingsNumberField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.TRACKINGS_NUMBER);

        productField.setFieldValue(null);
        trackingsNumberField.setFieldValue(null);
    }

    public void setDefaultNameUsingOrder(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        if (!(component instanceof FieldComponent)) {
            return;
        }

        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionBalanceFields.ORDER);
        FieldComponent nameField = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.NAME);

        Entity order = orderLookup.getEntity();

        if (order == null) {
            return;
        }

        String name = (String) nameField.getFieldValue();
        Locale locale = component.getLocale();
        String defaultName = makeDefaultName(order, locale);

        if (StringUtils.isEmpty(name) || !defaultName.equals(name)) {
            nameField.setFieldValue(defaultName);
        }
    }

    public String makeDefaultName(final Entity order, final Locale locale) {
        String orderNumber = L_EMPTY_NUMBER;

        if (order != null) {
            orderNumber = order.getStringField(OrderFields.NUMBER);
        }

        Calendar cal = Calendar.getInstance(locale);
        cal.setTime(new Date());

        return translationService.translate("productionCounting.productionBalance.name.default", locale, orderNumber,
                cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH));
    }

    public void disableCheckboxes(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productionBalanceService.disableCheckboxes(view);
    }

}
