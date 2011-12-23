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
package com.qcadoo.mes.simpleMaterialBalance.internal;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
import com.qcadoo.mes.simpleMaterialBalance.internal.constants.SimpleMaterialBalanceConstants;
import com.qcadoo.mes.simpleMaterialBalance.internal.print.SimpleMaterialBalancePdfService;
import com.qcadoo.mes.simpleMaterialBalance.internal.print.SimpleMaterialBalanceXlsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class SimpleMaterialBalanceService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private SimpleMaterialBalancePdfService simpleMaterialBalancePdfService;

    @Autowired
    private SimpleMaterialBalanceXlsService simpleMaterialBalanceXlsService;

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("date", null);
        entity.setField("generated", false);
        entity.setField("fileName", null);
        entity.setField("worker", null);
        return true;
    }

    public void setGenerateButtonState(final ViewDefinitionState state) {
        setGenerateButtonState(state, state.getLocale(), SimpleMaterialBalanceConstants.PLUGIN_IDENTIFIER,
                SimpleMaterialBalanceConstants.MODEL_SIMPLE_MATERIAL_BALANCE);
    }

    public void setGridGenerateButtonState(final ViewDefinitionState state) {
        setGridGenerateButtonState(state, state.getLocale(), SimpleMaterialBalanceConstants.PLUGIN_IDENTIFIER,
                SimpleMaterialBalanceConstants.MODEL_SIMPLE_MATERIAL_BALANCE);
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

            Entity simpleMaterialBalanceEntity = dataDefinitionService.get(plugin, entityName).get(form.getEntityId());

            if (simpleMaterialBalanceEntity.getField("generated") == null) {
                simpleMaterialBalanceEntity.setField("generated", "0");
            }

            if ("1".equals(simpleMaterialBalanceEntity.getField("generated"))) {
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

    public void setGridGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String plugin,
            final String entityName) {
        WindowComponent window = (WindowComponent) state.getComponentByReference("window");
        GridComponent grid = (GridComponent) state.getComponentByReference("grid");
        RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");

        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().size() == 0) {
            deleteButton.setMessage(null);
            deleteButton.setEnabled(false);
        } else {
            boolean canDelete = true;
            for (Long entityId : grid.getSelectedEntitiesIds()) {
                Entity simpleMaterialBalanceEntity = dataDefinitionService.get(plugin, entityName).get(entityId);

                if ((Boolean) simpleMaterialBalanceEntity.getField("generated")) {
                    canDelete = false;
                    break;
                }
            }
            if (canDelete) {
                deleteButton.setMessage(null);
                deleteButton.setEnabled(true);
            } else {
                deleteButton.setMessage("orders.ribbon.message.selectedRecordAlreadyGenerated");
                deleteButton.setEnabled(false);
            }
        }

        deleteButton.requestUpdate(true);
        window.requestRibbonRender();
    }

    @Transactional
    public void generateSimpleMaterialBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

            Entity simpleMaterialBalance = dataDefinitionService.get(SimpleMaterialBalanceConstants.PLUGIN_IDENTIFIER,
                    SimpleMaterialBalanceConstants.MODEL_SIMPLE_MATERIAL_BALANCE).get((Long) state.getFieldValue());

            if (simpleMaterialBalance == null) {
                String message = translationService.translate("qcadooView.message.entityNotFound", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(simpleMaterialBalance.getStringField("fileName"))) {
                String message = translationService.translate(
                        "simpleMaterialBalance.simpleMaterialBalanceDetails.window.simpleMaterialBalance.documentsWasGenerated",
                        state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (simpleMaterialBalance.getHasManyField("orders").isEmpty()) {
                String message = translationService.translate(
                        "simpleMaterialBalance.simpleMaterialBalance.window.simpleMaterialBalance.missingAssosiatedOrders",
                        state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (simpleMaterialBalance.getHasManyField("stockAreas").isEmpty()) {
                String message = translationService.translate(
                        "simpleMaterialBalance.simpleMaterialBalance.window.simpleMaterialBalance.missingAssosiatedStockAreas",
                        state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).format(new Date()));
            }

            state.performEvent(viewDefinitionState, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                worker.setFieldValue(null);
                generated.setFieldValue("0");
                date.setFieldValue(null);
                return;
            }

            simpleMaterialBalance = dataDefinitionService.get(SimpleMaterialBalanceConstants.PLUGIN_IDENTIFIER,
                    SimpleMaterialBalanceConstants.MODEL_SIMPLE_MATERIAL_BALANCE).get((Long) state.getFieldValue());

            try {
                generateSimpleMaterialBalanceDocuments(state, simpleMaterialBalance);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    public void printSimpleMaterialBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity simpleMaterialBalance = dataDefinitionService.get(SimpleMaterialBalanceConstants.PLUGIN_IDENTIFIER,
                    SimpleMaterialBalanceConstants.MODEL_SIMPLE_MATERIAL_BALANCE).get((Long) state.getFieldValue());
            if (simpleMaterialBalance == null) {
                state.addMessage(translationService.translate("qcadooView.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (!StringUtils.hasText(simpleMaterialBalance.getStringField("fileName"))) {
                state.addMessage(
                        translationService
                                .translate(
                                        "simpleMaterialBalance.simpleMaterialBalanceDetails.window.simpleMaterialBalance.documentsWasNotGenerated",
                                        state.getLocale()), MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo(
                        "/generateSavedReport/" + SimpleMaterialBalanceConstants.PLUGIN_IDENTIFIER + "/"
                                + SimpleMaterialBalanceConstants.MODEL_SIMPLE_MATERIAL_BALANCE + "." + args[0] + "?id="
                                + state.getFieldValue() + "&fieldDate=date", true, false);
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

    private void generateSimpleMaterialBalanceDocuments(final ComponentState state, final Entity simpleMaterialBalance)
            throws IOException, DocumentException {
        Entity simpleMaterialBalanceWithFileName = simpleMaterialBalancePdfService.updateFileName(simpleMaterialBalance,
                "Simple_material_balance");
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
        simpleMaterialBalancePdfService.generateDocument(simpleMaterialBalanceWithFileName, company, state.getLocale());
        simpleMaterialBalanceXlsService.generateDocument(simpleMaterialBalanceWithFileName, company, state.getLocale());
    }

}
