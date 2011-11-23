/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.materialFlow;

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_COMPANY;
import static com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants.MODEL_MATERIALS_IN_STOCK_AREAS;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlow.print.pdf.MaterialFlowPdfService;
import com.qcadoo.mes.materialFlow.print.xls.MaterialFlowXlsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class MaterialsInStockAreasService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MaterialFlowPdfService materialFlowPdfService;

    @Autowired
    private MaterialFlowXlsService materialFlowXlsService;

    @Value("${reportPath}")
    private String path;

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("fileName", null);
        entity.setField("generated", false);
        entity.setField("time", null);
        entity.setField("worker", null);
        return true;
    }

    public void setGenerateButtonState(final ViewDefinitionState state) {
        setGenerateButtonState(state, state.getLocale(), MaterialFlowConstants.PLUGIN_IDENTIFIER, MODEL_MATERIALS_IN_STOCK_AREAS);
    }

    @SuppressWarnings("unchecked")
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

            Entity materialsInStockAreasEntity = dataDefinitionService.get(plugin, entityName).get(form.getEntityId());
            List<Entity> stockAreaComponents = (List<Entity>) materialsInStockAreasEntity.getField("stockAreas");

            if (materialsInStockAreasEntity.getField("generated") == null) {
                materialsInStockAreasEntity.setField("generated", "0");
            }

            if (stockAreaComponents.size() == 0) {
                generateButton.setMessage("materialFlow.ribbon.message.noStockAreas");
                generateButton.setEnabled(false);
                deleteButton.setMessage(null);
                deleteButton.setEnabled(true);
            } else {
                if ((Boolean) materialsInStockAreasEntity.getField("generated")) {
                    generateButton.setMessage("materialFlow.ribbon.message.recordAlreadyGenerated");
                    generateButton.setEnabled(false);
                    deleteButton.setMessage("materialFlow.ribbon.message.recordAlreadyGenerated");
                    deleteButton.setEnabled(false);
                } else {
                    generateButton.setMessage(null);
                    generateButton.setEnabled(true);
                    deleteButton.setMessage(null);
                    deleteButton.setEnabled(true);
                }
            }
        }
        generateButton.requestUpdate(true);
        deleteButton.requestUpdate(true);
        window.requestRibbonRender();
    }

    public void setGridGenerateButtonState(final ViewDefinitionState state) {
        setGridGenerateButtonState(state, state.getLocale(), MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MODEL_MATERIALS_IN_STOCK_AREAS);
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
                Entity materialsInStockAreasEntity = dataDefinitionService.get(plugin, entityName).get(entityId);

                if ((Boolean) materialsInStockAreasEntity.getField("generated")) {
                    canDelete = false;
                    break;
                }
            }
            if (canDelete) {
                deleteButton.setMessage(null);
                deleteButton.setEnabled(true);
            } else {
                deleteButton.setMessage("materialFlow.ribbon.message.selectedRecordAlreadyGenerated");
                deleteButton.setEnabled(false);
            }
        }

        deleteButton.requestUpdate(true);
        window.requestRibbonRender();
    }

    public void disableFormForExistingMaterialsInStockAreas(final ViewDefinitionState state) {
        ComponentState name = state.getComponentByReference("name");
        ComponentState materialFlowForDate = state.getComponentByReference("materialFlowForDate");
        ComponentState materialsInStockAreasComponents = state.getComponentByReference("materialsInStockAreasComponents");
        FieldComponent generated = (FieldComponent) state.getComponentByReference("generated");

        if ("1".equals(generated.getFieldValue())) {
            name.setEnabled(false);
            materialFlowForDate.setEnabled(false);
            materialsInStockAreasComponents.setEnabled(false);
        } else {
            name.setEnabled(true);
            materialFlowForDate.setEnabled(true);
        }
    }

    public boolean checkMaterialFlowComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        Entity stockAreas = entity.getBelongsToField("stockAreas");
        Entity materialsInStockAreas = entity.getBelongsToField("materialsInStockAreas");

        if (materialsInStockAreas == null || stockAreas == null) {
            return false;
        }

        @SuppressWarnings("deprecation")
        SearchResult searchResult = dataDefinition.find().belongsTo("stockAreas", stockAreas.getId())
                .belongsTo("materialsInStockAreas", materialsInStockAreas.getId()).list();

        if (searchResult.getTotalNumberOfEntities() == 1 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField("stockAreas"),
                    "materialFlow.validate.global.error.mmaterialsInStockAreasDuplicated");
            return false;
        } else {
            return true;
        }
    }

    @Transactional
    public void generateDataForMaterialsInStockAreasReport(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("time");
            ComponentState worker = viewDefinitionState.getComponentByReference("worker");

            Entity materialsInStockAreas = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                    MODEL_MATERIALS_IN_STOCK_AREAS).get((Long) state.getFieldValue());

            if (materialsInStockAreas == null) {
                String message = translationService.translate("qcadooView.message.entityNotFound", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(materialsInStockAreas.getStringField("fileName"))) {
                String message = translationService.translate(
                        "materialFlow.materialsInStockAreasDetails.window.materialRequirement.documentsWasGenerated",
                        state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (materialsInStockAreas.getHasManyField("stockAreas").isEmpty()) {
                String message = translationService.translate(
                        "materialFlow.materialsInStockAreasDetails.window.materialRequirement.missingAssosiatedStockAreas",
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

            materialsInStockAreas = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                    MODEL_MATERIALS_IN_STOCK_AREAS).get((Long) state.getFieldValue());

            try {
                generatePdfAndXlsDocumentsForMaterialsInStockAreas(state, materialsInStockAreas);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private void generatePdfAndXlsDocumentsForMaterialsInStockAreas(final ComponentState state, final Entity materialsInStockAreas)
            throws IOException, DocumentException {
        Entity materialFlowWithFileName = updateFileName(materialsInStockAreas,
                getFullFileName((Date) materialsInStockAreas.getField("time"), materialsInStockAreas.getStringField("name")),
                MaterialFlowConstants.MODEL_MATERIALS_IN_STOCK_AREAS);
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_COMPANY).find().uniqueResult();
        materialFlowPdfService.generateDocument(materialFlowWithFileName, company, state.getLocale());
        materialFlowXlsService.generateDocument(materialFlowWithFileName, company, state.getLocale());
    }

    private String getFullFileName(final Date date, final String fileName) {
        return path + fileName + "_" + new SimpleDateFormat(DateUtils.REPORT_DATE_TIME_FORMAT).format(date);
    }

    private Entity updateFileName(final Entity entity, final String fileName, final String entityName) {
        entity.setField("fileName", fileName);
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, entityName).save(entity);
    }

    public void printMaterialsInStockAreasDocuments(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state.getFieldValue() instanceof Long) {
            Entity materialsInStockAreas = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                    MODEL_MATERIALS_IN_STOCK_AREAS).get((Long) state.getFieldValue());
            if (materialsInStockAreas == null) {
                state.addMessage(translationService.translate("qcadooView.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (!StringUtils.hasText(materialsInStockAreas.getStringField("fileName"))) {
                state.addMessage(translationService.translate(
                        "materialFlow.materialsInStockAreasDetails.window.materialRequirement.documentsWasNotGenerated",
                        state.getLocale()), MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo("/materialFlow/materialsInStockAreas." + args[0] + "?id=" + state.getFieldValue(),
                        true, false);
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

}
