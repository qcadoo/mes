/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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

import java.text.DecimalFormat;
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
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.PrioritizedString;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.security.api.SecurityService;

@Service
public class WorkPlanPdfService extends PdfDocumentService {

    private static final String OPERATION_LITERAL = "operation";

    private static final String L_NAME = "name";

    private static final String NUMBER_LITERAL = "number";

    private static final String PRODUCT_LITERAL = "product";

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private ColumnFetcher columnFetcher;

    enum ProductDirection {
        IN, OUT;
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("workPlans.workPlan.report.title", locale);
    }

    @Override
    public void buildPdfContent(final Document document, final Entity workPlan, final Locale locale) throws DocumentException {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
        decimalFormat.setMaximumFractionDigits(3);
        decimalFormat.setMinimumFractionDigits(3);

        addMainHeader(document, workPlan, locale);
        if (!workPlan.getBooleanField("dontPrintOrdersInWorkPlans")) {
            addOrdersTable(document, workPlan, locale, decimalFormat);
        }
        addOperations(document, workPlan, decimalFormat, locale);
    }

    private void addOrdersTable(final Document document, final Entity workPlan, final Locale locale,
            final DecimalFormat decimalFormat) throws DocumentException {
        List<Entity> columns = fetchOrderColumnDefinitions(workPlan);

        if (!columns.isEmpty()) {
            PdfPTable orderTable = pdfHelper.createTableWithHeader(columns.size(),
                    prepareOrdersTableHeader(document, columns, locale), false);

            List<Entity> orders = workPlan.getManyToManyField("orders");

            Map<Entity, Map<String, String>> columnValues = columnFetcher.getOrderColumnValues(orders);

            for (Entity order : orders) {
                for (Entity column : columns) {
                    String columnIdentifier = column.getStringField("identifier");
                    String value = columnValues.get(order).get(columnIdentifier);
                    orderTable.addCell(new Phrase(value, FontUtils.getDejavuRegular9Dark()));
                }
            }

            document.add(orderTable);
            document.add(Chunk.NEWLINE);
        }
    }

