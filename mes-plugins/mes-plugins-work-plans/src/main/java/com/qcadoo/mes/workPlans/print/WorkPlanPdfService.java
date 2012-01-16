/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.workPlans.print;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.report.api.PrioritizedString;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

@Service
public class WorkPlanPdfService extends PdfDocumentService {

    private static Locale currentLocale = LocaleContextHolder.getLocale();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DateUtils.DATE_FORMAT, currentLocale);

    private final int[] defaultWorkPlanColumnWidth = new int[] { 15, 25, 30, 15, 15 };

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private WorkPlanProductsService workPlanProductsService;

    enum ProductDirection {
        IN, OUT;
    }

    // TODO mici purely temporary solution until we make proper function calls from other plugins.
    enum Column {
        PRODUCT, QUANTITY;
    }

    @Override
    public String getReportTitle(Locale locale) {
        return getTranslationService().translate("workPlans.workPlan.report.title", locale);
    }

    @Override
    public void buildPdfContent(Document document, Entity workPlan, Locale locale) throws DocumentException {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
        decimalFormat.setMaximumFractionDigits(3);
        decimalFormat.setMinimumFractionDigits(3);

        addMainHeader(document, workPlan, locale);

        PdfPTable orderTable = PdfUtil.createTableWithHeader(5, prepareOrdersTableHeader(document, workPlan, locale), false,
                defaultWorkPlanColumnWidth);

        List<Entity> orders = workPlan.getManyToManyField("orders");
        addOrderSeries(orderTable, orders, decimalFormat);
        document.add(orderTable);
        document.add(Chunk.NEWLINE);

        addOperations(document, workPlan, decimalFormat, locale);
    }

    void addOperations(Document document, Entity workPlan, final DecimalFormat df, Locale locale) throws DocumentException {
        Map<Entity, Entity> operationComponent2order = new HashMap<Entity, Entity>();

        List<Entity> orders = workPlan.getManyToManyField("orders");

        Map<Entity, Map<Entity, BigDecimal>> productQuantitiesPerOperation = workPlanProductsService.getProductQuantities(orders);

        for (Entry<PrioritizedString, List<Entity>> entry : getOperationComponentsWithDistinction(workPlan,
                operationComponent2order, locale).entrySet()) {
            document.newPage();

            document.add(new Paragraph(entry.getKey().getString(), PdfUtil.getArialBold11Dark()));
            for (Entity operationComponent : entry.getValue()) {
                PdfPTable operationTable = PdfUtil.createPanelTable(3);
                addOperationInfoToTheOperationHeader(operationTable, operationComponent, locale);
                if (orders.size() > 1 && isOrderInfoEnabled(operationComponent)) {
                    addOrderInfoToTheOperationHeader(operationTable, operationComponent2order.get(operationComponent), locale);
                }

                if (isWorkstationInfoEnabled(operationComponent)) {
                    addWorkstationInfoToTheOperationHeader(operationTable, operationComponent, locale);
                }

                operationTable.setSpacingAfter(18);
                operationTable.setSpacingBefore(9);
                document.add(operationTable);

                if (isCommentEnabled(operationComponent)) {
                    addOperationComment(document, operationComponent, locale);
                }

                if (isOutputProductTableEnabled(operationComponent)) {
                    addOutProductsSeries(document, operationComponent, productQuantitiesPerOperation, df, locale);
                }

                if (isInputProductTableEnabled(operationComponent)) {
                    addInProductsSeries(document, operationComponent, productQuantitiesPerOperation, df, locale);
                }

                addAdditionalFields(document, operationComponent, locale);
            }
        }
    }

    void addOperationComment(final Document document, Entity operationComponent, Locale locale) throws DocumentException {
        PdfPTable table = PdfUtil.createPanelTable(1);
        table.getDefaultCell().setBackgroundColor(null);

        String commentLabel = getTranslationService().translate("workPlans.workPlan.report.operation.comment", locale);
        String commentContent = operationComponent.getBelongsToField("operation").getStringField("comment");

        if (commentContent == null) {
            return;
        }

        PdfUtil.addTableCellAsTable(table, commentLabel, commentContent, null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark());

        table.setSpacingAfter(18);
        table.setSpacingBefore(9);
        document.add(table);
    }

    void addOperationInfoToTheOperationHeader(PdfPTable operationTable, Entity operationComponent, Locale locale) {
        String operationLevel = operationComponent.getStringField("nodeNumber");
        PdfUtil.addTableCellAsTable(operationTable,
                getTranslationService().translate("workPlans.workPlan.report.operation.level", locale), operationLevel, null,
                PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());

        String operationName = operationComponent.getBelongsToField("operation").getStringField("name");
        PdfUtil.addTableCellAsTable(operationTable,
                getTranslationService().translate("workPlans.workPlan.report.operation.name", locale), operationName, null,
                PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());

        String operationNumber = operationComponent.getBelongsToField("operation").getStringField("number");
        PdfUtil.addTableCellAsTable(operationTable,
                getTranslationService().translate("workPlans.workPlan.report.operation.number", locale), operationNumber, null,
                PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
    }

    void addWorkstationInfoToTheOperationHeader(PdfPTable operationTable, Entity operationComponent, Locale locale) {
        Entity workstationType = operationComponent.getBelongsToField("operation").getBelongsToField("workstationType");
        String workstationTypeName = "-";
        String divisionName = "-";
        String supervisorName = "-";

        if (workstationType != null) {
            workstationTypeName = workstationType.getStringField("name");

            PdfUtil.addTableCellAsTable(operationTable,
                    getTranslationService().translate("workPlans.workPlan.report.operation.workstationType", locale),
                    workstationTypeName, null, PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());

            Entity division = workstationType.getBelongsToField("division");
            if (division != null) {
                divisionName = division.getStringField("name");

                PdfUtil.addTableCellAsTable(operationTable,
                        getTranslationService().translate("workPlans.workPlan.report.operation.division", locale), divisionName,
                        null, PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());

                Entity supervisor = division.getBelongsToField("supervisor");
                if (supervisor != null) {
                    supervisorName = supervisor.getStringField("string");

                    PdfUtil.addTableCellAsTable(operationTable,
                            getTranslationService().translate("workPlans.workPlan.report.operation.supervisor", locale),
                            supervisorName, null, PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
                }
            }
        }
    }

    void addOrderInfoToTheOperationHeader(PdfPTable operationTable, Entity order, Locale locale) {
        Entity technology = order.getBelongsToField("technology");
        String technologyString = null;
        if (technology != null) {
            technologyString = technology.getStringField("name");
        }
        PdfUtil.addTableCellAsTable(operationTable,
                getTranslationService().translate("workPlans.workPlan.report.operation.technology", locale), technologyString,
                null, PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());

        String orderName = order.getStringField("name");
        PdfUtil.addTableCellAsTable(operationTable,
                getTranslationService().translate("workPlans.workPlan.report.operation.orderName", locale), orderName, null,
                PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());

        String orderNumber = order.getStringField("number");
        PdfUtil.addTableCellAsTable(operationTable,
                getTranslationService().translate("workPlans.workPlan.report.operation.orderNumber", locale), orderNumber, null,
                PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
    }

    PrioritizedString generateOperationSectionTitle(Entity workPlan, Entity technology, Entity operationComponent, Locale locale) {
        String type = workPlan.getStringField("type");

        PrioritizedString title = null;

        if (WorkPlanType.NO_DISTINCTION.getStringValue().equals(type)) {
            title = new PrioritizedString(getTranslationService().translate("workPlans.workPlan.report.title.noDistinction",
                    locale));
        } else if (WorkPlanType.BY_END_PRODUCT.getStringValue().equals(type)) {
            Entity endProduct = technology.getBelongsToField("product");

            String prefix = getTranslationService().translate("workPlans.workPlan.report.title.byEndProduct", locale);
            String endProductName = endProduct.getStringField("name");
            title = new PrioritizedString(prefix + " " + endProductName);
        } else if (WorkPlanType.BY_WORKSTATION_TYPE.getStringValue().equals(type)) {
            Entity workstation = operationComponent.getBelongsToField("operation").getBelongsToField("workstationType");

            if (workstation == null) {
                title = new PrioritizedString(getTranslationService().translate(
                        "workPlans.workPlan.report.title.noWorkstationType", locale), 1);
            } else {
                String suffix = getTranslationService().translate("workPlans.workPlan.report.title.byWorkstationType", locale);
                String workstationName = workstation.getStringField("name");
                title = new PrioritizedString(suffix + " " + workstationName);
            }
        } else if (WorkPlanType.BY_DIVISION.getStringValue().equals(type)) {
            Entity workstation = operationComponent.getBelongsToField("operation").getBelongsToField("workstationType");

            if (workstation == null) {
                title = new PrioritizedString(getTranslationService().translate("workPlans.workPlan.report.title.noDivision",
                        locale), 1);
            } else {
                Entity division = workstation.getBelongsToField("division");

                if (division == null) {
                    title = new PrioritizedString(getTranslationService().translate("workPlans.workPlan.report.title.noDivision",
                            locale), 1);
                } else {
                    String suffix = getTranslationService().translate("workPlans.workPlan.report.title.byDivision", locale);
                    String divisionName = division.getStringField("name");
                    title = new PrioritizedString(suffix + " " + divisionName);
                }
            }
        }

        return title;
    }

    Map<PrioritizedString, List<Entity>> getOperationComponentsWithDistinction(Entity workPlan,
            final Map<Entity, Entity> operationComponent2order, Locale locale) {
        Map<PrioritizedString, List<Entity>> operationComponentsWithDistinction = new TreeMap<PrioritizedString, List<Entity>>();

        List<Entity> orders = workPlan.getManyToManyField("orders");

        for (Entity order : orders) {
            Entity technology = order.getBelongsToField("technology");
            if (technology == null) {
                continue;
            }
            EntityTree operationComponents = technology.getTreeField("operationComponents");

            for (Entity operationComponent : operationComponents) {
                PrioritizedString title = generateOperationSectionTitle(workPlan, technology, operationComponent, locale);

                if (title == null) {
                    throw new IllegalStateException("undefined workplan type");
                }

                if (!operationComponentsWithDistinction.containsKey(title)) {
                    operationComponentsWithDistinction.put(title, new ArrayList<Entity>());
                }

                operationComponent2order.put(operationComponent, order);
                operationComponentsWithDistinction.get(title).add(operationComponent);
            }
        }

        for (List<Entity> operationComponents : operationComponentsWithDistinction.values()) {
            Collections.sort(operationComponents, new Comparator<Entity>() {

                @Override
                public int compare(Entity o1, Entity o2) {
                    String o1comp = operationComponent2order.get(o1).getStringField("number") + o1.getStringField("nodeNumber");
                    String o2comp = operationComponent2order.get(o2).getStringField("number") + o2.getStringField("nodeNumber");

                    return o1comp.compareTo(o2comp);
                }

            });
        }

        return operationComponentsWithDistinction;
    }

    void addProductsSeries(List<Entity> productComponents, Document document, Entity operationComponent,
            Map<Entity, Map<Entity, BigDecimal>> productQuantitiesPerOperation, final DecimalFormat df,
            ProductDirection direction, Locale locale) throws DocumentException {
        if (productComponents.isEmpty()) {
            return;
        }

        // TODO mici, I couldnt sort productComponents without making a new linkedList out of it
        productComponents = Lists.newLinkedList(productComponents);

        Collections.sort(productComponents, new Comparator<Entity>() {

            @Override
            public int compare(Entity arg0, Entity arg1) {
                Entity prod0 = arg0.getBelongsToField("product");
                Entity prod1 = arg1.getBelongsToField("product");
                return prod0.getStringField("number").compareTo(prod1.getStringField("number"));
            }

        });

        List<String> columns = fetchColumnsDefinition(direction, operationComponent);

        if (columns.isEmpty()) {
            return;
        }

        // TODO mici, column widths got overlooked in analysis
        int[] columnWidths = new int[columns.size()];

        for (int i = 0; i < columnWidths.length; ++i) {
            columnWidths[i] = (int) (100.0f / columnWidths.length);
        }

        PdfPTable table = PdfUtil.createTableWithHeader(columns.size(),
                prepareProductsTableHeader(document, columns, direction, locale), false, columnWidths);

        for (Entity productComponent : productComponents) {
            // TODO mici, totally wrong way of doing this
            for (String columnNameKey : columns) {
                if (columnNameKey.endsWith(".productName")) {
                    Entity product = productComponent.getBelongsToField("product");

                    StringBuilder productString = new StringBuilder(product.getStringField("name"));
                    productString.append(" (");
                    productString.append(product.getStringField("number"));
                    productString.append(")");

                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    table.addCell(new Phrase(productString.toString(), PdfUtil.getArialRegular9Dark()));
                } else if (columnNameKey.endsWith(".plannedQuantity")) {
                    Entity product = productComponent.getBelongsToField("product");

                    BigDecimal quantity = productQuantitiesPerOperation.get(operationComponent).get(productComponent);

                    StringBuilder quantityString = new StringBuilder(df.format(quantity));

                    Object unit = product.getField("unit");
                    if (unit != null) {
                        quantityString.append(" ");
                        quantityString.append(unit.toString());
                    }

                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(new Phrase(quantityString.toString(), PdfUtil.getArialRegular9Dark()));
                }
            }
        }

        table.setSpacingAfter(18);
        table.setSpacingBefore(9);

        document.add(table);
    }

    void addInProductsSeries(Document document, Entity operationComponent,
            Map<Entity, Map<Entity, BigDecimal>> productQuantitiesPerOperation, final DecimalFormat df, Locale locale)
            throws DocumentException {

        List<Entity> productComponents = operationComponent.getHasManyField("operationProductInComponents");

        addProductsSeries(productComponents, document, operationComponent, productQuantitiesPerOperation, df,
                ProductDirection.IN, locale);
    }

    void addOutProductsSeries(Document document, Entity operationComponent,
            Map<Entity, Map<Entity, BigDecimal>> productQuantitiesPerOperation, final DecimalFormat df, Locale locale)
            throws DocumentException {

        List<Entity> productComponents = operationComponent.getHasManyField("operationProductOutComponents");

        addProductsSeries(productComponents, document, operationComponent, productQuantitiesPerOperation, df,
                ProductDirection.OUT, locale);
    }

    void addOrderSeries(final PdfPTable table, final List<Entity> orders, final DecimalFormat df) throws DocumentException {
        Collections.sort(orders, new EntityNumberComparator());
        for (Entity order : orders) {
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(order.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(new Phrase(order.getField("name").toString(), PdfUtil.getArialRegular9Dark()));

            String unitString = "";

            Entity product = (Entity) order.getField("product");
            if (product == null) {
                table.addCell(new Phrase("", PdfUtil.getArialRegular9Dark()));
            } else {
                String productString = product.getField("name").toString() + " (" + product.getField("number").toString() + ")";
                table.addCell(new Phrase(productString, PdfUtil.getArialRegular9Dark()));

                Object unit = product.getField("unit");
                if (unit != null) {
                    unitString = " " + unit.toString();
                }
            }

            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            plannedQuantity = (plannedQuantity == null) ? BigDecimal.ZERO : plannedQuantity;
            String quantityString = df.format(plannedQuantity) + unitString;
            table.addCell(new Phrase(quantityString, PdfUtil.getArialRegular9Dark()));
            String formattedDateTo = "-";
            if (order.getField("dateTo") != null) {
                synchronized (this) {
                    formattedDateTo = DATE_FORMAT.format((Date) order.getField("dateTo"));
                }
            }
            table.addCell(new Phrase(formattedDateTo, PdfUtil.getArialRegular9Dark()));
        }
    }

    void addAdditionalFields(final Document document, final Entity operationComponent, final Locale locale)
            throws DocumentException {
        String imagePath;
        try {
            imagePath = getImagePathFromDD(operationComponent);
        } catch (NoSuchElementException e) {
            return;
        }

        String titleString = getTranslationService().translate("workPlans.workPlan.report.additionalFields", locale);
        document.add(new Paragraph(titleString, PdfUtil.getArialBold10Dark()));

        PdfPTable table = PdfUtil.createPanelTable(1);
        table.getDefaultCell().setBackgroundColor(null);
        table.setTableEvent(null);

        PdfUtil.addImage(table, imagePath);

        table.setSpacingAfter(18);
        table.setSpacingBefore(9);
        document.add(table);
    }

    void addMainHeader(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documenTitle = getTranslationService().translate("workPlans.workPlan.report.title", locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, entity.getField("name").toString(), documenTitle, documentAuthor,
                (Date) entity.getField("date"), securityService.getCurrentUserName());
    }

    List<String> fetchColumnsDefinition(ProductDirection direction, Entity operationComponent) {
        List<String> columns = new LinkedList<String>();

        DataDefinition dd;

        // TODO mici, I still don't think that those two should be separated models
        final String columnDefinitionModel;

        if (ProductDirection.IN.equals(direction)) {
            dd = dataDefinitionService.get("workPlans", "parameterInputComponent");
            columnDefinitionModel = "columnForInputProducts";
        } else if (ProductDirection.OUT.equals(direction)) {
            dd = dataDefinitionService.get("workPlans", "parameterOutputComponent");
            columnDefinitionModel = "columnForOutputProducts";
        } else {
            throw new IllegalStateException("Wrong product direction");
        }

        List<Entity> columnComponents = dd.find().list().getEntities();

        Collections.sort(columnComponents, new Comparator<Entity>() {

            @Override
            public int compare(Entity o1, Entity o2) {
                Integer o1succession = (Integer) o1.getField("succession");
                Integer o2succession = (Integer) o2.getField("succession");
                return o1succession.compareTo(o2succession);
            }
        });

        for (Entity columnComponent : columnComponents) {
            Entity columnDefinition = columnComponent.getBelongsToField(columnDefinitionModel).getBelongsToField(
                    "columnDefinition");

            String columnNameKey = columnDefinition.getStringField("name");
            columns.add(columnNameKey);
        }

        return columns;
    }

    List<String> prepareProductsTableHeader(final Document document, List<String> columns, ProductDirection direction,
            final Locale locale) throws DocumentException {
        String title;
        if (ProductDirection.IN.equals(direction)) {
            title = getTranslationService().translate("workPlans.workPlan.report.productsInTable", locale);
        } else if (ProductDirection.OUT.equals(direction)) {
            title = getTranslationService().translate("workPlans.workPlan.report.productsOutTable", locale);
        } else {
            throw new IllegalStateException("unknown product direction");
        }

        document.add(new Paragraph(title, PdfUtil.getArialBold10Dark()));
        List<String> header = new ArrayList<String>();

        for (String columnNameKey : columns) {
            header.add(getTranslationService().translate(columnNameKey, locale));
        }

        return header;
    }

    List<String> prepareOrdersTableHeader(final Document document, final Entity entity, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(getTranslationService().translate("workPlans.workPlan.report.ordersTable", locale), PdfUtil
                .getArialBold11Dark()));
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(getTranslationService().translate("orders.order.number.label", locale));
        orderHeader.add(getTranslationService().translate("orders.order.name.label", locale));
        orderHeader.add(getTranslationService().translate("workPlans.workPlan.report.colums.product", locale));
        orderHeader.add(getTranslationService().translate("orders.order.plannedQuantity.label", locale));
        orderHeader.add(getTranslationService().translate("orders.order.dateTo.label", locale));
        return orderHeader;
    }

    String getImagePathFromDD(Entity operationComponent) {
        DataDefinition dd = dataDefinitionService.get("basic", "parameter");
        Entity parameters = dd.find().uniqueResult();

        String imagePath = parameters.getStringField("imageUrlInWorkPlan");

        if (imagePath == null) {
            throw new NoSuchElementException("no image");
        } else {
            return imagePath;
        }
    }

    public boolean isCommentEnabled(Entity operationComponent) {
        DataDefinition dd = dataDefinitionService.get("basic", "parameter");
        Entity parameters = dd.find().uniqueResult();

        boolean hideComment = (Boolean) parameters.getField("hideDescriptionInWorkPlans");

        return !hideComment;
    }

    public boolean isOrderInfoEnabled(Entity operationComponent) {
        DataDefinition dd = dataDefinitionService.get("basic", "parameter");
        Entity parameters = dd.find().uniqueResult();

        boolean hideOrderInfo = (Boolean) parameters.getField("hideTechnologyAndOrderInWorkPlans");

        return !hideOrderInfo;
    }

    public boolean isWorkstationInfoEnabled(Entity operationComponent) {
        DataDefinition dd = dataDefinitionService.get("basic", "parameter");
        Entity parameters = dd.find().uniqueResult();

        boolean hideWorkstationInfo = (Boolean) parameters.getField("hideDetailsInWorkPlans");

        return !hideWorkstationInfo;
    }

    public boolean isInputProductTableEnabled(Entity operationComponent) {
        DataDefinition dd = dataDefinitionService.get("basic", "parameter");
        Entity parameters = dd.find().uniqueResult();

        boolean hideInputProducts = (Boolean) parameters.getField("dontPrintInputProductsInWorkPlans");

        return !hideInputProducts;
    }

    public boolean isOutputProductTableEnabled(Entity operationComponent) {
        DataDefinition dd = dataDefinitionService.get("basic", "parameter");
        Entity parameters = dd.find().uniqueResult();

        boolean hideOutputProducts = (Boolean) parameters.getField("dontPrintOutputProductsInWorkPlans");

        return !hideOutputProducts;
    }
}
