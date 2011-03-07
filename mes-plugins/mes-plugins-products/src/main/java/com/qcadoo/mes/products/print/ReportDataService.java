/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.products.print;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.products.util.EntityNumberComparator;
import com.qcadoo.mes.products.util.EntityOperationInPairNumberComparator;
import com.qcadoo.mes.utils.Pair;
import com.qcadoo.mes.utils.SortUtil;
import com.qcadoo.mes.utils.pdf.PdfUtil;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.utils.DateUtils;
import com.qcadoo.model.beans.users.UsersUser;

@Service
public class ReportDataService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportDataService.class);

    private static final SimpleDateFormat D_F = new SimpleDateFormat(DateUtils.DATE_FORMAT);

    private final int[] defaultWorkPlanColumnWidth = new int[] { 20, 20, 20, 13, 13, 13 };

    private final int[] defaultWorkPlanOperationColumnWidth = new int[] { 10, 21, 23, 23, 23 };

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    private static final String MATERIAL_COMPONENT = "01component";

    private static final String MATERIAL_WASTE = "04waste";

    private static final String COMPONENT_QUANTITY_ALGORITHM = "02perTechnology";

    private static final String OPERATION_NODE_ENTITY_TYPE = "operation";

    public final void addOperationsFromSubtechnologiesToList(final EntityTree entityTree, final List<Entity> operationComponents) {
        for (Entity operationComponent : entityTree) {
            if (OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
                operationComponents.add(operationComponent);
            } else {
                addOperationsFromSubtechnologiesToList(
                        operationComponent.getBelongsToField("referenceTechnology").getTreeField("operationComponents"),
                        operationComponents);
            }
        }
    }

    public final Map<Entity, BigDecimal> prepareTechnologySeries(final Entity entity) {
        Map<Entity, BigDecimal> products = new HashMap<Entity, BigDecimal>();
        List<Entity> orders = entity.getHasManyField("orders");
        Boolean onlyComponents = (Boolean) entity.getField("onlyComponents");
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity technology = (Entity) order.getField("technology");
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            if (technology != null && plannedQuantity != null && plannedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                countQuantityForProductsIn(products, technology, plannedQuantity, onlyComponents);
            }
        }
        return products;
    }

    public final void countQuantityForProductsIn(final Map<Entity, BigDecimal> products, final Entity technology,
            final BigDecimal plannedQuantity, final Boolean onlyComponents) {
        EntityTree operationComponents = technology.getTreeField("operationComponents");
        if (COMPONENT_QUANTITY_ALGORITHM.equals(technology.getField("componentQuantityAlgorithm"))) {
            countQuntityComponentPerTechnology(products, operationComponents, onlyComponents, plannedQuantity);
        } else {
            Map<Entity, BigDecimal> orderProducts = new HashMap<Entity, BigDecimal>();
            EntityTreeNode rootNode = operationComponents.getRoot();
            if (rootNode != null) {
                boolean success = countQuntityComponentPerOutProducts(orderProducts, rootNode, onlyComponents, plannedQuantity);
                if (success) {
                    for (Entry<Entity, BigDecimal> entry : orderProducts.entrySet()) {
                        if (!onlyComponents || MATERIAL_COMPONENT.equals(entry.getKey().getField("typeOfMaterial"))) {
                            if (products.containsKey(entry.getKey())) {
                                products.put(entry.getKey(), products.get(entry.getKey()).add(entry.getValue()));
                            } else {
                                products.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }
            }
        }
    }

    public final void addSeries(final Document document, final Entity entity, final Locale locale, final String type)
            throws DocumentException {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
        decimalFormat.setMaximumFractionDigits(3);
        decimalFormat.setMinimumFractionDigits(3);
        boolean firstPage = true;
        for (Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> entry : prepareOperationSeries(
                entity, type).entrySet()) {
            if (!firstPage) {
                document.newPage();
            }

            List<Entity> orders = new ArrayList<Entity>();

            BigDecimal totalQuantity = createUniqueOrdersList(orders, entry);

            PdfPTable orderTable = PdfUtil.createTableWithHeader(6, prepareOrderHeader(document, entity, locale), false,
                    defaultWorkPlanColumnWidth);
            addOrderSeries(orderTable, orders, decimalFormat);
            document.add(orderTable);
            document.add(Chunk.NEWLINE);

            document.add(prepareTitle(totalQuantity, entry, locale, type));

            addOperationSeries(entry, document, decimalFormat, locale);
            firstPage = false;
        }
    }

    private void countQuntityComponentPerTechnology(final Map<Entity, BigDecimal> products,
            final List<Entity> operationComponents, final boolean onlyComponents, final BigDecimal plannedQuantity) {
        for (Entity operationComponent : operationComponents) {
            if (OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
                List<Entity> operationProductComponents = operationComponent.getHasManyField("operationProductInComponents");
                for (Entity operationProductComponent : operationProductComponents) {
                    Entity product = (Entity) operationProductComponent.getField("product");
                    if (!onlyComponents || MATERIAL_COMPONENT.equals(product.getField("typeOfMaterial"))) {
                        if (products.containsKey(product)) {
                            BigDecimal quantity = products.get(product);
                            quantity = ((BigDecimal) operationProductComponent.getField("quantity")).multiply(plannedQuantity,
                                    MathContext.DECIMAL128).add(quantity);
                            products.put(product, quantity);
                        } else {
                            products.put(product, ((BigDecimal) operationProductComponent.getField("quantity")).multiply(
                                    plannedQuantity, MathContext.DECIMAL128));
                        }
                    }
                }
            } else {
                countQuntityComponentPerTechnology(products, operationComponent.getBelongsToField("referenceTechnology")
                        .getTreeField("operationComponents"), onlyComponents, plannedQuantity);
            }
        }
    }

    private boolean countQuntityComponentPerOutProducts(final Map<Entity, BigDecimal> products, final EntityTreeNode node,
            final boolean onlyComponents, final BigDecimal plannedQuantity) {
        if (OPERATION_NODE_ENTITY_TYPE.equals(node.getField("entityType"))) {
            List<Entity> operationProductInComponents = node.getHasManyField("operationProductInComponents");
            if (operationProductInComponents.size() == 0) {
                return false;
            }
            Entity productOutComponent = checkOutProducts(node);
            if (productOutComponent == null) {
                return false;
            }
            for (Entity operationProductInComponent : operationProductInComponents) {
                Entity product = (Entity) operationProductInComponent.getField("product");
                BigDecimal quantity = ((BigDecimal) operationProductInComponent.getField("quantity")).multiply(plannedQuantity,
                        MathContext.DECIMAL128).divide((BigDecimal) productOutComponent.getField("quantity"),
                        MathContext.DECIMAL128);
                EntityTreeNode prevOperation = findPreviousOperation(node, product);
                if (prevOperation != null) {
                    boolean success = countQuntityComponentPerOutProducts(products, prevOperation, onlyComponents, quantity);
                    if (!success) {
                        return false;
                    }
                }
                if (products.containsKey(product)) {
                    products.put(product, products.get(product).add(quantity));
                } else {
                    products.put(product, quantity);
                }
            }
        } else {
            EntityTreeNode rootNode = node.getBelongsToField("referenceTechnology").getTreeField("operationComponents").getRoot();
            if (rootNode != null) {
                boolean success = countQuntityComponentPerOutProducts(products, rootNode, onlyComponents, plannedQuantity);
                if (!success) {
                    return false;
                }
            }
        }
        return true;
    }

    private Entity checkOutProducts(final Entity operationComponent) {
        List<Entity> operationProductOutComponents = operationComponent.getHasManyField("operationProductOutComponents");
        Entity productOutComponent = null;
        if (operationProductOutComponents.size() == 0) {
            return null;
        } else {
            int productCount = 0;
            for (Entity operationProductOutComponent : operationProductOutComponents) {
                Entity product = (Entity) operationProductOutComponent.getField("product");
                if (!MATERIAL_WASTE.equals(product.getField("typeOfMaterial"))) {
                    productOutComponent = operationProductOutComponent;
                    productCount++;
                }
            }
            if (productCount != 1) {
                return null;
            }
        }
        return productOutComponent;
    }

    private EntityTreeNode findPreviousOperation(final EntityTreeNode node, final Entity product) {
        for (EntityTreeNode operationComponent : node.getChildren()) {
            List<Entity> operationProductOutComponents = new ArrayList<Entity>();
            if (OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
                operationProductOutComponents = operationComponent.getHasManyField("operationProductOutComponents");
            } else {
                EntityTreeNode rootNode = operationComponent.getBelongsToField("referenceTechnology")
                        .getTreeField("operationComponents").getRoot();
                if (rootNode != null) {
                    operationProductOutComponents = rootNode.getHasManyField("operationProductOutComponents");
                }
            }
            for (Entity operationProductOutComponent : operationProductOutComponents) {
                Entity productOut = (Entity) operationProductOutComponent.getField("product");
                if (!MATERIAL_WASTE.equals(productOut.getField("typeOfMaterial"))
                        && productOut.getField("number").equals(product.getField("number"))) {
                    return operationComponent;
                }
            }
        }
        return null;
    }

    public final Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> prepareOperationSeries(
            final Entity entity, final String type) {
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = new HashMap<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>>();
        List<Entity> orders = entity.getHasManyField("orders");
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity technology = (Entity) order.getField("technology");
            if (technology != null) {
                EntityTree operationComponents = technology.getTreeField("operationComponents");
                if (COMPONENT_QUANTITY_ALGORITHM.equals(technology.getField("componentQuantityAlgorithm"))) {
                    aggregateTreeDataPerTechnology(operationComponents, operations, type, order,
                            (BigDecimal) order.getField("plannedQuantity"));
                } else {
                    Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> orderOperations = new HashMap<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>>();
                    EntityTreeNode rootNode = operationComponents.getRoot();
                    if (rootNode != null) {
                        boolean success = aggregateTreeDataPerOutProducts(rootNode, orderOperations, type, order,
                                (BigDecimal) order.getField("plannedQuantity"));
                        if (success) {
                            concatenateOperationsList(operations, orderOperations);
                        }
                    }
                }
            }
        }
        return operations;
    }

    private void concatenateOperationsList(
            final Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations,
            final Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> orderOperations) {
        if (operations.size() == 0) {
            operations.putAll(orderOperations);
        } else {
            for (Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> entry : orderOperations
                    .entrySet()) {
                if (operations.containsKey(entry.getKey())) {
                    Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> products = operations
                            .get(entry.getKey());
                    products.putAll(entry.getValue());
                    operations.put(entry.getKey(), products);
                } else {
                    operations.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void aggregateTreeDataPerTechnology(final List<Entity> operationComponents,
            final Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations,
            final String type, final Entity order, final BigDecimal plannedQuantity) {
        Entity entityKey = null;
        if (type.equals("product")) {
            Entity product = (Entity) order.getField("product");
            entityKey = product;
        }

        for (Entity operationComponent : operationComponents) {
            if (OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
                Entity operation = (Entity) operationComponent.getField("operation");

                if (type.equals("machine")) {
                    Object machine = operation.getField("machine");
                    if (machine != null) {
                        entityKey = (Entity) machine;
                    } else {
                        entityKey = null;
                    }
                } else if (type.equals("worker")) {
                    Object worker = operation.getField("staff");
                    if (worker != null) {
                        entityKey = (Entity) worker;
                    } else {
                        entityKey = null;
                    }
                }
                Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> operationMap = null;
                if (operations.containsKey(entityKey)) {
                    operationMap = operations.get(entityKey);
                } else {
                    operationMap = new HashMap<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>();
                }
                Pair<Entity, Entity> pair = Pair.of(operationComponent, order);
                Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>> mapPair = Pair.of(
                        prepareInProductsPerTechnology(operationComponent, plannedQuantity),
                        prepareOutProductsPerTechnology(operationComponent, plannedQuantity));
                operationMap.put(pair, mapPair);
                operations.put(entityKey, operationMap);
            } else {
                aggregateTreeDataPerTechnology(
                        operationComponent.getBelongsToField("referenceTechnology").getTreeField("operationComponents"),
                        operations, type, order, plannedQuantity);
            }
        }
    }

    private Map<Entity, BigDecimal> prepareInProductsPerTechnology(final Entity operationComponent,
            final BigDecimal plannedQuantity) {
        List<Entity> operationProductInComponents = operationComponent.getHasManyField("operationProductInComponents");
        Map<Entity, BigDecimal> productsInMap = new HashMap<Entity, BigDecimal>();
        for (Entity operationProductInComponent : operationProductInComponents) {
            Entity product = (Entity) operationProductInComponent.getField("product");
            BigDecimal quantity = ((BigDecimal) operationProductInComponent.getField("quantity")).multiply(plannedQuantity,
                    MathContext.DECIMAL128);
            productsInMap.put(product, quantity);
        }
        return productsInMap;
    }

    private Map<Entity, BigDecimal> prepareOutProductsPerTechnology(final Entity operationComponent,
            final BigDecimal plannedQuantity) {
        List<Entity> operationProductOutComponents = operationComponent.getHasManyField("operationProductOutComponents");
        Map<Entity, BigDecimal> productsOutMap = new HashMap<Entity, BigDecimal>();
        for (Entity operationProductOutComponent : operationProductOutComponents) {
            Entity product = (Entity) operationProductOutComponent.getField("product");
            if (!MATERIAL_WASTE.equals(product.getField("typeOfMaterial"))) {
                BigDecimal quantity = ((BigDecimal) operationProductOutComponent.getField("quantity")).multiply(plannedQuantity,
                        MathContext.DECIMAL128);
                productsOutMap.put(product, quantity);
            }
        }
        return productsOutMap;
    }

    private boolean aggregateTreeDataPerOutProducts(final EntityTreeNode node,
            final Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations,
            final String type, final Entity order, final BigDecimal plannedQuantity) {
        if (OPERATION_NODE_ENTITY_TYPE.equals(node.getField("entityType"))) {
            Entity entityKey = null;
            Entity operation = (Entity) node.getField("operation");
            List<Entity> operationProductInComponents = node.getHasManyField("operationProductInComponents");
            if (operationProductInComponents.size() == 0) {
                return false;
            }
            Entity productOutComponent = checkOutProducts(node);
            if (productOutComponent == null) {
                return false;
            }

            if (type.equals("product")) {
                Entity product = (Entity) order.getField("product");
                entityKey = product;
            } else if (type.equals("machine")) {
                Object machine = operation.getField("machine");
                if (machine != null) {
                    entityKey = (Entity) machine;
                } else {
                    entityKey = null;
                }
            } else if (type.equals("worker")) {
                Object worker = operation.getField("staff");
                if (worker != null) {
                    entityKey = (Entity) worker;
                } else {
                    entityKey = null;
                }
            }
            Map<Entity, BigDecimal> productsInMap = new HashMap<Entity, BigDecimal>();
            for (Entity operationProductInComponent : operationProductInComponents) {
                Entity product = (Entity) operationProductInComponent.getField("product");
                BigDecimal quantity = ((BigDecimal) operationProductInComponent.getField("quantity")).multiply(plannedQuantity,
                        MathContext.DECIMAL128).divide((BigDecimal) productOutComponent.getField("quantity"),
                        MathContext.DECIMAL128);
                EntityTreeNode prevOperation = findPreviousOperation(node, product);
                if (prevOperation != null) {
                    boolean success = aggregateTreeDataPerOutProducts(prevOperation, operations, type, order, quantity);
                    if (!success) {
                        return false;
                    }
                }
                productsInMap.put(product, quantity);
            }
            Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> operationMap = null;
            if (operations.containsKey(entityKey)) {
                operationMap = operations.get(entityKey);
            } else {
                operationMap = new HashMap<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>();
            }
            Pair<Entity, Entity> pair = Pair.of((Entity) node, order);
            Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>> mapPair = Pair.of(productsInMap,
                    prepareOutProducts(node, plannedQuantity));
            operationMap.put(pair, mapPair);
            operations.put(entityKey, operationMap);
        } else {
            EntityTreeNode rootNode = node.getBelongsToField("referenceTechnology").getTreeField("operationComponents").getRoot();
            if (rootNode != null) {
                boolean success = aggregateTreeDataPerOutProducts(rootNode, operations, type, order, plannedQuantity);
                if (!success) {
                    return false;
                }
            }
        }
        return true;
    }

    private Map<Entity, BigDecimal> prepareOutProducts(final EntityTreeNode node, final BigDecimal plannedQuantity) {
        Map<Entity, BigDecimal> productsOutMap = new HashMap<Entity, BigDecimal>();
        List<Entity> operationProductOutComponents = node.getHasManyField("operationProductOutComponents");
        for (Entity operationProductOutComponent : operationProductOutComponents) {
            Entity product = (Entity) operationProductOutComponent.getField("product");
            if (!MATERIAL_WASTE.equals(product.getField("typeOfMaterial"))) {
                productsOutMap.put(product, plannedQuantity);
            }
        }
        return productsOutMap;
    }

    private void addOperationSeries(
            final Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> entry,
            final Document document, final DecimalFormat decimalFormat, final Locale locale) throws DocumentException {
        PdfPTable table = PdfUtil.createTableWithHeader(5, prepareOperationHeader(locale), false,
                defaultWorkPlanOperationColumnWidth);

        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

        Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> operationMap = SortUtil
                .sortMapUsingComparator(entry.getValue(), new EntityOperationInPairNumberComparator());

        for (Entry<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> entryComponent : operationMap
                .entrySet()) {

            Pair<Entity, Entity> entryPair = entryComponent.getKey();
            Entity operation = (Entity) entryPair.getKey().getField("operation");
            table.addCell(new Phrase(operation.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(operation.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(entryPair.getValue().getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            addProductSeries(table, entryComponent.getValue().getValue(), decimalFormat);
            addProductSeries(table, entryComponent.getValue().getKey(), decimalFormat);
        }
        document.add(table);
    }

    private Paragraph prepareTitle(final BigDecimal totalQuantity,
            final Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> entry,
            final Locale locale, final String type) {
        Paragraph title = new Paragraph();
        if (type.equals("machine")) {
            Entity machine = entry.getKey();
            title.add(new Phrase(translationService.translate("products.workPlan.report.paragrah3", locale), PdfUtil
                    .getArialBold11Light()));
            String name = "";
            if (machine != null) {
                name = machine.getField("name").toString();
            }
            title.add(new Phrase(" " + name, PdfUtil.getArialBold19Dark()));
        } else if (type.equals("worker")) {
            Entity staff = entry.getKey();
            title.add(new Phrase(translationService.translate("products.workPlan.report.paragrah2", locale), PdfUtil
                    .getArialBold11Light()));
            String name = "";
            if (staff != null) {
                name = staff.getField("name") + " " + staff.getField("surname");
            }
            title.add(new Phrase(" " + name, PdfUtil.getArialBold19Dark()));
        } else if (type.equals("product")) {
            Entity product = entry.getKey();
            title.add(new Phrase(translationService.translate("products.workPlan.report.paragrah4", locale), PdfUtil
                    .getArialBold11Light()));
            title.add(new Phrase(" " + totalQuantity + " x " + product.getField("name"), PdfUtil.getArialBold19Dark()));
        }
        return title;
    }

    private BigDecimal createUniqueOrdersList(final List<Entity> orders,
            final Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> entry) {
        BigDecimal totalQuantity = BigDecimal.ZERO;

        for (Pair<Entity, Entity> pair : entry.getValue().keySet()) {
            if (!orders.contains(pair.getValue())) {
                totalQuantity = totalQuantity.add((BigDecimal) pair.getValue().getField("plannedQuantity"));
                orders.add(pair.getValue());
            }
        }
        return totalQuantity;
    }

    private void addProductSeries(final PdfPTable table, final Map<Entity, BigDecimal> productsQuantity, final DecimalFormat df) {
        StringBuilder products = new StringBuilder();
        for (Entry<Entity, BigDecimal> entry : productsQuantity.entrySet()) {
            products.append(entry.getKey().getField("number").toString() + " " + entry.getKey().getField("name").toString()
                    + " x " + df.format(entry.getValue()) + " ["
                    + (entry.getKey().getField("unit") != null ? entry.getKey().getField("unit").toString() : "") + "] \n\n");

        }
        table.addCell(new Phrase(products.toString(), PdfUtil.getArialRegular9Dark()));
    }

    /*
     * @SuppressWarnings({ "unused" }) private Image generateBarcode(final String code) throws BadElementException { Code128Bean
     * codeBean = new Code128Bean(); final int dpi = 150; codeBean.setModuleWidth(UnitConv.in2mm(1.0f / dpi));
     * codeBean.doQuietZone(false); codeBean.setHeight(8); codeBean.setFontSize(0.0); ByteArrayOutputStream out = new
     * ByteArrayOutputStream(); try { BitmapCanvasProvider canvas = new BitmapCanvasProvider(out, "image/x-png", dpi,
     * BufferedImage.TYPE_BYTE_BINARY, false, 0); codeBean.generateBarcode(canvas, code); canvas.finish(); } catch (IOException e)
     * { LOG.error(e.getMessage(), e); } finally { try { out.close(); } catch (IOException e) { LOG.error(e.getMessage(), e); } }
     * try { Image image = Image.getInstance(out.toByteArray()); image.setAlignment(Image.RIGHT); return image; } catch
     * (MalformedURLException e) { LOG.error(e.getMessage(), e); } catch (IOException e) { LOG.error(e.getMessage(), e); } return
     * null; }
     */
    private void addOrderSeries(final PdfPTable table, final List<Entity> orders, final DecimalFormat df)
            throws DocumentException {
        Collections.sort(orders, new EntityNumberComparator());
        for (Entity order : orders) {
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

    private List<String> prepareOrderHeader(final Document document, final Entity entity, final Locale locale)
            throws DocumentException {
        String documenTitle = translationService.translate("products.workPlan.report.title", locale);
        String documentAuthor = translationService.translate("products.materialRequirement.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, entity.getField("name").toString(), documenTitle, documentAuthor,
                (Date) entity.getField("date"), user);
        // document.add(generateBarcode(entity.getField("name").toString()));
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

    private List<String> prepareOperationHeader(final Locale locale) {
        List<String> operationHeader = new ArrayList<String>();
        operationHeader.add(translationService.translate("products.operation.number.label", locale));
        operationHeader.add(translationService.translate("products.operation.name.label", locale));
        operationHeader.add(translationService.translate("products.workPlan.report.operationTable.order.column", locale));
        operationHeader.add(translationService.translate("products.workPlan.report.operationTable.productsOut.column", locale));
        operationHeader.add(translationService.translate("products.workPlan.report.operationTable.productsIn.column", locale));
        return operationHeader;
    }

}
