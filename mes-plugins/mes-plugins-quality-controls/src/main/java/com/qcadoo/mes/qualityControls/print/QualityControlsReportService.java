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
import com.lowagie.text.Element;
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
                if ("forOrder".equals(args[1])) {
                    viewDefinitionState.redirectTo(
                            "/qualityControl/qualityControlForOrder." + args[0] + "?dateFrom=" + dateFrom.getFieldValue()
                                    + "&dateTo=" + dateTo.getFieldValue(), true, false);
                } else if ("forUnit".equals(args[1])) {
                    viewDefinitionState.redirectTo(
                            "/qualityControl/qualityControlForUnit." + args[0] + "?dateFrom=" + dateFrom.getFieldValue()
                                    + "&dateTo=" + dateTo.getFieldValue(), true, false);
                } else if ("forBatch".equals(args[1])) {
                    viewDefinitionState.redirectTo(
                            "/qualityControl/qualityControlForBatch." + args[0] + "?dateFrom=" + dateFrom.getFieldValue()
                                    + "&dateTo=" + dateTo.getFieldValue(), true, false);
                } else if ("forOperation".equals(args[1])) {
                    viewDefinitionState.redirectTo("/qualityControl/qualityControlForOperation." + args[0] + "?dateFrom="
                            + dateFrom.getFieldValue() + "&dateTo=" + dateTo.getFieldValue(), true, false);
                }
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

    public Element prepareTitle(Entity product, Locale locale, String type) {

        Paragraph title = new Paragraph();

        if (type.equals("product")) {
            title.add(new Phrase(translationService.translate("qualityControls.qualityControl.report.paragrah1", locale), PdfUtil
                    .getArialBold11Light()));
            String name = "";
            if (product != null) {
                name = product.getField("name").toString();
            }
            title.add(new Phrase(" " + name, PdfUtil.getArialBold19Dark()));
        }

        return title;
    }
}
