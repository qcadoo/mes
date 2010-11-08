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
import com.lowagie.text.pdf.PdfContentByte;
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

    public static final String FONT_PATH = "fonts/Arial.ttf";

    private static final String PDF_EXTENSION = ".pdf";

    private Font arialBold19Light;

    private Font arialBold19Dark;

    private Font arialBold11Dark;

    private Font arialRegular9Light;

    private Font arialRegular9Dark;

    private Font arialBold9Dark;

    private Color lineLightColor;

    private Color lineDarkColor;

    private Color backgroundColor;

    private Color lightColor;

    private BaseFont arial;

    @Override
    public void generateDocument(final Entity entity, final Locale locale) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        try {
            String fileName = getFileName((Date) entity.getField("date")) + PDF_EXTENSION;
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);
            writer.setPageEvent(new PdfPageNumbering(translationService.translate("products.report.page", locale),
                    translationService.translate("products.report.in", locale)));
            document.setMargins(8, 80, 40, 100);
            buildPdfMetadata(document, locale);
            prepareFontsAndColors();
            writer.createXmpMetadata();
            document.open();
            buildPdfContent(document, entity, locale);
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            cb.setColorFill(lightColor);
            String text = translationService.translate("products.report.endOfReport", locale);
            float textBase = document.bottom() - 35;
            float textSize = arial.getWidthPoint(text, 7);
            cb.beginText();
            cb.setFontAndSize(arial, 7);
            cb.setTextMatrix(document.right() - textSize, textBase);
            cb.showText(text);
            cb.endText();
            cb.restoreState();
            document.close();
        } catch (DocumentException e) {
            LOG.error("Problem with generating document - " + e.getMessage());
            document.close();
            throw e;
        }
    }

    private void prepareFontsAndColors() throws DocumentException, IOException {
        ClassPathResource classPathResource = new ClassPathResource(FONT_PATH);
        FontFactory.register(classPathResource.getPath());
        arial = BaseFont.createFont(classPathResource.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Color light = new Color(77, 77, 77);
        Color dark = new Color(26, 26, 26);
        lineDarkColor = new Color(102, 102, 102);
        lineLightColor = new Color(153, 153, 153);
        lightColor = new Color(77, 77, 77);
        backgroundColor = new Color(230, 230, 230);
        arialBold19Light = new Font(arial, 19);
        arialBold19Light.setStyle(Font.BOLD);
        arialBold19Light.setColor(light);
        arialBold19Dark = new Font(arial, 19);
        arialBold19Dark.setStyle(Font.BOLD);
        arialBold19Dark.setColor(dark);
        arialRegular9Light = new Font(arial, 9);
        arialRegular9Light.setColor(light);
        arialRegular9Dark = new Font(arial, 9);
        arialRegular9Dark.setColor(dark);
        arialBold9Dark = new Font(arial, 9);
        arialBold9Dark.setColor(dark);
        arialBold9Dark.setStyle(Font.BOLD);
        arialBold11Dark = new Font(arial, 11);
        arialBold11Dark.setColor(dark);
        arialBold11Dark.setStyle(Font.BOLD);
    }

    private void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        UsersUser user = securityService.getCurrentUser();
        SimpleDateFormat df = new SimpleDateFormat(DateType.DATE_TIME_FORMAT);
        LineSeparator line = new LineSeparator(3, 100f, lineDarkColor, Element.ALIGN_LEFT, 0);
        document.add(Chunk.NEWLINE);
        Paragraph title = new Paragraph(new Phrase(translationService.translate("products.materialRequirement.report.title",
                locale), arialBold19Light));
        title.add(new Phrase(" " + entity.getField("name"), arialBold19Dark));
        title.setSpacingAfter(7f);
        document.add(title);
        document.add(line);
        PdfPTable userAndDate = new PdfPTable(2);
        userAndDate.setWidthPercentage(100f);
        userAndDate.setHorizontalAlignment(Element.ALIGN_LEFT);
        userAndDate.getDefaultCell().setBorderWidth(0);
        Paragraph userParagraph = new Paragraph(new Phrase(translationService.translate(
                "products.materialRequirement.report.author", locale), arialRegular9Light));
        userParagraph.add(new Phrase(" " + user.getUserName(), arialRegular9Dark));
        Paragraph dateParagraph = new Paragraph(df.format(entity.getField("date")), arialRegular9Light);
        userAndDate.addCell(userParagraph);
        userAndDate.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
        userAndDate.addCell(dateParagraph);
        document.add(userAndDate);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate("products.materialRequirement.report.paragrah", locale),
                arialBold11Dark));
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(translationService.translate("products.order.number.label", locale));
        orderHeader.add(translationService.translate("products.order.name.label", locale));
        orderHeader.add(translationService.translate("products.order.product.label", locale));
        orderHeader.add(translationService.translate("products.product.unit.label", locale));
        orderHeader.add(translationService.translate("products.order.plannedQuantity.label", locale));
        addOrderSeries(document, entity, orderHeader);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate("products.materialRequirement.report.paragrah2", locale),
                arialBold11Dark));
        List<String> productHeader = new ArrayList<String>();
        productHeader.add(translationService.translate("products.product.number.label", locale));
        productHeader.add(translationService.translate("products.product.name.label", locale));
        productHeader.add(translationService.translate("products.product.unit.label", locale));
        productHeader.add(translationService.translate("products.instructionBomComponent.quantity.label", locale));
        addBomSeries(document, (DefaultEntity) entity, productHeader);
    }

    private void buildPdfMetadata(final Document document, final Locale locale) {
        document.addTitle(translationService.translate("products.materialRequirement.report.title", locale));
        document.addSubject("Using iText");
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("QCADOO");
        document.addCreator("QCADOO");
    }

    private void addOrderSeries(final Document document, final Entity entity, final List<String> orderHeader)
            throws DocumentException {
        List<Entity> orders = entity.getHasManyField("orders");
        PdfPTable table = createTableWithHeader(5, orderHeader, arialRegular9Dark);
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            table.addCell(new Phrase(order.getField("number").toString(), arialRegular9Dark));
            table.addCell(new Phrase(order.getField("name").toString(), arialRegular9Dark));
            Entity product = (Entity) order.getField("product");
            if (product != null) {
                table.addCell(new Phrase(product.getField("name").toString(), arialRegular9Dark));
            } else {
                table.addCell(new Phrase("", arialRegular9Dark));
            }
            if (product != null) {
                Object unit = product.getField("unit");
                if (unit != null) {
                    table.addCell(new Phrase(unit.toString(), arialRegular9Dark));
                } else {
                    table.addCell(new Phrase("", arialRegular9Dark));
                }
            } else {
                table.addCell(new Phrase("", arialRegular9Dark));
            }
            table.addCell(new Phrase(order.getField("plannedQuantity").toString(), arialRegular9Dark));
        }
        document.add(table);

    }

    private PdfPTable createTableWithHeader(final int numOfColumns, final List<String> orderHeader, final Font font) {
        PdfPTable table = new PdfPTable(numOfColumns);
        table.setWidthPercentage(100f);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingBefore(7.0f);
        table.getDefaultCell().setBackgroundColor(backgroundColor);
        table.getDefaultCell().setBorderColor(lineDarkColor);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
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
        table.getDefaultCell().setBorderColor(lineLightColor);
        return table;
    }

    private void addBomSeries(final Document document, final DefaultEntity entity, final List<String> productHeader)
            throws DocumentException {
        List<Entity> orders = entity.getHasManyField("orders");
        Map<ProxyEntity, BigDecimal> products = getBomSeries(entity, orders);
        PdfPTable table = createTableWithHeader(4, productHeader, arialRegular9Dark);
        for (Entry<ProxyEntity, BigDecimal> entry : products.entrySet()) {
            table.addCell(new Phrase(entry.getKey().getField("number").toString(), arialRegular9Dark));
            table.addCell(new Phrase(entry.getKey().getField("name").toString(), arialRegular9Dark));
            Object unit = entry.getKey().getField("unit");
            if (unit != null) {
                table.addCell(new Phrase(unit.toString(), arialRegular9Dark));
            } else {
                table.addCell(new Phrase("", arialRegular9Dark));
            }
            table.addCell(new Phrase(entry.getValue().toEngineeringString(), arialBold9Dark));
        }
        document.add(table);
    }
}