    void addOperations(final Document document, final Entity workPlan, final DecimalFormat df, final Locale locale)
            throws DocumentException {
        Map<Entity, Entity> operationComponent2order = new HashMap<Entity, Entity>();

        List<Entity> orders = workPlan.getManyToManyField("orders");

        Map<Entity, Map<String, String>> columnValues = columnFetcher.getColumnValues(orders);

        for (Entry<PrioritizedString, List<Entity>> entry : getOperationComponentsWithDistinction(workPlan,
                operationComponent2order, locale).entrySet()) {
            document.newPage();

            document.add(new Paragraph(entry.getKey().getString(), FontUtils.getDejavuBold11Dark()));

            for (Entity operationComponent : entry.getValue()) {
                PdfPTable operationTable = pdfHelper.createPanelTable(3);
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
                    addOutProductsSeries(document, columnValues, operationComponent, df, locale);
                }

                if (isInputProductTableEnabled(operationComponent)) {
                    addInProductsSeries(document, columnValues, operationComponent, df, locale);
                }

                addAdditionalFields(document, operationComponent, locale);
            }
        }
    }

    void addOperationComment(final Document document, final Entity operationComponent, final Locale locale)
            throws DocumentException {
        PdfPTable table = pdfHelper.createPanelTable(1);
        table.getDefaultCell().setBackgroundColor(null);

        String commentLabel = translationService.translate("workPlans.workPlan.report.operation.comment", locale);
        String commentContent = operationComponent.getBelongsToField(OPERATION_LITERAL).getStringField("comment");

        if (commentContent == null) {
            return;
        }

        pdfHelper.addTableCellAsOneColumnTable(table, commentLabel, commentContent);

        table.setSpacingAfter(18);
        table.setSpacingBefore(9);
        document.add(table);
    }

    void addOperationInfoToTheOperationHeader(final PdfPTable operationTable, final Entity operationComponent, final Locale locale) {
        String operationLevel = operationComponent.getStringField("nodeNumber");
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.level", locale), operationLevel);

        String operationName = operationComponent.getBelongsToField(OPERATION_LITERAL).getStringField(L_NAME);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.name", locale), operationName);

        String operationNumber = operationComponent.getBelongsToField(OPERATION_LITERAL).getStringField(NUMBER_LITERAL);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.number", locale), operationNumber);
    }

    void addWorkstationInfoToTheOperationHeader(final PdfPTable operationTable, final Entity operationComponent,
            final Locale locale) {
        Entity workstationType = operationComponent.getBelongsToField(OPERATION_LITERAL).getBelongsToField("workstationType");
        String workstationTypeName = "";
        String divisionName = "";
        String supervisorName = "";
        String divisionLabel = "";
        String supervisorLabel = "";

        if (workstationType != null) {
            workstationTypeName = workstationType.getStringField(L_NAME);

            Entity division = workstationType.getBelongsToField("division");
            if (division != null) {
                divisionName = division.getStringField(L_NAME);
                divisionLabel = translationService.translate("workPlans.workPlan.report.operation.division", locale);
                Entity supervisor = division.getBelongsToField("supervisor");
                if (supervisor != null) {
                    supervisorName = supervisor.getStringField(L_NAME) + " " + supervisor.getStringField("surname");
                    supervisorLabel = translationService.translate("workPlans.workPlan.report.operation.supervisor", locale);
                }
            }

            pdfHelper.addTableCellAsOneColumnTable(operationTable,
                    translationService.translate("workPlans.workPlan.report.operation.workstationType", locale),
                    workstationTypeName);
            pdfHelper.addTableCellAsOneColumnTable(operationTable, divisionLabel, divisionName);
            pdfHelper.addTableCellAsOneColumnTable(operationTable, supervisorLabel, supervisorName);
        }
    }

    void addOrderInfoToTheOperationHeader(final PdfPTable operationTable, final Entity order, final Locale locale) {
        Entity technology = order.getBelongsToField("technology");
        String technologyString = null;
        if (technology != null) {
            technologyString = technology.getStringField(L_NAME);
        }
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.technology", locale), technologyString);

        String orderName = order.getStringField(L_NAME);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.orderName", locale), orderName);

        String orderNumber = order.getStringField(NUMBER_LITERAL);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.orderNumber", locale), orderNumber);
    }

    PrioritizedString generateOperationSectionTitle(final Entity workPlan, final Entity technology,
            final Entity operationComponent, final Locale locale) {
        String type = workPlan.getStringField("type");

        PrioritizedString title = null;

        if (WorkPlanType.NO_DISTINCTION.getStringValue().equals(type)) {
            title = new PrioritizedString(translationService.translate("workPlans.workPlan.report.title.noDistinction", locale));
        } else if (WorkPlanType.BY_END_PRODUCT.getStringValue().equals(type)) {
            Entity endProduct = technology.getBelongsToField(PRODUCT_LITERAL);

            String prefix = translationService.translate("workPlans.workPlan.report.title.byEndProduct", locale);
            String endProductName = endProduct.getStringField(L_NAME);
            title = new PrioritizedString(prefix + " " + endProductName);
        } else if (WorkPlanType.BY_WORKSTATION_TYPE.getStringValue().equals(type)) {
            Entity workstation = operationComponent.getBelongsToField(OPERATION_LITERAL).getBelongsToField("workstationType");

            if (workstation == null) {
                title = new PrioritizedString(translationService.translate("workPlans.workPlan.report.title.noWorkstationType",
                        locale), 1);
            } else {
                String suffix = translationService.translate("workPlans.workPlan.report.title.byWorkstationType", locale);
                String workstationName = workstation.getStringField(L_NAME);
                title = new PrioritizedString(suffix + " " + workstationName);
            }
        } else if (WorkPlanType.BY_DIVISION.getStringValue().equals(type)) {
            Entity workstation = operationComponent.getBelongsToField(OPERATION_LITERAL).getBelongsToField("workstationType");

            if (workstation == null) {
                title = new PrioritizedString(translationService.translate("workPlans.workPlan.report.title.noDivision", locale),
                        1);
            } else {
                Entity division = workstation.getBelongsToField("division");

                if (division == null) {
                    title = new PrioritizedString(translationService.translate("workPlans.workPlan.report.title.noDivision",
                            locale), 1);
                } else {
                    String suffix = translationService.translate("workPlans.workPlan.report.title.byDivision", locale);
                    String divisionName = division.getStringField(L_NAME);
                    title = new PrioritizedString(suffix + " " + divisionName);
                }
            }
        }

        return title;
    }

    private void fetchOperationComponentsFromTechnology(final Entity technology, final Entity workPlan, final Entity order,
            final Locale locale, final Map<Entity, Entity> opComps2Order, final Map<PrioritizedString, List<Entity>> opComps) {
        EntityTree operationComponents = technology.getTreeField("operationComponents");

        for (Entity operationComponent : operationComponents) {
            if ("referenceTechnology".equals(operationComponent.getStringField("entityType"))) {
                Entity refTech = operationComponent.getBelongsToField("referenceTechnology");
                fetchOperationComponentsFromTechnology(refTech, workPlan, order, locale, opComps2Order, opComps);
                continue;
            }

            PrioritizedString title = generateOperationSectionTitle(workPlan, technology, operationComponent, locale);

            if (title == null) {
                throw new IllegalStateException("undefined workplan type");
            }

            if (!opComps.containsKey(title)) {
                opComps.put(title, new ArrayList<Entity>());
            }

            opComps2Order.put(operationComponent, order);
            opComps.get(title).add(operationComponent);
        }
    }

    Map<PrioritizedString, List<Entity>> getOperationComponentsWithDistinction(final Entity workPlan,
            final Map<Entity, Entity> operationComponent2order, final Locale locale) {
        Map<PrioritizedString, List<Entity>> operationComponentsWithDistinction = new TreeMap<PrioritizedString, List<Entity>>();

        List<Entity> orders = workPlan.getManyToManyField("orders");

        for (Entity order : orders) {
            Entity technology = order.getBelongsToField("technology");
            if (technology == null) {
                continue;
            }

            fetchOperationComponentsFromTechnology(technology, workPlan, order, locale, operationComponent2order,
                    operationComponentsWithDistinction);
        }

        for (List<Entity> operationComponents : operationComponentsWithDistinction.values()) {
            Collections.sort(operationComponents, new Comparator<Entity>() {

                @Override
                public int compare(final Entity o1, final Entity o2) {
                    String o1comp = operationComponent2order.get(o1).getStringField(NUMBER_LITERAL)
                            + o1.getStringField("nodeNumber");
                    String o2comp = operationComponent2order.get(o2).getStringField(NUMBER_LITERAL)
                            + o2.getStringField("nodeNumber");

                    return o1comp.compareTo(o2comp);
                }

            });
        }

        return operationComponentsWithDistinction;
    }

    private static final class OperationProductComponentComparator implements Comparator<Entity> {

        @Override
        public int compare(final Entity o0, final Entity o1) {
            Entity prod0 = o0.getBelongsToField(PRODUCT_LITERAL);
            Entity prod1 = o1.getBelongsToField(PRODUCT_LITERAL);
            return prod0.getStringField(NUMBER_LITERAL).compareTo(prod1.getStringField(NUMBER_LITERAL));
        }

    }

    private static final class ColumnSuccessionComparator implements Comparator<Entity> {

        @Override
        public int compare(final Entity o1, final Entity o2) {
            Integer o1succession = (Integer) o1.getField("succession");
            Integer o2succession = (Integer) o2.getField("succession");
            return o1succession.compareTo(o2succession);
        }

    }

    void addProductsSeries(final List<Entity> productComponentsArg, final Document document,
            final Map<Entity, Map<String, String>> columnValues, final Entity operationComponent, final DecimalFormat df,
            final ProductDirection direction, final Locale locale) throws DocumentException {
        if (productComponentsArg.isEmpty()) {
            return;
        }

        // TODO mici, I couldnt sort productComponents without making a new linkedList out of it
        List<Entity> productComponents = Lists.newLinkedList(productComponentsArg);
        Collections.sort(productComponents, new OperationProductComponentComparator());

        List<Entity> columns = fetchColumnDefinitions(direction, operationComponent);

        if (columns.isEmpty()) {
            return;
        }

        PdfPTable table = pdfHelper.createTableWithHeader(columns.size(),
                prepareProductsTableHeader(document, columns, direction, locale), false);

        for (Entity productComponent : productComponents) {
            for (Entity column : columns) {
                String columnIdentifier = column.getStringField("identifier");
                String value = columnValues.get(productComponent).get(columnIdentifier);
                table.addCell(new Phrase(value, FontUtils.getDejavuRegular9Dark()));
            }
        }

        table.setSpacingAfter(18);
        table.setSpacingBefore(9);

        document.add(table);
    }

    void addInProductsSeries(final Document document, final Map<Entity, Map<String, String>> columnValues,
            final Entity operationComponent, final DecimalFormat df, final Locale locale) throws DocumentException {

        List<Entity> productComponents = operationComponent.getHasManyField("operationProductInComponents");

        addProductsSeries(productComponents, document, columnValues, operationComponent, df, ProductDirection.IN, locale);
    }

    void addOutProductsSeries(final Document document, final Map<Entity, Map<String, String>> columnValues,
            final Entity operationComponent, final DecimalFormat df, final Locale locale) throws DocumentException {

        List<Entity> productComponents = operationComponent.getHasManyField("operationProductOutComponents");

        addProductsSeries(productComponents, document, columnValues, operationComponent, df, ProductDirection.OUT, locale);
    }

    void addAdditionalFields(final Document document, final Entity operationComponent, final Locale locale)
            throws DocumentException {
        String imagePath;
        try {
            imagePath = getImagePathFromDD(operationComponent);
        } catch (NoSuchElementException e) {
            return;
        }

        String titleString = translationService.translate("workPlans.workPlan.report.additionalFields", locale);
        document.add(new Paragraph(titleString, FontUtils.getDejavuBold10Dark()));

        pdfHelper.addImage(document, imagePath);
    }

    void addMainHeader(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documenTitle = translationService.translate("workPlans.workPlan.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeader(document, entity.getField(L_NAME).toString(), documenTitle, documentAuthor,
                (Date) entity.getField("date"), securityService.getCurrentUserName());
    }

    private List<Entity> fetchOrderColumnDefinitions(final Entity workPlan) {
        List<Entity> columns = new LinkedList<Entity>();

        List<Entity> columnComponents = workPlan.getHasManyField("workPlanOrderColumns");

        // TODO mici, I couldnt sort, without making a new linkedList out of it
        columnComponents = Lists.newLinkedList(columnComponents);

        Collections.sort(columnComponents, new ColumnSuccessionComparator());

        for (Entity columnComponent : columnComponents) {
            Entity columnDefinition = columnComponent.getBelongsToField("columnForOrders");

            columns.add(columnDefinition);
        }

        return columns;
    }

    private List<Entity> fetchColumnDefinitions(final ProductDirection direction, final Entity operationComponent) {
        List<Entity> columns = new LinkedList<Entity>();

        final String columnDefinitionModel;

        List<Entity> columnComponents;
        if (ProductDirection.IN.equals(direction)) {
            columnComponents = operationComponent.getHasManyField("technologyOperationInputColumns");
            columnDefinitionModel = "columnForInputProducts";
        } else if (ProductDirection.OUT.equals(direction)) {
            columnComponents = operationComponent.getHasManyField("technologyOperationOutputColumns");
            columnDefinitionModel = "columnForOutputProducts";
        } else {
            throw new IllegalStateException("Wrong product direction");
        }

        // TODO mici, I couldnt sort productComponents without making a new linkedList out of it
        columnComponents = Lists.newLinkedList(columnComponents);

        Collections.sort(columnComponents, new ColumnSuccessionComparator());

        for (Entity columnComponent : columnComponents) {
            Entity columnDefinition = columnComponent.getBelongsToField(columnDefinitionModel);

            columns.add(columnDefinition);
        }

        return columns;
    }

    List<String> prepareProductsTableHeader(final Document document, final List<Entity> columns,
            final ProductDirection direction, final Locale locale) throws DocumentException {
        String title;
        if (ProductDirection.IN.equals(direction)) {
            title = translationService.translate("workPlans.workPlan.report.productsInTable", locale);
        } else if (ProductDirection.OUT.equals(direction)) {
            title = translationService.translate("workPlans.workPlan.report.productsOutTable", locale);
        } else {
            throw new IllegalStateException("unknown product direction");
        }

        document.add(new Paragraph(title, FontUtils.getDejavuBold10Dark()));

        List<String> header = new ArrayList<String>();

        for (Entity column : columns) {
            String nameKey = column.getStringField(L_NAME);
            header.add(translationService.translate(nameKey, locale));
        }

        return header;
    }

    List<String> prepareOrdersTableHeader(final Document document, final List<Entity> columns, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(translationService.translate("workPlans.workPlan.report.ordersTable", locale), FontUtils
                .getDejavuBold11Dark()));

        List<String> orderHeader = new ArrayList<String>();

        for (Entity column : columns) {
            String nameKey = column.getStringField(L_NAME);
            orderHeader.add(translationService.translate(nameKey, locale));
        }

        return orderHeader;
    }

    String getImagePathFromDD(final Entity operationComponent) {
        String imagePath = operationComponent.getStringField("imageUrlInWorkPlan");

        if (imagePath == null) {
            throw new NoSuchElementException("no image");
        } else {
            return imagePath;
        }
    }

    public boolean isCommentEnabled(final Entity operationComponent) {
        return !operationComponent.getBooleanField("hideDescriptionInWorkPlans");
    }

    public boolean isOrderInfoEnabled(final Entity operationComponent) {
        return !operationComponent.getBooleanField("hideTechnologyAndOrderInWorkPlans");
    }

    public boolean isWorkstationInfoEnabled(final Entity operationComponent) {
        return !operationComponent.getBooleanField("hideDetailsInWorkPlans");
    }

    public boolean isInputProductTableEnabled(final Entity operationComponent) {
        return !operationComponent.getBooleanField("dontPrintInputProductsInWorkPlans");
    }

    public boolean isOutputProductTableEnabled(final Entity operationComponent) {
        return !operationComponent.getBooleanField("dontPrintOutputProductsInWorkPlans");
    }
}
