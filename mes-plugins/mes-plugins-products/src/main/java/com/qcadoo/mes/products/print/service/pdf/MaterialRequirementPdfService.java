package com.qcadoo.mes.products.print.service.pdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
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
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            buildPdfContent(document, entity, locale, prepareFont());
            buildPdfMetadata(document, locale);
            document.close();
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
        return new Font(baseFont, 12);
    }

    private void buildPdfContent(final Document document, final Entity entity, final Locale locale, final Font font)
            throws DocumentException {
        UsersUser user = securityService.getCurrentUser();
        SimpleDateFormat df = new SimpleDateFormat(DateType.DATE_TIME_FORMAT);
        document.add(new Paragraph(df.format(entity.getField("date")), font));
        document.add(new Paragraph(user.getUserName(), font));
        document.add(new Paragraph(translationService.translate("products.materialRequirement.report.paragrah", locale),
                getFontBold(font)));
        List<Entity> instructions = addOrderSeries(document, entity, font);
        document.add(new Paragraph(translationService.translate("products.materialRequirement.report.paragrah2", locale),
                getFontBold(font)));
        addBomSeries(document, (DefaultEntity) entity, instructions, font);
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

    private List<Entity> addOrderSeries(final Document document, final Entity entity, final Font font) throws DocumentException {
        List<Entity> orders = (List<Entity>) entity.getField("orders");
        List<Entity> instructions = new ArrayList<Entity>();
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity instruction = (Entity) order.getField("instruction");
            if (instruction != null) {
                instructions.add(instruction);
            }
            document.add(new Paragraph(order.getField("number") + " " + order.getField("name"), font));
        }
        return instructions;
    }

    private void addBomSeries(final Document document, final DefaultEntity entity, final List<Entity> instructions,
            final Font font) throws DocumentException {
        Map<ProxyEntity, BigDecimal> products = getBomSeries(entity, instructions);
        for (Entity product : products.keySet()) {
            document.add(new Paragraph(product.getField("number") + " " + product.getField("name") + " " + products.get(product)
                    + " " + product.getField("unit"), font));
        }
    }
}
