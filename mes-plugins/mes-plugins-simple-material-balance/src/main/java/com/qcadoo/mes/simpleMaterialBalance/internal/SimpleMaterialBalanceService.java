/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.simpleMaterialBalance.internal.constants.SimpleMaterialBalanceConstants;
import com.qcadoo.mes.simpleMaterialBalance.internal.print.SimpleMaterialBalancePdfService;
import com.qcadoo.mes.simpleMaterialBalance.internal.print.SimpleMaterialBalanceXlsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.ReportService;
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

    private static final String L_DATE = "date";

    private static final String L_WORKER = "worker";

    private static final String L_FILE_NAME = "fileName";

    private static final String L_GENERATED = "generated";

    private static final String L_SIMPLE_MATERIAL_BALANCE_ORDERS_COMPONENTS = "simpleMaterialBalanceOrdersComponents";

    private static final String L_SIMPLE_MATERIAL_BALANCE_LOCATIONS_COMPONENTS = "simpleMaterialBalanceLocationsComponents";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private SimpleMaterialBalancePdfService simpleMaterialBalancePdfService;

    @Autowired
    private SimpleMaterialBalanceXlsService simpleMaterialBalanceXlsService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(L_DATE, null);
        entity.setField(L_GENERATED, false);
        entity.setField(L_FILE_NAME, null);
        entity.setField(L_WORKER, null);
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

            if (simpleMaterialBalanceEntity.getField(L_GENERATED) == null) {
                simpleMaterialBalanceEntity.setField(L_GENERATED, "0");
            }

            if ("1".equals(simpleMaterialBalanceEntity.getField(L_GENERATED))) {
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

                if ((Boolean) simpleMaterialBalanceEntity.getField(L_GENERATED)) {
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
            ComponentState generated = viewDefinitionState.getComponentByReference(L_GENERATED);
            ComponentState date = viewDefinitionState.getComponentByReference(L_DATE);
            ComponentState worker = viewDefinitionState.getComponentByReference(L_WORKER);

            Entity simpleMaterialBalance = dataDefinitionService.get(SimpleMaterialBalanceConstants.PLUGIN_IDENTIFIER,
                    SimpleMaterialBalanceConstants.MODEL_SIMPLE_MATERIAL_BALANCE).get((Long) state.getFieldValue());

            if (simpleMaterialBalance == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(simpleMaterialBalance.getStringField(L_FILE_NAME))) {
                state.addMessage(
                        "simpleMaterialBalance.simpleMaterialBalanceDetails.window.simpleMaterialBalance.documentsWasGenerated",
                        MessageType.FAILURE);
                return;
            } else if (simpleMaterialBalance.getHasManyField(L_SIMPLE_MATERIAL_BALANCE_ORDERS_COMPONENTS).isEmpty()) {
                state.addMessage(
                        "simpleMaterialBalance.simpleMaterialBalance.window.simpleMaterialBalance.missingAssosiatedOrders",
                        MessageType.FAILURE);
                return;
            } else if (simpleMaterialBalance.getHasManyField(L_SIMPLE_MATERIAL_BALANCE_LOCATIONS_COMPONENTS).isEmpty()) {
                state.addMessage(
                        "simpleMaterialBalance.simpleMaterialBalance.window.simpleMaterialBalance.missingAssosiatedLocations",
                        MessageType.FAILURE);
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                worker.setFieldValue(securityService.getCurrentUserName());
                generated.setFieldValue("1");
                date.setFieldValue(new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, LocaleContextHolder.getLocale())
                        .format(new Date()));
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
        reportService.printGeneratedReport(viewDefinitionState, state, new String[] { args[0],
                SimpleMaterialBalanceConstants.PLUGIN_IDENTIFIER, SimpleMaterialBalanceConstants.MODEL_SIMPLE_MATERIAL_BALANCE });
    }

    private void generateSimpleMaterialBalanceDocuments(final ComponentState state, final Entity simpleMaterialBalance)
            throws IOException, DocumentException {
        Entity simpleMaterialBalanceWithFileName = fileService.updateReportFileName(simpleMaterialBalance, "date",
                "simpleMaterialBalance.simpleMaterialBalance.report.fileName");
        simpleMaterialBalancePdfService.generateDocument(simpleMaterialBalanceWithFileName, state.getLocale());
        simpleMaterialBalanceXlsService.generateDocument(simpleMaterialBalanceWithFileName, state.getLocale());
    }
}
