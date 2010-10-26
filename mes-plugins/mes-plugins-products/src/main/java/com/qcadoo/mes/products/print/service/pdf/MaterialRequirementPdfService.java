package com.qcadoo.mes.products.print.service.pdf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
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
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.internal.DataAccessService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.internal.ProxyEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.internal.InternalDataDefinition;

@Service
public final class MaterialRequirementPdfService {

    private static final Logger LOG = LoggerFactory.getLogger(MaterialRequirementPdfService.class);

    @Autowired
    protected TranslationService translationService;

    @Autowired
    protected SecurityService securityService;

    @Autowired
    DataAccessService dataAccessService;

    @Autowired
    DataDefinitionService dataDefinitionService;

    private static final String FONT_PATH = "fonts/Arial.ttf";

    private static final String FILE_DIR = "/Users/krna/Documents/workspace/";

    private static final String FILE_NAME = "MaterialRequirement";

    private static final String FILE_EXTENSION = ".pdf";

    private static final String DATE_FORMAT = "yyyy_MM_dd_HH_mm";

    public void generatePdfDocument(final Entity entity) {
        Document document = new Document(PageSize.A4);
        try {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            String fileName = FILE_DIR + FILE_NAME + df.format(new Date()) + FILE_EXTENSION;
            entity.setField("fileName", fileName);
            DataDefinition dataDefinition = dataDefinitionService.get("products", "materialRequirement");
            dataAccessService.save((InternalDataDefinition) dataDefinition, entity);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            // Build PDF document.
            document.open();
            ClassPathResource classPathResource = new ClassPathResource(FONT_PATH);
            FontFactory.register(classPathResource.getPath());
            BaseFont baseFont = BaseFont.createFont(classPathResource.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font12 = new Font(baseFont, 12);
            buildPdfContent(document, entity, null // request.getLocale()
                    , font12);
            buildPdfMetadata(document, null// request.getLocale()
            );
            writer.flush(); // ?
            document.close();
        } catch (FileNotFoundException e) {
            LOG.error("Problem with generating document - " + e.getMessage());
        } catch (DocumentException e) {
            LOG.error("Problem with generating document - " + e.getMessage());
            document.close();
        } catch (IOException e) {
            LOG.error("Problem with generating document - " + e.getMessage());
            document.close();
        }
    }

    private void buildPdfContent(final Document document, final Entity entity, final Locale locale, final Font font)
            throws DocumentException {
        UsersUser user = securityService.getCurrentUser();
        document.add(new Paragraph(entity.getField("date").toString(), font));
        document.add(new Paragraph(user.getUserName(), font));
        document.add(new Paragraph(""// translationService.translate("products.materialRequirement.report.paragrah", locale)
                , getFontBold(font)));
        List<Entity> instructions = addOrderSeries(document, entity, font);
        document.add(new Paragraph(""// translationService.translate("products.materialRequirement.report.paragrah2", locale)
                , getFontBold(font)));
        addBomSeries(document, (DefaultEntity) entity, instructions, font);
    }

    private void buildPdfMetadata(final Document document, final Locale locale) {
        document.addTitle(""// translationService.translate("products.materialRequirement.report.title", locale)
        );
        // TODO KRNA add to properties ?
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
        Map<ProxyEntity, BigDecimal> products = new HashedMap();
        for (Entity instruction : instructions) {
            List<Entity> bomComponents = (List<Entity>) instruction.getField("bomComponents");
            for (Entity bomComponent : bomComponents) {
                ProxyEntity product = (ProxyEntity) bomComponent.getField("product");
                if (!(Boolean) entity.getField("onlyComponents") || "component".equals(product.getField("typeOfMaterial"))) {
                    if (products.containsKey(product)) {
                        BigDecimal quantity = products.get(product);
                        quantity = ((BigDecimal) bomComponent.getField("quantity")).add(quantity);
                        products.put(product, quantity);
                    } else {
                        products.put(product, (BigDecimal) bomComponent.getField("quantity"));
                    }
                }
            }
        }
        for (Entity product : products.keySet()) {
            document.add(new Paragraph(product.getField("number") + " " + product.getField("name") + " " + products.get(product)
                    + " " + product.getField("unit"), font));
        }
    }

}
