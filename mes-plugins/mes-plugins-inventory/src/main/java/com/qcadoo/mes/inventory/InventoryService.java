/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.6
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
package com.qcadoo.mes.inventory;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.inventory.constants.InventoryConstants;
import com.qcadoo.mes.inventory.print.pdf.InventoryPdfService;
import com.qcadoo.mes.inventory.print.xls.InventoryXlsService;
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
public class InventoryService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private InventoryPdfService inventoryPdfService;

    @Autowired
    private InventoryXlsService inventoryXlsService;

    @Value("${reportPath}")
    private String path;

    BigDecimal calculateShouldBe(String warehouse, String product, String forDate) {

        BigDecimal countProductIn = BigDecimal.ZERO;
        BigDecimal countProductOut = BigDecimal.ZERO;
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal countProduct = BigDecimal.ZERO;
        Date lastCorrectionDate = null;

        DataDefinition transferDataCorrection = dataDefinitionService.get("inventory", "correction");
        DataDefinition transferTo = dataDefinitionService.get("inventory", "transfer");
        DataDefinition transferFrom = dataDefinitionService.get("inventory", "transfer");

        SearchResult resultDataCorrection = transferDataCorrection.find(
                "where warehouse = " + warehouse + " order by correctionDate asc").list();

        for (Entity e : resultDataCorrection.getEntities()) {
            lastCorrectionDate = (Date) e.getField("correctionDate");
            quantity = (BigDecimal) e.getField("found");
            countProduct = quantity;
        }

        SearchResult resultTo = null;
        SearchResult resultFrom = null;

        if (lastCorrectionDate == null) {
            resultTo = transferTo.find(
                    "where warehouseTo = " + warehouse + " and product = " + product + " and date <= '" + forDate + "'").list();

            resultFrom = transferFrom.find(
                    "where warehouseFrom = " + warehouse + " and product = " + product + " and date <= '" + forDate + "'").list();

        } else {
            resultTo = transferTo.find(
                    "where warehouseTo = " + warehouse + " and product = " + product + " and date <= '" + forDate
                            + "' and date > '" + lastCorrectionDate + "'").list();

            resultFrom = transferFrom.find(
                    "where warehouseFrom = " + warehouse + " and product = " + product + " and date <= '" + forDate
                            + "' and date > '" + lastCorrectionDate + "'").list();
        }

        for (Entity e : resultTo.getEntities()) {
            quantity = (BigDecimal) e.getField("quantity");
            countProductIn = countProductIn.add(quantity);
        }

        for (Entity e : resultFrom.getEntities()) {
            quantity = (BigDecimal) e.getField("quantity");
            countProductOut = countProductOut.add(quantity);
        }

        if (lastCorrectionDate == null) {

            countProductIn = countProductIn.subtract(countProductOut);
        } else {
            countProductIn = countProductIn.add(countProduct);
            countProductIn = countProductIn.subtract(countProductOut);
        }

        if (countProductIn.compareTo(BigDecimal.ZERO) == -1)
            countProductIn = BigDecimal.ZERO;
        return countProductIn;
    }

    public void refreshShouldBe(final ViewDefinitionState state, final ComponentState componentState, final String[] args) {

        ComponentState warehouse = (ComponentState) state.getComponentByReference("warehouse");
        ComponentState product = (ComponentState) state.getComponentByReference("product");
        ComponentState date = (ComponentState) state.getComponentByReference("correctionDate");
        FieldComponent should = (FieldComponent) state.getComponentByReference("shouldBe");

        if (warehouse.getFieldValue() != null && warehouse != null && product.getFieldValue() != null && product != null
                && date.getFieldValue() != null && date != null && !date.getFieldValue().toString().equals("")) {

            String warehouseNumber = warehouse.getFieldValue().toString();
            String productNumber = product.getFieldValue().toString();
            String forDate = date.getFieldValue().toString();

            BigDecimal shouldBe = calculateShouldBe(warehouseNumber, productNumber, forDate);

            if (shouldBe != null && shouldBe != BigDecimal.ZERO) {
                should.setFieldValue(shouldBe);
            } else {
                should.setFieldValue(BigDecimal.ZERO);
            }
        }

    }

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("fileName", null);
        entity.setField("generated", "0");
        entity.setField("date", null);
        return true;
    }

    public void setGridGenerateButtonState(final ViewDefinitionState state) {
        setGridGenerateButtonState(state, state.getLocale(), InventoryConstants.PLUGIN_IDENTIFIER,
                InventoryConstants.MODEL_INVENTORY_REPORT);
    }

    public boolean validateTransfer(final DataDefinition dataDefinition, final Entity entity) {

        Entity warehouseFrom = (Entity) (entity.getField("warehouseFrom") != null ? entity.getField("warehouseFrom") : null);
        Entity warehouseTo = (Entity) (entity.getField("warehouseTo") != null ? entity.getField("warehouseTo") : null);

        if (warehouseFrom == null && warehouseTo == null) {
            entity.addError(dataDefinition.getField("warehouseFrom"), "inventory.validate.global.error.fillAtLeastOneWarehouse");
            entity.addError(dataDefinition.getField("warehouseTo"), "inventory.validate.global.error.fillAtLeastOneWarehouse");
            return false;
        }
        return true;

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

            Entity inventoryReportEntity = dataDefinitionService.get(plugin, entityName).get(form.getEntityId());

            if (inventoryReportEntity.getField("generated") == null)
                inventoryReportEntity.setField("generated", "0");

            if ("1".equals(inventoryReportEntity.getField("generated"))) {
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

    @SuppressWarnings("unchecked")
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
                Entity inventoryReportEntity = dataDefinitionService.get(plugin, entityName).get(entityId);

                if ((Boolean) inventoryReportEntity.getField("generated")) {
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

    public void printInventory(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {

        if (state.getFieldValue() instanceof Long) {
            Entity inventoryReport = dataDefinitionService.get(InventoryConstants.PLUGIN_IDENTIFIER,
                    InventoryConstants.MODEL_INVENTORY_REPORT).get((Long) state.getFieldValue());
            if (inventoryReport == null) {
                state.addMessage(translationService.translate("qcadooView.message.entityNotFound", state.getLocale()),
                        MessageType.FAILURE);
            } else if (!StringUtils.hasText(inventoryReport.getStringField("fileName"))) {
                state.addMessage(
                        translationService.translate(
                                "inventory.inventoryReportDetails.window.materialRequirement.documentsWasNotGenerated",
                                state.getLocale()), MessageType.FAILURE);
            } else {
                viewDefinitionState.redirectTo("/inventory/inventoryReport." + args[0] + "?id=" + state.getFieldValue(), false,
                        false);
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

    public void generateInventory(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (state instanceof FormComponent) {
            ComponentState generated = viewDefinitionState.getComponentByReference("generated");
            ComponentState date = viewDefinitionState.getComponentByReference("date");

            Entity inventoryReport = dataDefinitionService.get(InventoryConstants.PLUGIN_IDENTIFIER,
                    InventoryConstants.MODEL_INVENTORY_REPORT).get((Long) state.getFieldValue());

            if (inventoryReport == null) {
                String message = translationService.translate("qcadooView.message.entityNotFound", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(inventoryReport.getStringField("fileName"))) {
                String message = translationService.translate(
                        "inventory.inventoryReportDetails.window.materialRequirement.documentsWasGenerated", state.getLocale());
                state.addMessage(message, MessageType.FAILURE);
                return;
            }

            if ("0".equals(generated.getFieldValue())) {
                generated.setFieldValue("1");
                date.setFieldValue(new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).format(new Date()));
            }

            state.performEvent(viewDefinitionState, "save", new String[0]);

            if (state.getFieldValue() == null || !((FormComponent) state).isValid()) {
                generated.setFieldValue("0");
                date.setFieldValue(null);
                return;
            }

            inventoryReport = dataDefinitionService.get(InventoryConstants.PLUGIN_IDENTIFIER,
                    InventoryConstants.MODEL_INVENTORY_REPORT).get((Long) state.getFieldValue());

            try {
                generateMaterialReqDocuments(state, inventoryReport);
                state.performEvent(viewDefinitionState, "reset", new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private String getFullFileName(final Date date, final String fileName) {

        return path + fileName + "_" + new SimpleDateFormat(DateUtils.REPORT_DATE_TIME_FORMAT).format(date);

    }

    private Entity updateFileName(final Entity entity, final String fileName, final String entityName) {
        entity.setField("fileName", fileName);
        return dataDefinitionService.get(InventoryConstants.PLUGIN_IDENTIFIER, entityName).save(entity);
    }

    private void generateMaterialReqDocuments(final ComponentState state, final Entity inventory) throws IOException,
            DocumentException {

        Entity inventoryWithFileName = updateFileName(inventory,
                getFullFileName((Date) inventory.getField("date"), inventory.getStringField("name")),
                InventoryConstants.MODEL_INVENTORY_REPORT);
        Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .uniqueResult();
        inventoryPdfService.generateDocument(inventoryWithFileName, company, state.getLocale());
        inventoryXlsService.generateDocument(inventoryWithFileName, company, state.getLocale());
    }

}