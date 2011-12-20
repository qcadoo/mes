/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.print.ProductionCountingPdfService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class ProductionCountingService {

    private static final String FIELD_GENERATED = "generated";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductionCountingPdfService productionCountingPdfService;

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("date", null);
        entity.setField(FIELD_GENERATED, false);
        entity.setField("fileName", null);
        entity.setField("worker", null);
        return true;
    }

    public void setGenerateButtonState(final ViewDefinitionState state) {
        setGenerateButtonState(state, state.getLocale(), ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_COUNTING);
    }

    public void setGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String plugin,
            final String entityName) {
        WindowComponent window = (WindowComponent) state.getComponentByReference("window");
        FormComponent form = (FormComponent) state.getComponentByReference("form");
        RibbonActionItem generateButton = window.getRibbon().getGroupByName("generate").getItemByName("generate");
        RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");

        if (form.getEntityId() == null) {
            generateButton.setMessage("recordNotCreated");
            generateButton.setEnabled(false);
            deleteButton.setMessage(null);
            deleteButton.setEnabled(false);
        } else {
            Entity productionCounting = dataDefinitionService.get(plugin, entityName).get(form.getEntityId());

            if (productionCounting.getField(FIELD_GENERATED) == null) {
                productionCounting.setField(FIELD_GENERATED, "0");
            }

            if ("1".equals(productionCounting.getField(FIELD_GENERATED))) {
                generateButton.setMessage("orders.ribbon.message.recordAlreadyGenerated");
                generateButton.setEnabled(false);
                deleteButton.setMessage("orders.ribbon.message.recordAlreadyGenerated");
                deleteButton.setEnabled(false);
            } else {
                generateButton.setMessage(null);
                generateButton.setEnabled(true);
                deleteButton.setMessage(null);
                deleteButton.setEnabled(true);
            }
        }
        generateButton.requestUpdate(true);
        deleteButton.requestUpdate(true);
        window.requestRibbonRender();
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
                String message = translationService.translate("qcadooView.message.entityNotFound", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(productionCounting.getStringField("fileName"))) {
                String message = translationService.translate(
                        "productionCounting.productionBalance.report.error.documentsWasGenerated", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                name.setEnabled(false);
                order.setEnabled(false);
                description.setEnabled(false);
                date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).format(new Date()));
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
        if (state.getFieldValue() instanceof Long) {
            Entity productionCounting = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingConstants.MODEL_PRODUCTION_COUNTING).get((Long) state.getFieldValue());
            if (productionCounting == null) {
                state.addMessage(translationService.translate("qcadooView.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (!StringUtils.hasText(productionCounting.getStringField("fileName"))) {
                state.addMessage(translationService.translate(
                        "productionCounting.productionBalance.report.error.documentsWasNotGenerated", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo("/generateSavedReport/" + ProductionCountingConstants.PLUGIN_IDENTIFIER + "/"
                        + ProductionCountingConstants.MODEL_PRODUCTION_COUNTING + "." + args[0] + "?id=" + state.getFieldValue()
                        + "&fieldDate=date&suffix=", true, false);
            }
        } else {
            if (state instanceof FormComponent) {
                state.addMessage(translationService.translate("qcadooView.form.entityWithoutIdentifier", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                state.addMessage(translationService.translate("qcadooView.grid.noRowSelectedError", state.getLocale()),
                        MessageType.FAILURE);
            }
        }
    }

    private void generateProductionCountingDocuments(final ComponentState state, final Entity productionCounting)
            throws IOException, DocumentException {
        Entity productionCountingWithFileName = productionCountingPdfService.updateFileName(productionCounting,
                "Production_counting");
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
        productionCountingPdfService.generateDocument(productionCountingWithFileName, company, state.getLocale());
    }

}
