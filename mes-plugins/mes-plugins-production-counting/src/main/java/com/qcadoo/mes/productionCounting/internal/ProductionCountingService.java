/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.productionCounting.internal;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.print.ProductionCountingPdfService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionCountingService {

    private static final String FIELD_GENERATED = "generated";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ProductionCountingPdfService productionCountingPdfService;

    @Autowired
    private ReportService reportService;

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("date", null);
        entity.setField(FIELD_GENERATED, false);
        entity.setField("fileName", null);
        entity.setField("worker", null);
        return true;
    }

    @Transactional
    public void generateProductionCounting(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = viewDefinitionState.getComponentByReference(FIELD_GENERATED);
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");
            FieldComponent name = (FieldComponent) viewDefinitionState.getComponentByReference("name");
            FieldComponent description = (FieldComponent) viewDefinitionState.getComponentByReference("description");
            FieldComponent order = (FieldComponent) viewDefinitionState.getComponentByReference("order");
            Entity productionCounting = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingConstants.MODEL_PRODUCTION_COUNTING).get((Long) state.getFieldValue());

            if (productionCounting == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(productionCounting.getStringField("fileName"))) {
                state.addMessage("productionCounting.productionBalance.report.error.documentsWasGenerated", MessageType.FAILURE);
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                name.setEnabled(false);
                order.setEnabled(false);
                description.setEnabled(false);
                date.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, LocaleContextHolder.getLocale())
                        .format(new Date()));
                requestComponentUpdateState(viewDefinitionState);
            }

            state.performEvent(viewDefinitionState, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                worker.setFieldValue(null);
                generated.setFieldValue("0");
                date.setFieldValue(null);
                return;
            }

            productionCounting = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingConstants.MODEL_PRODUCTION_COUNTING).get((Long) state.getFieldValue());

            try {
                generateProductionCountingDocuments(state, productionCounting);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private void requestComponentUpdateState(final ViewDefinitionState view) {
        for (String reference : Arrays.asList("name", "description", "order")) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(reference);
            component.requestComponentUpdateState();
        }
    }

    public void printProductionCounting(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        reportService.printGeneratedReport(viewDefinitionState, state, new String[] { args[0],
                ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_COUNTING });
    }

    private void generateProductionCountingDocuments(final ComponentState state, final Entity productionCounting)
            throws IOException, DocumentException {
        Entity productionCountingWithFileName = fileService.updateReportFileName(productionCounting, "date",
                "productionCounting.productionCounting.report.fileName");
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
        productionCountingPdfService.generateDocument(productionCountingWithFileName, company, state.getLocale());
    }

}
