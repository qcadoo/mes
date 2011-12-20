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
package com.qcadoo.mes.qualityControls.print;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.qualityControls.constants.QualityControlsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class QualityControlsReportService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void printQualityControlReport(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference("dateFrom");
            FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference("dateTo");

            if (dateFrom == null || dateTo == null || dateFrom.getFieldValue() == null || dateTo.getFieldValue() == null) {
                state.addMessage(translationService.translate("qualityControl.report.invalidDates", state.getLocale()),
                        MessageType.FAILURE);
            } else {
                if (dateFrom.getFieldValue().toString().compareTo(dateTo.getFieldValue().toString()) > 0) {
                    state.addMessage(translationService.translate("qualityControl.report.invalidDates.fromBiggerThanTo",
                            state.getLocale()), MessageType.FAILURE);
                } else {
                    viewDefinitionState.redirectTo("/qualityControl/qualityControlByDates." + args[0] + "?type=" + args[1]
                            + "&dateFrom=" + dateFrom.getFieldValue() + "&dateTo=" + dateTo.getFieldValue(), true, false);
                }
            }
        } else {
            state.addMessage(translationService.translate("qualityControl.report.invalidDates", state.getLocale()),
                    MessageType.FAILURE);
        }
    }

    public final void printQualityControlReportForOrder(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
        if (!(state instanceof GridComponent)) {
            throw new IllegalStateException("method only for grid");
        }
        GridComponent gridState = (GridComponent) state;
        if (gridState.getSelectedEntitiesIds().size() == 0) {
            state.addMessage(translationService.translate("qcadooView.grid.noRowSelectedError", state.getLocale()),
                    MessageType.FAILURE);
            return;
        }
        StringBuilder redirectUrl = new StringBuilder();
        redirectUrl.append("/qualityControl/qualityControlReport.");
        redirectUrl.append(args[0]);
        redirectUrl.append("?type=");
        redirectUrl.append(args[1]);
        for (Long entityId : gridState.getSelectedEntitiesIds()) {
            redirectUrl.append("&id=");
            redirectUrl.append(entityId);
        }
        viewDefinitionState.redirectTo(redirectUrl.toString(), true, false);
    }

    public final void addQualityControlReportHeader(final Document document, final Map<String, Object> model, final Locale locale)
            throws DocumentException {
        if (!model.containsKey("entities")) {
            Paragraph firstParagraphTitle = new Paragraph(new Phrase(translationService.translate(
                    "qualityControls.qualityControl.report.paragrah", locale), PdfUtil.getArialBold11Light()));
            firstParagraphTitle.add(new Phrase(" " + model.get("dateFrom") + " - " + model.get("dateTo"), PdfUtil
                    .getArialBold11Light()));
            firstParagraphTitle.setSpacingBefore(20);
            document.add(firstParagraphTitle);

        }
        Paragraph secondParagraphTitle = new Paragraph(new Phrase(translationService.translate(
                "qualityControls.qualityControl.report.paragrah2", locale), PdfUtil.getArialBold11Light()));
        secondParagraphTitle.setSpacingBefore(20);
        document.add(secondParagraphTitle);
    }

    public final Map<Entity, List<Entity>> getQualityOrdersForProduct(final List<Entity> orders) {
        Map<Entity, List<Entity>> productOrders = new HashMap<Entity, List<Entity>>();
        for (Entity entity : orders) {
            Entity product = entity.getBelongsToField("order").getBelongsToField("product");
            List<Entity> ordersList = new ArrayList<Entity>();
            if (productOrders.containsKey(product)) {
                ordersList = productOrders.get(product);
            }
            ordersList.add(entity);
            productOrders.put(product, ordersList);
        }
        return productOrders;
    }

    public final Map<Entity, List<BigDecimal>> getQualityOrdersQuantitiesForProduct(final List<Entity> orders) {
        Map<Entity, List<BigDecimal>> quantities = new HashMap<Entity, List<BigDecimal>>();
        for (Entity entity : orders) {
            Entity product = entity.getBelongsToField("order").getBelongsToField("product");
            List<BigDecimal> quantitiesList = new ArrayList<BigDecimal>();
            if (quantities.containsKey(product)) {
                quantitiesList = quantities.get(product);
                quantitiesList.set(0, quantitiesList.get(0).add((BigDecimal) entity.getField("controlledQuantity")));
                quantitiesList.set(1, quantitiesList.get(1).add((BigDecimal) entity.getField("rejectedQuantity")));
                quantitiesList.set(2, quantitiesList.get(2).add((BigDecimal) entity.getField("acceptedDefectsQuantity")));
            } else {
                quantitiesList.add(0, (BigDecimal) entity.getField("controlledQuantity"));
                quantitiesList.add(1, (BigDecimal) entity.getField("rejectedQuantity"));
                quantitiesList.add(2, (BigDecimal) entity.getField("acceptedDefectsQuantity"));
            }
            quantities.put(product, quantitiesList);
        }
        return quantities;
    }

    public final Map<Entity, List<BigDecimal>> getQualityOrdersResultsQuantitiesForProduct(final List<Entity> orders) {
        Map<Entity, List<BigDecimal>> quantities = new HashMap<Entity, List<BigDecimal>>();
        for (Entity entity : orders) {
            Entity product = entity.getBelongsToField("order").getBelongsToField("product");
            List<BigDecimal> quantitiesList = new ArrayList<BigDecimal>();
            if (quantities.containsKey(product)) {
                quantitiesList = quantities.get(product);
                quantitiesList.set(0, quantitiesList.get(0).add(BigDecimal.ONE));
                if ("01correct".equals(entity.getField("controlResult"))) {
                    quantitiesList.set(1, quantitiesList.get(1).add(BigDecimal.ONE));
                } else if ("02incorrect".equals(entity.getField("controlResult"))) {
                    quantitiesList.set(2, quantitiesList.get(2).add(BigDecimal.ONE));
                } else if ("03objection".equals(entity.getField("controlResult"))) {
                    quantitiesList.set(3, quantitiesList.get(3).add(BigDecimal.ONE));
                }
                if (entity.getBelongsToField("order").getField("doneQuantity") != null) {
                    quantitiesList.set(4,
                            quantitiesList.get(4).add((BigDecimal) entity.getBelongsToField("order").getField("doneQuantity")));
                } else {
                    quantitiesList
                            .set(4,
                                    quantitiesList.get(4).add(
                                            (BigDecimal) entity.getBelongsToField("order").getField("plannedQuantity")));
                }
            } else {
                quantitiesList.add(0, BigDecimal.ONE);
                if ("01correct".equals(entity.getField("controlResult"))) {
                    quantitiesList.add(1, BigDecimal.ONE);
                    quantitiesList.add(2, BigDecimal.ZERO);
                    quantitiesList.add(3, BigDecimal.ZERO);
                } else if ("02incorrect".equals(entity.getField("controlResult"))) {
                    quantitiesList.add(1, BigDecimal.ZERO);
                    quantitiesList.add(2, BigDecimal.ONE);
                    quantitiesList.add(3, BigDecimal.ZERO);
                } else if ("03objection".equals(entity.getField("controlResult"))) {
                    quantitiesList.add(1, BigDecimal.ZERO);
                    quantitiesList.add(2, BigDecimal.ZERO);
                    quantitiesList.add(3, BigDecimal.ONE);
                }
                if (entity.getBelongsToField("order").getField("doneQuantity") != null) {
                    quantitiesList.add(4, (BigDecimal) entity.getBelongsToField("order").getField("doneQuantity"));
                } else if (entity.getBelongsToField("order").getField("plannedQuantity") != null) {
                    quantitiesList.add(4, (BigDecimal) entity.getBelongsToField("order").getField("plannedQuantity"));
                } else {
                    quantitiesList.add(4, BigDecimal.ZERO);
                }
            }
            quantities.put(product, quantitiesList);
        }
        return quantities;
    }

    public final Map<Entity, List<Entity>> getQualityOrdersForOperation(final List<Entity> orders) {
        Map<Entity, List<Entity>> operationOrders = new HashMap<Entity, List<Entity>>();
        for (Entity entity : orders) {
            Entity operation = entity.getBelongsToField("operation");
            List<Entity> ordersList = new ArrayList<Entity>();
            if (operationOrders.containsKey(operation)) {
                ordersList = operationOrders.get(operation);
            }
            ordersList.add(entity);
            operationOrders.put(operation, ordersList);
        }
        return operationOrders;
    }

    public final Map<Entity, List<BigDecimal>> getQualityOrdersResultsQuantitiesForOperation(final List<Entity> orders) {
        Map<Entity, List<BigDecimal>> quantities = new HashMap<Entity, List<BigDecimal>>();
        for (Entity entity : orders) {
            Entity operation = entity.getBelongsToField("operation");
            List<BigDecimal> quantitiesList = new ArrayList<BigDecimal>();
            if (quantities.containsKey(operation)) {
                quantitiesList = quantities.get(operation);
                quantitiesList.set(0, quantitiesList.get(0).add(BigDecimal.ONE));
                if ("01correct".equals(entity.getField("controlResult"))) {
                    quantitiesList.set(1, quantitiesList.get(1).add(BigDecimal.ONE));
                } else if ("02incorrect".equals(entity.getField("controlResult"))) {
                    quantitiesList.set(2, quantitiesList.get(2).add(BigDecimal.ONE));
                } else if ("03objection".equals(entity.getField("controlResult"))) {
                    quantitiesList.set(3, quantitiesList.get(3).add(BigDecimal.ONE));
                }
            } else {
                quantitiesList.add(0, BigDecimal.ONE);
                if ("01correct".equals(entity.getField("controlResult"))) {
                    quantitiesList.add(1, BigDecimal.ONE);
                    quantitiesList.add(2, BigDecimal.ZERO);
                    quantitiesList.add(3, BigDecimal.ZERO);
                } else if ("02incorrect".equals(entity.getField("controlResult"))) {
                    quantitiesList.add(1, BigDecimal.ZERO);
                    quantitiesList.add(2, BigDecimal.ONE);
                    quantitiesList.add(3, BigDecimal.ZERO);
                } else if ("03objection".equals(entity.getField("controlResult"))) {
                    quantitiesList.add(1, BigDecimal.ZERO);
                    quantitiesList.add(2, BigDecimal.ZERO);
                    quantitiesList.add(3, BigDecimal.ONE);
                }
            }
            quantities.put(operation, quantitiesList);
        }
        return quantities;
    }

    @SuppressWarnings("unchecked")
    public final List<Entity> getOrderSeries(final Map<String, Object> model, final String type) {
        DataDefinition dataDef = dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL);
        if (model.containsKey("entities")) {
            if (!(model.get("entities") instanceof List<?>)) {
                throw new IllegalStateException("entities are not list");
            }
            List<Entity> entities = (List<Entity>) model.get("entities");
            for (Entity entity : entities) {
                if (!(Boolean) entity.getField("closed")) {
                    throw new IllegalStateException("quality controll is not closed");
                }
            }
            return entities;
        } else {
            try {
                SearchResult result = dataDef.find()
                        .isGe("date", DateUtils.parseAndComplete(model.get("dateFrom").toString(), false))
                        .isLe("date", DateUtils.parseAndComplete(model.get("dateTo").toString(), true))
                        .isEq("qualityControlType", type).isEq("closed", true).list();
                return result.getEntities();
            } catch (ParseException e) {
                return Collections.emptyList();
            }
        }
    }

    public final Element prepareTitle(final Entity product, final Locale locale, final String type) {

        Paragraph title = new Paragraph();

        if ("batch".equals(type)) {
            title.add(new Phrase(translationService.translate("qualityControls.qualityControl.report.paragrah3", locale), PdfUtil
                    .getArialBold11Light()));
        } else if ("order".equals(type)) {
            title.add(new Phrase(translationService.translate("qualityControls.qualityControl.report.paragrah4", locale), PdfUtil
                    .getArialBold11Light()));
        } else if ("unit".equals(type)) {
            title.add(new Phrase(translationService.translate("qualityControls.qualityControl.report.paragrah5", locale), PdfUtil
                    .getArialBold11Light()));
        } else if ("operation".equals(type)) {
            title.add(new Phrase(translationService.translate("qualityControls.qualityControl.report.paragrah6", locale), PdfUtil
                    .getArialBold11Light()));
        }

        String name = "";

        if (product != null) {
            if ("operation".equals(type)) {
                name = product.getBelongsToField("operation").getStringField("name");
            } else {
                name = product.getField("name").toString();
            }
        }
        title.add(new Phrase(" " + name, PdfUtil.getArialBold11Dark()));

        return title;
    }
}
