package com.qcadoo.mes.products.print.pdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.internal.ProxyEntity;
import com.qcadoo.mes.products.print.MaterialRequirementDocumentService;
import com.qcadoo.mes.products.print.pdf.util.PdfPageNumbering;
import com.qcadoo.mes.products.print.pdf.util.PdfUtil;

@Service
public final class MaterialRequirementPdfService extends MaterialRequirementDocumentService {

    @Autowired
    private SecurityService securityService;

    private static final Logger LOG = LoggerFactory.getLogger(MaterialRequirementPdfService.class);

    private DecimalFormat df;

    @Override
    public void generateDocument(final Entity entity, final Locale locale) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        try {
            df = (DecimalFormat) DecimalFormat.getInstance(locale);
            String fileName = getFileName((Date) entity.getField("date")) + PdfUtil.PDF_EXTENSION;
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);
            writer.setPageEvent(new PdfPageNumbering(getTranslationService().translate("products.report.page", locale),
                    getTranslationService().translate("products.report.in", locale)));
            document.setMargins(8, 80, 40, 100);
            buildPdfMetadata(document, locale);
            writer.createXmpMetadata();
            document.open();
            buildPdfContent(document, entity, locale);
            String text = getTranslationService().translate("products.report.endOfReport", locale);
            PdfUtil.addEndOfDocument(document, writer, text);
            document.close();
        } catch (DocumentException e) {
            LOG.error("Problem with generating document - " + e.getMessage());
            document.close();
            throw e;
        }
    }

    private void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documenTitle = getTranslationService().translate("products.materialRequirement.report.title", locale);
        String documentAuthor = getTranslationService().translate("products.materialRequirement.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, entity, documenTitle, documentAuthor, (Date) entity.getField("date"), user);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("products.materialRequirement.report.paragrah", locale),
                PdfUtil.getArialBold11Dark()));
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(getTranslationService().translate("products.order.number.label", locale));
        orderHeader.add(getTranslationService().translate("products.order.name.label", locale));
        orderHeader.add(getTranslationService().translate("products.order.product.label", locale));
        orderHeader.add(getTranslationService().translate("products.product.unit.label", locale));
        orderHeader.add(getTranslationService().translate("products.order.plannedQuantity.label", locale));
        addOrderSeries(document, entity, orderHeader);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(getTranslationService().translate("products.materialRequirement.report.paragrah2", locale),
                PdfUtil.getArialBold11Dark()));
        List<String> productHeader = new ArrayList<String>();
        productHeader.add(getTranslationService().translate("products.product.number.label", locale));
        productHeader.add(getTranslationService().translate("products.product.name.label", locale));
        productHeader.add(getTranslationService().translate("products.product.unit.label", locale));
        productHeader.add(getTranslationService().translate("products.instructionBomComponent.quantity.label", locale));
        addBomSeries(document, (DefaultEntity) entity, productHeader);
    }

    private void buildPdfMetadata(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("products.materialRequirement.report.title", locale));
        PdfUtil.addMetaData(document);
    }

    private void addOrderSeries(final Document document, final Entity entity, final List<String> orderHeader)
            throws DocumentException {
        List<Entity> orders = entity.getHasManyField("orders");
        PdfPTable table = PdfUtil.createTableWithHeader(5, orderHeader);
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            table.addCell(new Phrase(order.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(order.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            Entity product = (Entity) order.getField("product");
            if (product != null) {
                table.addCell(new Phrase(product.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            } else {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            }
            if (product != null) {
                Object unit = product.getField("unit");
                if (unit != null) {
                    table.addCell(new Phrase(unit.toString(), PdfUtil.getArialRegular9Dark()));
                } else {
                    table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
                }
            } else {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            }
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(df.format(((BigDecimal) order.getField("plannedQuantity")).stripTrailingZeros()), PdfUtil
                    .getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        document.add(table);

    }

    private void addBomSeries(final Document document, final DefaultEntity entity, final List<String> productHeader)
            throws DocumentException {
        List<Entity> orders = entity.getHasManyField("orders");
        Map<ProxyEntity, BigDecimal> products = getBomSeries(entity, orders);
        PdfPTable table = PdfUtil.createTableWithHeader(4, productHeader);
        for (Entry<ProxyEntity, BigDecimal> entry : products.entrySet()) {
            table.addCell(new Phrase(entry.getKey().getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entry.getKey().getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            Object unit = entry.getKey().getField("unit");
            if (unit != null) {
                table.addCell(new Phrase(unit.toString(), PdfUtil.getArialRegular9Dark()));
            } else {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            }
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(df.format(entry.getValue().stripTrailingZeros()), PdfUtil.getArialBold9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        document.add(table);
    }
}
