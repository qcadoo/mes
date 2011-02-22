package com.qcadoo.mes.genealogies.print;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.genealogies.print.util.EntityOrderNumberComparator;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.utils.pdf.PdfUtil;
import com.qcadoo.mes.utils.pdf.ReportPdfView;

public class GenealogyForComponentView extends ReportPdfView {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    protected String addContent(final Document document, final Map<String, Object> model, final Locale locale,
            final PdfWriter writer) throws DocumentException, IOException {
        Entity entity = dataDefinitionService.get("genealogies", "productInBatch").get(
                Long.valueOf(model.get("value").toString()));
        String documentTitle = getTranslationService().translate("genealogies.genealogyForComponent.report.title", locale);
        String documentAuthor = getTranslationService().translate("genealogies.genealogyForComponent.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, "", documentTitle, documentAuthor, new Date(), user);
        addTables(document, entity, locale);
        String text = getTranslationService().translate("core.report.endOfReport", locale);
        PdfUtil.addEndOfDocument(document, writer, text);
        return getTranslationService().translate("genealogies.genealogyForComponent.report.fileName", locale);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("genealogies.genealogyForComponent.report.title", locale));
    }

    private void addTables(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(getTranslationService().translate("products.order.number.label", locale));
        orderHeader.add(getTranslationService().translate("products.order.name.label", locale));
        orderHeader.add(getTranslationService().translate("products.order.product.label", locale));
        orderHeader.add(getTranslationService().translate("genealogies.genealogyForComponent.report.productBatch", locale));
        Paragraph productTitle = new Paragraph(new Phrase(getTranslationService().translate(
                "genealogies.genealogyForComponent.report.paragrah.product", locale), PdfUtil.getArialBold11Light()));
        productTitle.setSpacingBefore(20);
        document.add(productTitle);
        PdfPTable headerData = PdfUtil.createPanelTable(3);
        headerData.setSpacingBefore(7);
        Entity product = entity.getBelongsToField("productInComponent").getBelongsToField("productInComponent")
                .getBelongsToField("product");
        PdfUtil.addTableCellAsTable(headerData, getTranslationService().translate("products.product.number.label", locale),
                product.getField("number"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(headerData, getTranslationService().translate("products.product.name.label", locale),
                product.getField("name"), "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(headerData,
                getTranslationService().translate("genealogies.productInBatch.batch.label", locale), entity.getField("batch"),
                "", PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        document.add(headerData);
        Paragraph orderTitle = new Paragraph(new Phrase(getTranslationService().translate(
                "genealogies.genealogyForComponent.report.paragrah.order", locale), PdfUtil.getArialBold11Light()));
        orderTitle.setSpacingBefore(20);
        document.add(orderTitle);
        addOrderSeries(document, entity, orderHeader);
    }

    private void addOrderSeries(final Document document, final Entity entity, final List<String> orderHeader)
            throws DocumentException {
        PdfPTable table = PdfUtil.createTableWithHeader(4, orderHeader, false);
        List<Entity> genealogies = getGenealogies(entity);
        Collections.sort(genealogies, new EntityOrderNumberComparator());
        for (Entity genealogy : genealogies) {
            Entity order = (Entity) genealogy.getField("order");
            table.addCell(new Phrase(order.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(order.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            Entity product = (Entity) order.getField("product");
            if (product == null) {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            } else {
                table.addCell(new Phrase(product.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            }
            table.addCell(new Phrase(genealogy.getField("batch").toString(), PdfUtil.getArialRegular9Dark()));
        }
        document.add(table);
    }

    private List<Entity> getGenealogies(final Entity entity) {
        List<Entity> genealogies = new ArrayList<Entity>();
        List<Entity> batchList = dataDefinitionService.get("genealogies", "productInBatch").find()
                .restrictedWith(Restrictions.eq("batch", entity.getField("batch"))).list().getEntities();
        for (Entity batch : batchList) {
            Entity genealogy = (Entity) ((Entity) ((Entity) batch.getField("productInComponent")).getField("genealogy"));
            if (!genealogies.contains(genealogy)) {
                genealogies.add(genealogy);
            }
        }
        return genealogies;
    }
}
