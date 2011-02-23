package com.qcadoo.mes.qualityControls.print;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.utils.pdf.PdfUtil;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.grid.GridComponentState;

@Service
public class QualityControlsReportService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void printQualityControlReport(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponentState) {
            FieldComponentState dateFrom = (FieldComponentState) viewDefinitionState.getComponentByReference("dateFrom");
            FieldComponentState dateTo = (FieldComponentState) viewDefinitionState.getComponentByReference("dateTo");
            if (dateFrom != null && dateTo != null && dateFrom.getFieldValue() != null && dateTo.getFieldValue() != null) {
                viewDefinitionState.redirectTo("/qualityControl/qualityControlByDates." + args[0] + "?type=" + args[1]
                        + "&dateFrom=" + dateFrom.getFieldValue() + "&dateTo=" + dateTo.getFieldValue(), true, false);
            } else {
                state.addMessage(translationService.translate("qualityControl.report.invalidDates", state.getLocale()),
                        MessageType.FAILURE);
            }
        } else {
            state.addMessage(translationService.translate("qualityControl.report.invalidDates", state.getLocale()),
                    MessageType.FAILURE);
        }
    }

    public void addQualityControlReportHeader(final Document document, final String dateFrom, final String dateTo,
            final Locale locale) throws DocumentException {
        Paragraph firstParagraphTitle = new Paragraph(new Phrase(translationService.translate(
                "qualityControls.qualityControl.report.paragrah", locale), PdfUtil.getArialBold11Light()));
        firstParagraphTitle.add(new Phrase(" " + dateFrom + " - " + dateTo, PdfUtil.getArialBold11Light()));
        firstParagraphTitle.setSpacingBefore(20);
        document.add(firstParagraphTitle);

        Paragraph secondParagraphTitle = new Paragraph(new Phrase(translationService.translate(
                "qualityControls.qualityControl.report.paragrah2", locale), PdfUtil.getArialBold11Light()));
        secondParagraphTitle.setSpacingBefore(20);
        document.add(secondParagraphTitle);
    }

    public void aggregateOrdersData(final Map<Entity, List<Entity>> productOrders,
            final Map<Entity, List<BigDecimal>> quantities, final List<Entity> orders, final boolean countQuantities) {
        for (Entity entity : orders) {
            Entity product = entity.getBelongsToField("order").getBelongsToField("product");
            List<Entity> ordersList = new ArrayList<Entity>();
            if (productOrders.containsKey(product)) {
                ordersList = productOrders.get(product);
            }
            ordersList.add(entity);
            productOrders.put(product, ordersList);
            if (countQuantities) {
                List<BigDecimal> quantitiesList = new ArrayList<BigDecimal>();
                if (quantities.containsKey(product)) {
                    quantitiesList = quantities.get(product);
                    quantitiesList.add(0, quantitiesList.get(0).add((BigDecimal) entity.getField("controlledQuantity")));
                    quantitiesList.add(1, quantitiesList.get(1).add((BigDecimal) entity.getField("rejectedQuantity")));
                    quantitiesList.add(2, quantitiesList.get(2).add((BigDecimal) entity.getField("acceptedDefectsQuantity")));
                } else {
                    quantitiesList.add(0, (BigDecimal) entity.getField("controlledQuantity"));
                    quantitiesList.add(1, (BigDecimal) entity.getField("rejectedQuantity"));
                    quantitiesList.add(2, (BigDecimal) entity.getField("acceptedDefectsQuantity"));
                }
                quantities.put(product, quantitiesList);
            }
        }
    }

    public List<Entity> getOrderSeries(final String dateFrom, final String dateTo, final String type) {
        DataDefinition dataDef = dataDefinitionService.get("qualityControls", "qualityControl");
        try {
            SearchResult result = dataDef.find()
                    .restrictedWith(Restrictions.ge(dataDef.getField("date"), DateType.parseDate(dateFrom, false)))
                    .restrictedWith(Restrictions.le(dataDef.getField("date"), DateType.parseDate(dateTo, true)))
                    .restrictedWith(Restrictions.eq("qualityControlType", type)).restrictedWith(Restrictions.eq("closed", true))
                    .list();
            return result.getEntities();
        } catch (ParseException e) {
            return Collections.emptyList();
        }
    }

    public void printQualityControlReportForOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof GridComponentState)) {
            throw new IllegalStateException("method only for grid");
        }
        GridComponentState gridState = (GridComponentState) state;
        if (gridState.getSelectedEntitiesId().size() == 0) {
            state.addMessage(translationService.translate("core.grid.noRowSelectedError", state.getLocale()), MessageType.FAILURE);
            return;
        }
        StringBuilder redirectUrl = new StringBuilder();
        redirectUrl.append("/qualityControl/qualityControlReport.");
        redirectUrl.append(args[0]);
        redirectUrl.append("?type=");
        redirectUrl.append(args[1]);
        for (Long entityId : gridState.getSelectedEntitiesId()) {
            redirectUrl.append("&id=");
            redirectUrl.append(entityId);
        }
        viewDefinitionState.redirectTo(redirectUrl.toString(), true, false);
    }
}
