package com.qcadoo.mes.products.print;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.internal.ProxyEntity;
import com.qcadoo.mes.model.types.internal.DateType;
import com.qcadoo.mes.products.print.pdf.util.PdfUtil;

@Service
public class ProductReportService {

    private static final SimpleDateFormat D_F = new SimpleDateFormat(DateType.DATE_FORMAT);

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    private static final String MATERIAL_COMPONENT = "component";

    public final Map<ProxyEntity, BigDecimal> getTechnologySeries(final Entity entity, final List<Entity> orders) {
        Map<ProxyEntity, BigDecimal> products = new HashMap<ProxyEntity, BigDecimal>();
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity technology = (Entity) order.getField("technology");
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            if (technology != null && plannedQuantity != null && plannedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                List<Entity> operationComponents = technology.getHasManyField("operationComponents");
                for (Entity operationComponent : operationComponents) {
                    List<Entity> operationProductComponents = operationComponent.getHasManyField("operationProductComponents");
                    for (Entity operationProductComponent : operationProductComponents) {
                        if ((Boolean) operationProductComponent.getField("inParameter")) {
                            ProxyEntity product = (ProxyEntity) operationProductComponent.getField("product");
                            if (!(Boolean) entity.getField("onlyComponents")
                                    || MATERIAL_COMPONENT.equals(product.getField("typeOfMaterial"))) {
                                if (products.containsKey(product)) {
                                    BigDecimal quantity = products.get(product);
                                    quantity = ((BigDecimal) operationProductComponent.getField("quantity")).multiply(
                                            plannedQuantity).add(quantity);
                                    products.put(product, quantity);
                                } else {
                                    products.put(product, ((BigDecimal) operationProductComponent.getField("quantity"))
                                            .multiply(plannedQuantity));
                                }
                            }
                        }
                    }
                }
            }
        }
        return products;
    }

    public final void addOrderHeader(final Document document, final Entity entity, final Locale locale, final DecimalFormat df)
            throws DocumentException {
        String documenTitle = translationService.translate("products.workPlan.report.title", locale);
        String documentAuthor = translationService.translate("products.materialRequirement.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, entity, documenTitle, documentAuthor, (Date) entity.getField("date"), user);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(translationService.translate("products.workPlan.report.paragrah", locale), PdfUtil
                .getArialBold11Dark()));
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(translationService.translate("products.order.number.label", locale));
        orderHeader.add(translationService.translate("products.order.name.label", locale));
        orderHeader.add(translationService.translate("products.order.product.label", locale));
        orderHeader.add(translationService.translate("products.order.plannedQuantity.label", locale));
        orderHeader.add(translationService.translate("products.product.unit.label", locale));
        orderHeader.add(translationService.translate("products.order.dateTo.label", locale));
        addOrderSeries(document, entity, orderHeader, df);
        document.add(Chunk.NEWLINE);
    }

    public final List<String> addOperationHeader(final Locale locale) {
        List<String> machineHeader = new ArrayList<String>();
        machineHeader.add(translationService.translate("products.operation.number.label", locale));
        machineHeader.add(translationService.translate("products.operation.name.label", locale));
        machineHeader.add(translationService.translate("products.workPlan.report.operationTable.order.column", locale));
        machineHeader.add(translationService.translate("products.workPlan.report.operationTable.productsOut.column", locale));
        machineHeader.add(translationService.translate("products.workPlan.report.operationTable.productsIn.column", locale));
        return machineHeader;
    }

    public final void addOrderSeries(final Document document, final Entity entity, final List<String> orderHeader,
            final DecimalFormat df) throws DocumentException {
        List<Entity> orders = entity.getHasManyField("orders");
        PdfPTable table = PdfUtil.createTableWithHeader(6, orderHeader);
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
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            plannedQuantity = (plannedQuantity == null) ? new BigDecimal(0) : plannedQuantity.stripTrailingZeros();
            table.addCell(new Phrase(df.format(plannedQuantity), PdfUtil.getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
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
            table.addCell(new Phrase(D_F.format((Date) order.getField("dateTo")), PdfUtil.getArialRegular9Dark()));
        }
        document.add(table);
    }

    public final PdfPTable addProductOutSeries(final PdfPTable table, final List<Entity> operationProductComponents) {
        boolean firstRow = true;
        for (Entity operationProductComponent : operationProductComponents) {
            if (!(Boolean) operationProductComponent.getField("inParameter")) {
                ProxyEntity product = (ProxyEntity) operationProductComponent.getField("product");
                Object unit = product.getField("unit");
                if (!firstRow) {
                    table.addCell("");
                    table.addCell("");
                    table.addCell("");
                }
                table.addCell(new Phrase(product.getField("number").toString() + " " + product.getField("name").toString()
                        + " x " + operationProductComponent.getField("quantity").toString() + " ["
                        + (unit != null ? unit.toString() : "") + "]", PdfUtil.getArialRegular9Dark()));
                table.addCell("");
                firstRow = false;
            }
        }
        if (firstRow) {
            table.addCell("");
            table.addCell("");
        }
        return table;
    }

    public final PdfPTable addProductInSeries(final PdfPTable table, final List<Entity> operationProductComponents) {
        for (Entity operationProductComponent : operationProductComponents) {
            if ((Boolean) operationProductComponent.getField("inParameter")) {
                ProxyEntity product = (ProxyEntity) operationProductComponent.getField("product");
                Object unit = product.getField("unit");
                table.addCell("");
                table.addCell("");
                table.addCell("");
                table.addCell("");
                table.addCell(new Phrase(product.getField("number").toString() + " " + product.getField("name").toString()
                        + " x " + operationProductComponent.getField("quantity").toString() + " ["
                        + (unit != null ? unit.toString() : "") + "]", PdfUtil.getArialRegular9Dark()));
            }
        }
        return table;
    }

    public final String copyContent(final Document document, final DefaultEntity entity, final PdfWriter writer)
            throws IOException, DocumentException {
        Object fileName = entity.getField("fileName");
        String fileNameWithoutPath = "";
        if (fileName != null && !"".equals(fileName.toString().trim())) {
            PdfUtil.copyPdf(document, writer, (String) fileName);
            fileNameWithoutPath = ((String) fileName).substring(((String) fileName).lastIndexOf("/") + 1);
        }
        return fileNameWithoutPath;
    }
}
