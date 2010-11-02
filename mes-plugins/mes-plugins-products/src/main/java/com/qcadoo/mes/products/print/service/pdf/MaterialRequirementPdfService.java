package com.qcadoo.mes.products.print.service.pdf;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.internal.ProxyEntity;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.products.print.service.MaterialRequirementDocumentService;

@Service
public final class MaterialRequirementPdfService extends MaterialRequirementDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(MaterialRequirementPdfService.class);

    @Autowired
    private SecurityService securityService;

    private static final String FONT_PATH = "fonts/Arial.ttf";

    private static final String PDF_EXTENSION = ".pdf";

    @Override
    public void generateDocument(final Entity entity, final Locale locale) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        try {
            String fileName = getFileName((Date) entity.getField("date")) + PDF_EXTENSION;
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);
            buildPdfMetadata(document, locale);
            writer.createXmpMetadata();
            document.open();
            buildPdfContent(document, entity, locale, prepareFont());
            document.close();
            /*
             * PdfReader reader = new PdfReader(fileName); int n = reader.getNumberOfPages(); PdfStamper stamper = new
             * PdfStamper(reader, fileOutputStream); PdfContentByte page; Rectangle rect; BaseFont bf = BaseFont.createFont(); for
             * (int i = 1; i < n + 1; i++) { page = stamper.getOverContent(i); rect = reader.getPageSizeWithRotation(i);
             * page.beginText(); page.setFontAndSize(bf, 10); page.showTextAligned(Element.ALIGN_RIGHT, "Strona " + i + " z " + n,
             * rect.getRight(36), rect.getTop(32), 0); DottedLineSeparator dottedLine = new DottedLineSeparator();
             * dottedLine.setGap(2f); dottedLine.setPercentage(90f); dottedLine.setAlignment(Element.ALIGN_LEFT);
             * page.setLineDash(6f); // document.add(dottedLine); page.endText(); } stamper.close();
             */

        } catch (DocumentException e) {
            LOG.error("Problem with generating document - " + e.getMessage());
            document.close();
            throw e;
        }
    }

    private Font prepareFont() throws DocumentException, IOException {
        ClassPathResource classPathResource = new ClassPathResource(FONT_PATH);
        FontFactory.register(classPathResource.getPath());
        BaseFont baseFont = BaseFont.createFont(classPathResource.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font font = new Font(baseFont, 10);
        font.setColor(new Color(70, 70, 70));
        return font;
    }

    private void buildPdfContent(final Document document, final Entity entity, final Locale locale, final Font font)
            throws DocumentException {
        UsersUser user = securityService.getCurrentUser();
        SimpleDateFormat df = new SimpleDateFormat(DateType.DATE_TIME_FORMAT);
        Font font18 = new Font(font);
        font18.setSize(18);
        font18.setColor(new Color(70, 70, 70));
        LineSeparator line = new LineSeparator(3, 90f, new Color(102, 102, 102), Element.ALIGN_LEFT, 0);
        document.add(Chunk.NEWLINE);
        Paragraph title = new Paragraph(translationService.translate("products.materialRequirement.report.title", locale) + " "
                + entity.getField("name"), getFontBold(font18));
        title.setSpacingAfter(10f);
        document.add(title);
        document.add(line);
        PdfPTable userAndDate = new PdfPTable(2);
        userAndDate.setWidthPercentage(90f);
        userAndDate.setHorizontalAlignment(Element.ALIGN_LEFT);
        userAndDate.getDefaultCell().setBorderWidth(0);
        Paragraph userParagraph = new Paragraph(
                translationService.translate("products.materialRequirement.report.author", locale) + " " + user.getUserName(),
                font);
        Paragraph dateParagraph = new Paragraph(df.format(entity.getField("date")), font);
        userAndDate.addCell(userParagraph);
        userAndDate.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
        userAndDate.addCell(dateParagraph);
        document.add(userAndDate);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate("products.materialRequirement.report.paragrah", locale),
                getFontBold(font)));
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(translationService.translate("products.order.number.label", locale));
        orderHeader.add(translationService.translate("products.order.name.label", locale));
        orderHeader.add(translationService.translate("products.order.product.label", locale));
        List<Entity> instructions = addOrderSeries(document, entity, font, orderHeader);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate("products.materialRequirement.report.paragrah2", locale),
                getFontBold(font)));
        List<String> productHeader = new ArrayList<String>();
        productHeader.add(translationService.translate("products.product.number.label", locale));
        productHeader.add(translationService.translate("products.product.name.label", locale));
        productHeader.add(translationService.translate("products.product.unit.label", locale));
        productHeader.add(translationService.translate("products.instructionBomComponent.quantity.label", locale));
        addBomSeries(document, (DefaultEntity) entity, instructions, font, productHeader);
    }

    private void buildPdfMetadata(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("products.materialRequirement.report.title", locale));
        document.addSubject("Using iText");
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("QCADOO");
        document.addCreator("QCADOO");
    }

    private Font getFontBold(final Font font) {
        Font fontBold = new Font(font);
        fontBold.setStyle(Font.BOLD);
        return fontBold;
    }

    private List<Entity> addOrderSeries(final Document document, final Entity entity, final Font font,
            final List<String> orderHeader) throws DocumentException {
        List<Entity> orders = (List<Entity>) entity.getField("orders");
        List<Entity> instructions = new ArrayList<Entity>();
        PdfPTable table = createTableWithHeader(3, orderHeader, font);
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity instruction = (Entity) order.getField("instruction");
            if (instruction != null) {
                instructions.add(instruction);
            }
            table.addCell(new Phrase(order.getField("number").toString(), font));
            table.addCell(new Phrase(order.getField("name").toString(), font));
            Entity product = (Entity) order.getField("product");
            if (product != null) {
                table.addCell(new Phrase(product.getField("name").toString(), font));
            } else {
                table.addCell(new Phrase("", font));
            }
        }
        document.add(table);
        return instructions;
    }

    private PdfPTable createTableWithHeader(final int numOfColumns, final List<String> orderHeader, final Font font) {
        PdfPTable table = new PdfPTable(numOfColumns);
        table.setWidthPercentage(90f);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingBefore(10.0f);
        table.getDefaultCell().setBackgroundColor(new Color(230, 230, 230));
        table.getDefaultCell().setBorderColor(new Color(153, 153, 153));
        table.getDefaultCell().setPadding(5.0f);
        table.getDefaultCell().disableBorderSide(Rectangle.RIGHT);
        int i = 0;
        for (String element : orderHeader) {
            i++;
            if (i == orderHeader.size()) {
                table.getDefaultCell().enableBorderSide(Rectangle.RIGHT);
            }
            table.addCell(new Phrase(element, font));
            if (i == 1) {
                table.getDefaultCell().disableBorderSide(Rectangle.LEFT);
            }
        }
        table.getDefaultCell().setBackgroundColor(null);
        table.getDefaultCell().disableBorderSide(Rectangle.RIGHT);
        return table;
    }

    private void addBomSeries(final Document document, final DefaultEntity entity, final List<Entity> instructions,
            final Font font, final List<String> productHeader) throws DocumentException {
        Map<ProxyEntity, BigDecimal> products = getBomSeries(entity, instructions);
        PdfPTable table = createTableWithHeader(4, productHeader, font);
        for (Entry<ProxyEntity, BigDecimal> entry : products.entrySet()) {
            table.addCell(new Phrase(entry.getKey().getField("number").toString(), font));
            table.addCell(new Phrase(entry.getKey().getField("name").toString(), font));
            Object unit = entry.getKey().getField("unit");
            if (unit != null) {
                table.addCell(new Phrase(unit.toString(), font));
            } else {
                table.addCell(new Phrase("", font));
            }
            table.addCell(new Phrase(entry.getValue().toEngineeringString(), getFontBold(font)));
        }
        document.add(table);
    }
}
