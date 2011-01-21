package com.qcadoo.mes.products.print;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
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

    private static final Logger LOG = LoggerFactory.getLogger(ProductReportService.class);

    private static final SimpleDateFormat D_F = new SimpleDateFormat(DateType.DATE_FORMAT);

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    private static final String MATERIAL_COMPONENT = "01component";

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

    private Map<Entity, Map<Entity, List<Entity>>> getOperationSeries(final Entity entity, final String type) {
        Map<Entity, Map<Entity, List<Entity>>> operations = new HashMap<Entity, Map<Entity, List<Entity>>>();
        List<Entity> orders = entity.getHasManyField("orders");
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity technology = (Entity) order.getField("technology");
            if (technology != null) {
                List<Entity> operationComponents = technology.getHasManyField("operationComponents");

                Entity entityKey = null;

                if (type.equals("product")) {
                    Entity product = (Entity) order.getField("product");
                    entityKey = product;
                }

                for (Entity operationComponent : operationComponents) {
                    Entity operation = (Entity) operationComponent.getField("operation");

                    if (type.equals("machine")) {
                        Object machine = operation.getField("machine");
                        if (machine != null) {
                            entityKey = (Entity) machine;
                        }
                    } else if (type.equals("worker")) {
                        Object machine = operation.getField("staff");
                        if (machine != null) {
                            entityKey = (Entity) machine;
                        }
                    }
                    if (operations.containsKey(entityKey)) {
                        Map<Entity, List<Entity>> operationMap = operations.get(entityKey);
                        List<Entity> ordersList;
                        if (operationMap.containsKey(operationComponent)) {
                            ordersList = operationMap.get(operationComponent);
                        } else {
                            ordersList = new ArrayList<Entity>();
                        }
                        ordersList.add(order);
                        operationMap.put(operationComponent, ordersList);
                    } else {
                        Map<Entity, List<Entity>> operationMap = new HashMap<Entity, List<Entity>>();
                        List<Entity> ordersList = new ArrayList<Entity>();
                        ordersList.add(order);
                        operationMap.put(operationComponent, ordersList);
                        operations.put(entityKey, operationMap);
                    }
                }
            }
        }
        return operations;
    }

    public void addOperationSeries(final Document document, final Entity entity, final Locale locale, final String type)
            throws DocumentException {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
        decimalFormat.setMaximumFractionDigits(3);
        decimalFormat.setMinimumFractionDigits(3);
        boolean firstPage = true;
        Map<Entity, Map<Entity, List<Entity>>> operations = getOperationSeries(entity, type);
        for (Entry<Entity, Map<Entity, List<Entity>>> entry : operations.entrySet()) {
            if (!firstPage) {
                document.newPage();
            }

            PdfPTable orderTable = PdfUtil.createTableWithHeader(6, getOrderHeader(document, entity, locale), false);

            if (type.equals("machine") || type.equals("worker")) {
                addOrderSeries(orderTable, entity, decimalFormat);
                document.add(orderTable);
                document.add(Chunk.NEWLINE);
            }

            if (type.equals("machine")) {
                Entity machine = entry.getKey();
                Paragraph title = new Paragraph(new Phrase(translationService.translate("products.workPlan.report.paragrah3",
                        locale), PdfUtil.getArialBold11Light()));
                String name = "";
                if (machine != null) {
                    name = machine.getField("name").toString();
                }
                title.add(new Phrase(" " + name, PdfUtil.getArialBold19Dark()));
                document.add(title);
            } else if (type.equals("worker")) {
                Entity staff = entry.getKey();
                Paragraph title = new Paragraph(new Phrase(translationService.translate("products.workPlan.report.paragrah2",
                        locale), PdfUtil.getArialBold11Light()));
                String name = "";
                if (staff != null) {
                    name = staff.getField("name") + " " + staff.getField("surname");
                }
                title.add(new Phrase(" " + name, PdfUtil.getArialBold19Dark()));
                document.add(title);
            } else if (type.equals("product")) {
                Entity product = entry.getKey();
                Paragraph title = new Paragraph(new Phrase(translationService.translate("products.workPlan.report.paragrah4",
                        locale), PdfUtil.getArialBold11Light()));

                Map<Entity, List<Entity>> values = entry.getValue();
                List<List<Entity>> orders = new ArrayList<List<Entity>>(values.values());

                Double totalQuantity = 0.0;

                Set<String> addedOrders = new HashSet<String>();

                for (List<Entity> singleOrderList : orders) {
                    for (Entity singleOrder : singleOrderList) {
                        if (!addedOrders.contains(singleOrder.getField("name").toString())) {
                            totalQuantity += Double.parseDouble(singleOrder.getField("plannedQuantity").toString());
                            addedOrders.add(singleOrder.getField("name").toString());
                        }
                    }
                }

                addOrderSeries(orderTable, orders, decimalFormat);

                document.add(orderTable);
                document.add(Chunk.NEWLINE);

                title.add(new Phrase(" " + totalQuantity + " x " + product.getField("name"), PdfUtil.getArialBold19Dark()));
                document.add(title);

            }
            PdfPTable table = PdfUtil.createTableWithHeader(5, getOperationHeader(locale), false);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
            Map<Entity, List<Entity>> operationMap = entry.getValue();
            for (Entry<Entity, List<Entity>> entryComponent : operationMap.entrySet()) {
                for (Entity singleOperationComponent : entryComponent.getValue()) {
                    Entity operation = (Entity) entryComponent.getKey().getField("operation");
                    table.addCell(new Phrase(operation.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
                    table.addCell(new Phrase(operation.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
                    table.addCell(new Phrase(singleOperationComponent.getField("number").toString(), PdfUtil
                            .getArialRegular9Dark()));
                    List<Entity> operationProductOutComponents = entryComponent.getKey().getHasManyField(
                            "operationProductOutComponents");
                    List<Entity> operationProductInComponents = entryComponent.getKey().getHasManyField(
                            "operationProductInComponents");
                    addProductSeries(table, operationProductOutComponents, decimalFormat, singleOperationComponent);
                    addProductSeries(table, operationProductInComponents, decimalFormat, singleOperationComponent);
                }
            }
            document.add(table);
            firstPage = false;
        }
    }

    private Image generateBarcode(final String code) throws BadElementException {
        Code128Bean codeBean = new Code128Bean();
        final int dpi = 150;

        codeBean.setModuleWidth(UnitConv.in2mm(1.0f / dpi));
        codeBean.doQuietZone(false);
        codeBean.setHeight(8);
        codeBean.setFontSize(0.0);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(out, "image/x-png", dpi, BufferedImage.TYPE_BYTE_BINARY,
                    false, 0);

            codeBean.generateBarcode(canvas, code);

            canvas.finish();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }

        try {
            Image image = Image.getInstance(out.toByteArray());
            image.setAlignment(Image.RIGHT);

            return image;
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }

    private void addProductSeries(final PdfPTable table, final List<Entity> operationProductComponents, final DecimalFormat df,
            final Entity entity) {
        StringBuilder products = new StringBuilder();
        for (Entity operationProductComponent : operationProductComponents) {
            ProxyEntity product = (ProxyEntity) operationProductComponent.getField("product");
            Object unit = product.getField("unit");

            Double quantity = Double.parseDouble(operationProductComponent.getField("quantity").toString())
                    * Double.parseDouble(entity.getField("plannedQuantity").toString());

            // operationProductComponent.getField("quantity")

            products.append(product.getField("number").toString() + " " + product.getField("name").toString() + " x "
                    + df.format(quantity) + " [" + (unit != null ? unit.toString() : "") + "] \n\n");
        }
        table.addCell(new Phrase(products.toString(), PdfUtil.getArialRegular9Dark()));
    }

    private List<String> getOrderHeader(final Document document, final Entity entity, final Locale locale)
            throws DocumentException {
        String documenTitle = translationService.translate("products.workPlan.report.title", locale);
        String documentAuthor = translationService.translate("products.materialRequirement.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, entity, documenTitle, documentAuthor, (Date) entity.getField("date"), user);
        document.add(generateBarcode(entity.getField("name").toString()));
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
            plannedQuantity = (plannedQuantity == null) ? BigDecimal.ZERO : plannedQuantity;
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

    private void addOrderSeries(final PdfPTable table, final List<List<Entity>> values, final DecimalFormat df)
            throws DocumentException {

        Set<String> added = new HashSet<String>();

        for (List<Entity> entry : values) {
            List<Entity> orderList = entry;
            for (Entity order : orderList) {
                if (!added.contains(order.getField("name").toString())) {
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
                    plannedQuantity = (plannedQuantity == null) ? BigDecimal.ZERO : plannedQuantity;
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
                added.add(order.getField("name").toString());
            }
        }
    }

}
