package com.qcadoo.mes.genealogies.print;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.utils.Pair;
import com.qcadoo.mes.utils.pdf.PdfUtil;
import com.qcadoo.mes.utils.pdf.ReportPdfView;

public class GenealogyForProductView extends ReportPdfView {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    protected String addContent(final Document document, final Object value, final Locale locale, final PdfWriter writer)
            throws DocumentException, IOException {
        Entity entity = dataDefinitionService.get("genealogies", "genealogy").get(Long.valueOf(value.toString()));
        String documentTitle = getTranslationService().translate("genealogies.genealogyForProduct.report.title", locale);
        String documentAuthor = getTranslationService().translate("genealogies.genealogyForProduct.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, entity.getField("batch").toString(), documentTitle, documentAuthor, new Date(), user);
        addTables(document, entity, locale);
        String text = getTranslationService().translate("core.report.endOfReport", locale);
        PdfUtil.addEndOfDocument(document, writer, text);
        return getTranslationService().translate("genealogies.genealogyForProduct.report.fileName", locale);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("genealogies.genealogyForProduct.report.title", locale));
    }

    private void addTables(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(getTranslationService().translate("products.order.number.label", locale));
        orderHeader.add(getTranslationService().translate("products.order.name.label", locale));
        orderHeader.add(getTranslationService().translate("products.order.dateFrom.label", locale));
        List<Entity> orders = getOrders(entity);
        addOrderSeries(document, orders, orderHeader);
        addComponentSeries(document, orders, locale);
    }

    private void addComponentSeries(final Document document, final List<Entity> orders, final Locale locale)
            throws DocumentException {
        for (Entity order : orders) {
            document.add(Chunk.NEWLINE);
            Paragraph title = new Paragraph(new Phrase(getTranslationService().translate(
                    "genealogies.genealogyForProduct.report.paragrah", locale), PdfUtil.getArialBold11Light()));
            title.add(new Phrase(" " + order.getField("number").toString(), PdfUtil.getArialBold19Dark()));
            document.add(title);
            List<String> componentHeader = new ArrayList<String>();
            componentHeader.add(getTranslationService().translate("products.product.number.label", locale));
            componentHeader.add(getTranslationService().translate("products.product.name.label", locale));
            componentHeader.add(getTranslationService().translate("genealogies.productInBatch.quantity.label", locale));
            componentHeader.add(getTranslationService().translate("genealogies.productInBatch.batch.label", locale));
            PdfPTable table = PdfUtil.createTableWithHeader(4, componentHeader, false);

            for (Entry<Pair<String, Entity>, BigDecimal> entry : getBatchMap(order).entrySet()) {
                String batch = entry.getKey().getKey();
                Entity product = entry.getKey().getValue();
                BigDecimal quantity = entry.getValue();
                table.addCell(new Phrase(product.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
                table.addCell(new Phrase(product.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(new Phrase(getDecimalFormat().format(quantity), PdfUtil.getArialRegular9Dark()));
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(new Phrase(batch, PdfUtil.getArialRegular9Dark()));
            }

            document.add(table);
        }
    }

    private void addOrderSeries(final Document document, final List<Entity> orders, final List<String> orderHeader)
            throws DocumentException {
        PdfPTable table = PdfUtil.createTableWithHeader(3, orderHeader, false);
        for (Entity order : orders) {
            table.addCell(new Phrase(order.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(order.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(order.getField("dateFrom").toString(), PdfUtil.getArialRegular9Dark()));
        }
        document.add(table);
    }

    private Map<Pair<String, Entity>, BigDecimal> getBatchMap(final Entity entity) {
        Map<Pair<String, Entity>, BigDecimal> batchMap = new HashMap<Pair<String, Entity>, BigDecimal>();
        for (Entity genealogy : entity.getHasManyField("genealogies")) {
            for (Entity productInComponent : genealogy.getHasManyField("productInComponents")) {
                Entity product = (Entity) ((Entity) productInComponent.getField("productInComponent")).getField("product");
                for (Entity batch : productInComponent.getHasManyField("batch")) {
                    BigDecimal quantity = (BigDecimal) batch.getField("quantity");
                    quantity = (quantity == null) ? BigDecimal.ZERO : quantity;
                    Pair<String, Entity> pair = Pair.of(batch.getField("batch").toString(), product);
                    if (batchMap.containsKey(pair)) {
                        BigDecimal oldQuantity = batchMap.get(pair);
                        oldQuantity = oldQuantity.add(quantity);
                        batchMap.put(pair, oldQuantity);
                    } else {
                        batchMap.put(pair, quantity);
                    }
                }
            }
        }
        return batchMap;
    }

    private List<Entity> getOrders(final Entity entity) {
        List<Entity> orders = new ArrayList<Entity>();
        List<Entity> genealogyList = dataDefinitionService.get("genealogies", "genealogy").find()
                .restrictedWith(Restrictions.eq("batch", entity.getField("batch"))).list().getEntities();
        for (Entity genealogy : genealogyList) {
            Entity order = (Entity) genealogy.getField("order");
            if (!orders.contains(order)) {
                orders.add(order);
            }
        }
        return orders;
    }
}
