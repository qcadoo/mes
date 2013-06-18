/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingFields;
import com.qcadoo.mes.productionCounting.print.ProductionCountingPdfService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class ProductionCountingDetailsListeners {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductionCountingPdfService productionCountingPdfService;

    public void fillProductAndProductionRecordWhenOrderChanged(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        productionCountingService.fillFieldsFromProduct(view);
        productionCountingService.fillProductionRecordsGrid(view);

        addErrorWhenOrderWithoutRecordingType(view);
    }

    private void addErrorWhenOrderWithoutRecordingType(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionCountingFields.ORDER);

        Entity order = orderLookup.getEntity();

        if (order == null) {
            return;
        }

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (productionCountingService.checkIfTypeOfProductionRecordingIsEmptyOrBasic(typeOfProductionRecording)) {
            orderLookup.addMessage("productionCounting.productionBalance.report.error.orderWithoutRecordingType",
                    ComponentState.MessageType.FAILURE);
        }
    }

    @Transactional
    public void generateProductionCounting(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        state.performEvent(view, "save", new String[0]);

        if (!state.isHasError()) {
            Long productionCountingId = (Long) state.getFieldValue();

            Entity productionCounting = productionCountingService.getProductionCounting(productionCountingId);

            if (productionCounting == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);

                return;
            } else if (StringUtils.hasText(productionCounting.getStringField(ProductionCountingFields.FILE_NAME))) {
                state.addMessage("productionCounting.productionBalance.report.error.documentsWasGenerated", MessageType.FAILURE);

                return;
            }

            if (!productionCounting.getBooleanField(ProductionCountingFields.GENERATED)) {
                fillReportValues(productionCounting);
            }

            productionCounting = productionCountingService.getProductionBalance(productionCountingId);

            try {
                generateProductionCountingDocuments(state, productionCounting);

                state.performEvent(view, "reset", new String[0]);

                state.addMessage(
                        "productionCounting.productionCountingDetails.window.mainTab.productionCountingDetails.generatedMessage",
                        MessageType.SUCCESS);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private void fillReportValues(final Entity productionCounting) {
        productionCounting.setField(ProductionCountingFields.GENERATED, true);
        productionCounting.setField(ProductionCountingFields.DATE, new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT,
                LocaleContextHolder.getLocale()).format(new Date()));
        productionCounting.setField(ProductionCountingFields.WORKER, securityService.getCurrentUserName());
    }

    private void generateProductionCountingDocuments(final ComponentState state, final Entity productionCounting)
            throws IOException, DocumentException {
        String localePrefix = "productionCounting.productionCounting.report.fileName";

        Entity productionCountingWithFileName = fileService.updateReportFileName(productionCounting,
                ProductionCountingFields.DATE, localePrefix);

        try {
            productionCountingPdfService.generateDocument(productionCountingWithFileName, state.getLocale());
        } catch (IOException e) {
            throw new IllegalStateException("Problem with saving productionCounting report");
        } catch (DocumentException e) {
            throw new IllegalStateException("Problem with generating productionCounting report");
        }
    }

    public void printProductionCounting(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        reportService.printGeneratedReport(view, state, new String[] { args[0], ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_COUNTING });
    }

}
