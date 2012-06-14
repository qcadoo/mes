/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

import static com.qcadoo.mes.materialFlow.constants.MaterialsInStockAreasComponentFields.MATERIALS_IN_STOCK_AREAS;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInStockAreasFields.FILE_NAME;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInStockAreasFields.GENERATED;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInStockAreasFields.MATERIAL_FLOW_FOR_DATE;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInStockAreasFields.NAME;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInStockAreasFields.STOCK_AREAS;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInStockAreasFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInStockAreasFields.WORKER;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlow.print.pdf.MaterialFlowPdfService;
import com.qcadoo.mes.materialFlow.print.xls.MaterialFlowXlsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.report.api.ReportService;
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

    private static final String L_WINDOW = "window";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MaterialFlowPdfService materialFlowPdfService;

    @Autowired
    private MaterialFlowXlsService materialFlowXlsService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(FILE_NAME, null);
        entity.setField(GENERATED, false);
        entity.setField(TIME, null);
        entity.setField(WORKER, null);
        return true;
    }

    public void setGenerateButtonState(final ViewDefinitionState state) {
        setGenerateButtonState(state, state.getLocale(), MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_MATERIALS_IN_STOCK_AREAS);
    }

    @SuppressWarnings("unchecked")
    public void setGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String plugin,
            final String entityName) {
        WindowComponent window = (WindowComponent) state.getComponentByReference(L_WINDOW);
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
            List<Entity> stockAreaComponents = (List<Entity>) materialsInStockAreasEntity.getField(STOCK_AREAS);

            if (materialsInStockAreasEntity.getField(GENERATED) == null) {
                materialsInStockAreasEntity.setField(GENERATED, "0");
            }

            if (stockAreaComponents.isEmpty()) {
                generateButton.setMessage("materialFlow.ribbon.message.noStockAreas");
                generateButton.setEnabled(false);
                deleteButton.setMessage(null);
                deleteButton.setEnabled(true);
            } else {
                if ((Boolean) materialsInStockAreasEntity.getField(GENERATED)) {
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
                MaterialFlowConstants.MODEL_MATERIALS_IN_STOCK_AREAS);
    }

    public void setGridGenerateButtonState(final ViewDefinitionState state, final Locale locale, final String plugin,
            final String entityName) {
        WindowComponent window = (WindowComponent) state.getComponentByReference(L_WINDOW);
        GridComponent grid = (GridComponent) state.getComponentByReference("grid");
        RibbonActionItem deleteButton = window.getRibbon().getGroupByName("actions").getItemByName("delete");

        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().size() == 0) {
            deleteButton.setMessage(null);
            deleteButton.setEnabled(false);
        } else {
            boolean canDelete = true;
            for (Long entityId : grid.getSelectedEntitiesIds()) {
                Entity materialsInStockAreasEntity = dataDefinitionService.get(plugin, entityName).get(entityId);

                if ((Boolean) materialsInStockAreasEntity.getField(GENERATED)) {
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
        ComponentState name = state.getComponentByReference(NAME);
        ComponentState materialFlowForDate = state.getComponentByReference(MATERIAL_FLOW_FOR_DATE);
        ComponentState materialsInStockAreasComponents = state.getComponentByReference("materialsInStockAreasComponents");
        FieldComponent generated = (FieldComponent) state.getComponentByReference(GENERATED);

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
        Entity stockAreas = entity.getBelongsToField(STOCK_AREAS);
        Entity materialsInStockAreas = entity.getBelongsToField(MATERIALS_IN_STOCK_AREAS);

        if (materialsInStockAreas == null || stockAreas == null) {
            return false;
        }

        SearchResult searchResult = dataDefinition.find().add(SearchRestrictions.belongsTo(STOCK_AREAS, stockAreas))
                .add(SearchRestrictions.belongsTo(MATERIALS_IN_STOCK_AREAS, materialsInStockAreas)).list();

        if (searchResult.getTotalNumberOfEntities() == 1 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField(STOCK_AREAS),
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
            ComponentState generated = viewDefinitionState.getComponentByReference(GENERATED);
            ComponentState date = viewDefinitionState.getComponentByReference(TIME);
            ComponentState worker = viewDefinitionState.getComponentByReference(WORKER);

            Entity materialsInStockAreas = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowConstants.MODEL_MATERIALS_IN_STOCK_AREAS).get((Long) state.getFieldValue());

            if (materialsInStockAreas == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(materialsInStockAreas.getStringField(FILE_NAME))) {
                state.addMessage("materialFlow.materialsInStockAreasDetails.window.materialRequirement.documentsWasGenerated",
                        MessageType.FAILURE);
                return;
            } else if (materialsInStockAreas.getHasManyField(STOCK_AREAS).isEmpty()) {
                state.addMessage(
                        "materialFlow.materialsInStockAreasDetails.window.materialRequirement.missingAssosiatedStockAreas",
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

            materialsInStockAreas = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowConstants.MODEL_MATERIALS_IN_STOCK_AREAS).get((Long) state.getFieldValue());

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
        Entity materialFlowWithFileName = fileService.updateReportFileName(materialsInStockAreas, TIME,
                "materialFlow.materialsInStockAreas.report.fileName");
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
        materialFlowPdfService.generateDocument(materialFlowWithFileName, company, state.getLocale());
        materialFlowXlsService.generateDocument(materialFlowWithFileName, company, state.getLocale());
    }

    public void printMaterialsInStockAreasDocuments(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        reportService.printGeneratedReport(viewDefinitionState, state, new String[] { args[0],
                MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_MATERIALS_IN_STOCK_AREAS });
    }

}
