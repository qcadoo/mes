package com.qcadoo.mes.products.print;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.beans.users.UsersUser;
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

    public final Map<Entity, BigDecimal> getTechnologySeries(final Entity entity, final List<Entity> orders) {
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity technology = (Entity) order.getField("technology");
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            if (technology != null && plannedQuantity != null && plannedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                List<Entity> operationComponents = technology.getHasManyField("operationComponents");
                for (Entity operationComponent : operationComponents) {
                    List<Entity> operationProductComponents = operationComponent.getHasManyField("operationProductInComponents");
                    for (Entity operationProductComponent : operationProductComponents) {
                        Entity product = (Entity) operationProductComponent.getField("product");
                        if (!(Boolean) entity.getField("onlyComponents")
                                || MATERIAL_COMPONENT.equals(product.getField("typeOfMaterial"))) {
                            if (products.containsKey(product)) {
                                BigDecimal quantity = products.get(product);
                                quantity = ((BigDecimal) operationProductComponent.getField("quantity"))
                                        .multiply(plannedQuantity).add(quantity);
                                products.put(product, quantity);
                            } else {
                                products.put(product,
                                        ((BigDecimal) operationProductComponent.getField("quantity")).multiply(plannedQuantity));
                            }
                        }
                    }
                }
            }
        }
        return products;
    }

    private Map<Entity, Map<Entity, Entity>> getOperationSeries(final Entity entity, final boolean isMachine) {
        Map<Entity, Map<Entity, Entity>> operations = new HashMap<Entity, Map<Entity, Entity>>();
        List<Entity> orders = entity.getHasManyField("orders");
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity technology = (Entity) order.getField("technology");
            if (technology != null) {
                List<Entity> operationComponents = technology.getHasManyField("operationComponents");
                for (Entity operationComponent : operationComponents) {
                    Entity operation = (Entity) operationComponent.getField("operation");
                    Entity entityKey = null;
                    if (isMachine) {
                        entityKey = (Entity) operation.getField("machine");
                    } else {
                        entityKey = (Entity) operation.getField("staff");
                    }
                    if (operations.containsKey(entityKey)) {
                        Map<Entity, Entity> operationMap = operations.get(entityKey);
                        operationMap.put(operationComponent, order);
                    } else {
                        Map<Entity, Entity> operationMap = new HashMap<Entity, Entity>();
                        operationMap.put(operationComponent, order);
                        operations.put(entityKey, operationMap);
                    }
                }
            }
        }
        return operations;
    }

    public void addOperationSeries(final Document document, final Entity entity, final Locale locale, final boolean isMachine)
            throws DocumentException {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
        boolean firstPage = true;
        Map<Entity, Map<Entity, Entity>> operations = getOperationSeries(entity, isMachine);
        for (Entry<Entity, Map<Entity, Entity>> entry : operations.entrySet()) {
            if (!firstPage) {
                document.newPage();
            }
            PdfPTable orderTable = PdfUtil.createTableWithHeader(6, getOrderHeader(document, entity, locale));
            addOrderSeries(orderTable, entity, decimalFormat);
            document.add(orderTable);
            document.add(Chunk.NEWLINE);
            if (isMachine) {
                Entity machine = entry.getKey();
                document.add(new Paragraph(translationService.translate("products.workPlan.report.paragrah3", locale) + " "
                        + machine.getField("name"), PdfUtil.getArialBold11Dark()));
            } else {
                Entity staff = entry.getKey();
                document.add(new Paragraph(translationService.translate("products.workPlan.report.paragrah2", locale) + " "
                        + staff.getField("name") + " " + staff.getField("surname"), PdfUtil.getArialBold11Dark()));
            }
            PdfPTable table = PdfUtil.createTableWithHeader(5, getOperationHeader(locale));
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
            Map<Entity, Entity> operationMap = entry.getValue();
            for (Entry<Entity, Entity> entryComponent : operationMap.entrySet()) {
                Entity operation = (Entity) entryComponent.getKey().getField("operation");
                table.addCell(new Phrase(operation.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
                table.addCell(new Phrase(operation.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
                table.addCell(new Phrase(entryComponent.getValue().getField("number").toString(), PdfUtil.getArialRegular9Dark()));
                List<Entity> operationProductOutComponents = entryComponent.getKey().getHasManyField(
                        "operationProductOutComponents");
                List<Entity> operationProductInComponents = entryComponent.getKey().getHasManyField(
                        "operationProductInComponents");
                addProductSeries(table, operationProductOutComponents, decimalFormat);
                addProductSeries(table, operationProductInComponents, decimalFormat);
            }
            document.add(table);
            firstPage = false;
        }
    }

    private void addProductSeries(final PdfPTable table, final List<Entity> operationProductComponents, final DecimalFormat df) {
        StringBuilder products = new StringBuilder();
        for (Entity operationProductComponent : operationProductComponents) {
            ProxyEntity product = (ProxyEntity) operationProductComponent.getField("product");
            Object unit = product.getField("unit");
            products.append(product.getField("number").toString() + " " + product.getField("name").toString() + " x "
                    + df.format(((BigDecimal) operationProductComponent.getField("quantity")).stripTrailingZeros()) + " ["
                    + (unit != null ? unit.toString() : "") + "] \n\n");
        }
        table.addCell(new Phrase(products.toString(), PdfUtil.getArialRegular9Dark()));
    }

    private List<String> getOrderHeader(final Document document, final Entity entity, final Locale locale)
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
        return orderHeader;
    }

    private List<String> getOperationHeader(final Locale locale) {
        List<String> operationHeader = new ArrayList<String>();
        operationHeader.add(translationService.translate("products.operation.number.label", locale));
        operationHeader.add(translationService.translate("products.operation.name.label", locale));
        operationHeader.add(translationService.translate("products.workPlan.report.operationTable.order.column", locale));
        operationHeader.add(translationService.translate("products.workPlan.report.operationTable.productsOut.column", locale));
        operationHeader.add(translationService.translate("products.workPlan.report.operationTable.productsIn.column", locale));
        return operationHeader;
    }

    private void addOrderSeries(final PdfPTable table, final Entity entity, final DecimalFormat df) throws DocumentException {
        List<Entity> orders = entity.getHasManyField("orders");
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
    }

}
