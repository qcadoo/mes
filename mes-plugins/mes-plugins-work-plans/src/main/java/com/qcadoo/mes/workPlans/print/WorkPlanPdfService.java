/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.DivisionFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentEntityType;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.workPlans.constants.*;
import com.qcadoo.mes.workPlans.util.OperationProductComponentComparator;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.PrioritizedString;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;

@Service
public class WorkPlanPdfService extends PdfDocumentService {

    private static final String L_NAME = "name";

    private static final String L_ALIGNMENT = "alignment";

    private static final String L_IDENTIFIER = "identifier";

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ColumnFetcher columnFetcher;

    @Autowired
    private EntityTreeUtilsService entityTreeUtilsService;

    @Autowired
    private PdfHelper pdfHelper;

    enum ProductDirection {
        IN,
        OUT;
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("workPlans.workPlan.report.title", locale);
    }

    @Override
    public void buildPdfContent(final Document document, final Entity workPlan, final Locale locale) throws DocumentException {
        addMainHeader(document, workPlan, locale);

        if (!workPlan.getBooleanField(WorkPlanFields.DONT_PRINT_ORDERS_IN_WORK_PLANS)) {
            addOrdersTable(document, workPlan, locale);
        }

        addOperations(document, workPlan, locale);
    }

    void addMainHeader(final Document document, final Entity workPlan, final Locale locale) throws DocumentException {
        String documenTitle = translationService.translate("workPlans.workPlan.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, workPlan.getField(WorkPlanFields.NAME).toString(), documenTitle, documentAuthor,
                workPlan.getDateField(WorkPlanFields.DATE));
    }

    private void addOrdersTable(final Document document, final Entity workPlan, final Locale locale) throws DocumentException {
        List<Entity> columnsForOrders = getOrderColumnDefinitions(workPlan);

        if (!columnsForOrders.isEmpty()) {
            PdfPTable orderTable = pdfHelper
                    .createTableWithHeader(columnsForOrders.size(), prepareOrdersTableHeader(document, columnsForOrders, locale),
                            false);

            List<Entity> orders = workPlan.getManyToManyField(WorkPlanFields.ORDERS);

            Map<Entity, Map<String, String>> columnValues = columnFetcher.getOrderColumnValues(orders);

            for (Entity order : orders) {
                for (Entity columnForOrders : columnsForOrders) {
                    String columnIdentifier = columnForOrders.getStringField(ColumnForOrdersFields.IDENTIFIER);
                    String value = columnValues.get(order).get(columnIdentifier);

                    alignColumn(orderTable.getDefaultCell(),
                            ColumnAlignment.parseString(columnForOrders.getStringField(ColumnForOrdersFields.ALIGNMENT)));

                    orderTable.addCell(new Phrase(value, FontUtils.getDejavuRegular7Dark()));
                }
            }

            document.add(orderTable);
            document.add(Chunk.NEWLINE);
        }
    }

    private List<Entity> getOrderColumnDefinitions(final Entity workPlan) {
        List<Entity> columnsForOrders = new LinkedList<Entity>();

        List<Entity> workPlanOrderColumns = workPlan.getHasManyField(WorkPlanFields.WORK_PLAN_ORDER_COLUMNS).find()
                .addOrder(SearchOrders.asc(WorkPlanOrderColumnFields.SUCCESSION)).list().getEntities();

        for (Entity workPlanOrderColumn : workPlanOrderColumns) {
            Entity columnForOrders = workPlanOrderColumn.getBelongsToField(WorkPlanOrderColumnFields.COLUMN_FOR_ORDERS);

            columnsForOrders.add(columnForOrders);
        }

        return columnsForOrders;
    }

    List<String> prepareOrdersTableHeader(final Document document, final List<Entity> columnsForOrders, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(translationService.translate("workPlans.workPlan.report.ordersTable", locale),
                FontUtils.getDejavuBold11Dark()));

        List<String> orderHeader = Lists.newArrayList();

        for (Entity columnForOrders : columnsForOrders) {
            String nameKey = columnForOrders.getStringField(ColumnForOrdersFields.NAME);
            orderHeader.add(translationService.translate(nameKey, locale));
        }

        return orderHeader;
    }

    private void addOperations(final Document document, final Entity workPlan, final Locale locale) throws DocumentException {
        final List<Entity> orders = workPlan.getManyToManyField(WorkPlanFields.ORDERS);

        final boolean haveManyOrders = orders.size() > 1;

        final Map<Long, Entity> orderIdWithOrderMap = getOrderIdWIthOrderMap(orders);
        final Map<Long, Map<PrioritizedString, List<Entity>>> orderIdWithTitleAndOperationComponentMap =
                getOrderIdWithTitleAndOperationComponentsMap(
                workPlan, locale);
        final Map<Long, Map<Entity, Map<String, String>>> orderColumnValues = columnFetcher.getColumnValues(orders);

        for (final Entry<Long, Map<PrioritizedString, List<Entity>>> orderIdWithTitleAndOperationComponent :
                orderIdWithTitleAndOperationComponentMap
                .entrySet()) {
            final Long orderId = orderIdWithTitleAndOperationComponent.getKey();
            final Entity order = orderIdWithOrderMap.get(orderId);

            addOperationsForSpecifiedOrder(document, orderIdWithTitleAndOperationComponent.getValue(),
                    orderColumnValues.get(orderId), order, haveManyOrders, locale);
        }
    }

    private Map<Long, Entity> getOrderIdWIthOrderMap(final List<Entity> orders) {
        final Map<Long, Entity> orderIdWIthOrderMap = Maps.newHashMap();

        for (final Entity order : orders) {
            orderIdWIthOrderMap.put(order.getId(), order);
        }

        return orderIdWIthOrderMap;
    }

    Map<Long, Map<PrioritizedString, List<Entity>>> getOrderIdWithTitleAndOperationComponentsMap(final Entity workPlan,
            final Locale locale) {
        final Map<Long, Map<PrioritizedString, List<Entity>>> orderIdWithTitleAndOperationComponentMap = Maps.newTreeMap();

        List<Entity> orders = workPlan.getManyToManyField(WorkPlanFields.ORDERS);

        for (Entity order : orders) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            if (technology == null) {
                continue;
            }

            orderIdWithTitleAndOperationComponentMap
                    .put(order.getId(), getTitleWithOperationComponentMap(technology, workPlan, order, locale));
        }

        return orderIdWithTitleAndOperationComponentMap;
    }

    private Map<PrioritizedString, List<Entity>> getTitleWithOperationComponentMap(final Entity technology, final Entity workPlan,
            final Entity order, final Locale locale) {
        List<Entity> operationComponents = entityTreeUtilsService
                .getSortedEntities(technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS));

        final Map<PrioritizedString, List<Entity>> titleWithOperationComponentMap = Maps.newTreeMap();

        for (Entity operationComponent : operationComponents) {
            if (TechnologyOperationComponentEntityType.REFERENCE_TECHNOLOGY.getStringValue()
                    .equals(operationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE))) {
                Entity referenceTechnology = operationComponent
                        .getBelongsToField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY);

                titleWithOperationComponentMap
                        .putAll(getTitleWithOperationComponentMap(referenceTechnology, workPlan, order, locale));

                continue;
            }

            PrioritizedString title = resolveOperationSectionTitle(workPlan, technology, operationComponent, locale);

            if (title == null) {
                throw new IllegalStateException("undefined workplan type");
            }

            if (!titleWithOperationComponentMap.containsKey(title)) {
                titleWithOperationComponentMap.put(title, new ArrayList<Entity>());
            }

            titleWithOperationComponentMap.get(title).add(operationComponent);
        }

        return titleWithOperationComponentMap;
    }

    private PrioritizedString resolveOperationSectionTitle(final Entity workPlan, final Entity technology,
            final Entity operationComponent, final Locale locale) {
        String type = workPlan.getStringField(WorkPlanFields.TYPE);

        if (WorkPlanType.NO_DISTINCTION.getStringValue().equals(type)) {
            return new PrioritizedString(translationService.translate("workPlans.workPlan.report.title.noDistinction", locale));
        } else if (WorkPlanType.BY_END_PRODUCT.getStringValue().equals(type)) {
            return getOperationByProductSectionTitle(technology, locale);
        } else if (WorkPlanType.BY_WORKSTATION_TYPE.getStringValue().equals(type)) {
            return getOperationByWorkstationSectionTitle(operationComponent, locale);
        } else if (WorkPlanType.BY_DIVISION.getStringValue().equals(type)) {
            return getOperationByDivisionSectionTitle(operationComponent, locale);
        }
        // [maku] I think that returning some 'error value' will be better than throwing an exception,
        // since section title resolving isn't a critical functionality and whole report can be successfully generated without
        // them..
        return new PrioritizedString("###");
    }

    private PrioritizedString getOperationByProductSectionTitle(final Entity technology, final Locale locale) {
        PrioritizedString title;
        Entity endProduct = technology.getBelongsToField(TechnologyFields.PRODUCT);

        String prefix = translationService.translate("workPlans.workPlan.report.title.byEndProduct", locale);
        String endProductName = endProduct.getStringField(ProductFields.NAME);
        title = new PrioritizedString(prefix + " " + endProductName);
        return title;
    }

    private PrioritizedString getOperationByDivisionSectionTitle(final Entity operationComponent, final Locale locale) {
        Entity workstationType = extractWorkstationTypeFromToc(operationComponent);

        if (workstationType == null) {
            return new PrioritizedString(translationService.translate("workPlans.workPlan.report.title.noDivision", locale), 1);
        }
        // TODO KASI to change
        Entity division = null;
        // Entity division = workstationType.getBelongsToField(WorkstationTypeFields.DIVISION);

        if (division == null) {
            return new PrioritizedString(translationService.translate("workPlans.workPlan.report.title.noDivision", locale), 1);
        }
        String suffix = translationService.translate("workPlans.workPlan.report.title.byDivision", locale);
        String divisionName = division.getStringField(DivisionFields.NAME);
        return new PrioritizedString(suffix + " " + divisionName);
    }

    private PrioritizedString getOperationByWorkstationSectionTitle(final Entity operationComponent, final Locale locale) {
        Entity workstationType = extractWorkstationTypeFromToc(operationComponent);

        if (workstationType == null) {
            return new PrioritizedString(
                    translationService.translate("workPlans.workPlan.report.title.noWorkstationType", locale), 1);
        }
        String suffix = translationService.translate("workPlans.workPlan.report.title.byWorkstationType", locale);
        String workstationName = workstationType.getStringField(WorkstationTypeFields.NAME);
        return new PrioritizedString(suffix + " " + workstationName);
    }

    private void addOperationsForSpecifiedOrder(final Document document,
            final Map<PrioritizedString, List<Entity>> titleWithOperationComponentMap,
            final Map<Entity, Map<String, String>> columnValues, final Entity order, final boolean haveManyOrders,
            final Locale locale) throws DocumentException {
        for (Entry<PrioritizedString, List<Entity>> entry : titleWithOperationComponentMap.entrySet()) {
            document.newPage();

            document.add(new Paragraph(entry.getKey().getString(), FontUtils.getDejavuBold11Dark()));

            for (Entity operationComponent : entry.getValue()) {
                PdfPTable operationTable = pdfHelper.createPanelTable(3);
                addOperationInfoToTheOperationHeader(operationTable, operationComponent, locale);

                if (haveManyOrders && isOrderInfoEnabled(operationComponent)) {
                    addOrderInfoToTheOperationHeader(operationTable, order, locale);
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
                    addOutputProductsSeries(document, columnValues, operationComponent, locale);
                }

                if (isInputProductTableEnabled(operationComponent)) {
                    addInputProductsSeries(document, columnValues, operationComponent, locale);
                }

                addAdditionalFields(document, operationComponent, locale);
            }
        }
    }

    private void addOperationInfoToTheOperationHeader(final PdfPTable operationTable, final Entity operationComponent,
            final Locale locale) {
        String operationLevel = operationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.level", locale), operationLevel);

        String operationName = operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                .getStringField(OperationFields.NAME);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.name", locale), operationName);

        String operationNumber = operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                .getStringField(OperationFields.NUMBER);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.number", locale), operationNumber);
    }

    private void addWorkstationInfoToTheOperationHeader(final PdfPTable operationTable, final Entity operationComponent,
            final Locale locale) {
        Entity workstationType = extractWorkstationTypeFromToc(operationComponent);

        String workstationTypeName = "";
        String divisionName = "";
        String supervisorName = "";
        String divisionLabel = "";
        String supervisorLabel = "";

        if (workstationType != null) {
            workstationTypeName = workstationType.getStringField(WorkstationTypeFields.NAME);
            // TODO KASI to change
            Entity division = null;
            // Entity division = workstationType.getBelongsToField(WorkstationTypeFields.DIVISION);
            if (division != null) {
                divisionName = division.getStringField(DivisionFields.NAME);
                divisionLabel = translationService.translate("workPlans.workPlan.report.operation.division", locale);
                Entity supervisor = division.getBelongsToField(DivisionFields.SUPERVISOR);
                if (supervisor != null) {
                    supervisorName =
                            supervisor.getStringField(StaffFields.NAME) + " " + supervisor.getStringField(StaffFields.SURNAME);
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

    private Entity extractWorkstationTypeFromToc(final Entity operationComponent) {
        return operationComponent.getBelongsToField(TechnologyOperationComponentFields.WORKSTATION_TYPE);
    }

    void addOperationComment(final Document document, final Entity operationComponent, final Locale locale)
            throws DocumentException {
        PdfPTable table = pdfHelper.createPanelTable(1);
        table.getDefaultCell().setBackgroundColor(null);

        String commentLabel = translationService.translate("workPlans.workPlan.report.operation.comment", locale);
        String commentContent = operationComponent.getStringField(TechnologyOperationComponentFields.COMMENT);

        if (commentContent == null) {
            return;
        }

        pdfHelper.addTableCellAsOneColumnTable(table, commentLabel, commentContent);

        table.setSpacingAfter(18);
        table.setSpacingBefore(9);
        document.add(table);
    }

    private void addOrderInfoToTheOperationHeader(final PdfPTable operationTable, final Entity order, final Locale locale) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        String technologyName = null;
        if (technology != null) {
            technologyName = technology.getStringField(TechnologyFields.NAME);
        }
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.technology", locale), technologyName);

        String orderName = order.getStringField(OrderFields.NAME);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.orderName", locale), orderName);

        String orderNumber = order.getStringField(OrderFields.NUMBER);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.orderNumber", locale), orderNumber);
    }

    private void addInputProductsSeries(final Document document, final Map<Entity, Map<String, String>> columnValues,
            final Entity operationComponent, final Locale locale) throws DocumentException {

        List<Entity> operationProductInComponents = operationComponent
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

        addProductsSeries(document, columnValues, operationComponent, operationProductInComponents, ProductDirection.IN, locale);
    }

    private void addOutputProductsSeries(final Document document, final Map<Entity, Map<String, String>> columnValues,
            final Entity operationComponent, final Locale locale) throws DocumentException {

        List<Entity> operationProductOutComponents = operationComponent
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

        addProductsSeries(document, columnValues, operationComponent, operationProductOutComponents, ProductDirection.OUT,
                locale);
    }

    void addProductsSeries(final Document document, final Map<Entity, Map<String, String>> columnValues,
            final Entity operationComponent, final List<Entity> operationProductComponents, final ProductDirection direction,
            final Locale locale) throws DocumentException {
        if (operationProductComponents.isEmpty()) {
            return;
        }

        List<Entity> productComponents = Lists.newLinkedList(operationProductComponents);
        Collections.sort(productComponents, new OperationProductComponentComparator());

        List<Entity> columnsForProducts = getProductColumnDefinitions(direction, operationComponent);

        if (columnsForProducts.isEmpty()) {
            return;
        }

        PdfPTable table = pdfHelper.createTableWithHeader(columnsForProducts.size(),
                prepareProductsTableHeader(document, columnsForProducts, direction, locale), false);

        for (Entity productComponent : productComponents) {
            for (Entity column : columnsForProducts) {
                String columnIdentifier = column.getStringField(L_IDENTIFIER);
                String value = columnValues.get(productComponent).get(columnIdentifier);

                alignColumn(table.getDefaultCell(), ColumnAlignment.parseString(column.getStringField(L_ALIGNMENT)));

                table.addCell(new Phrase(value, FontUtils.getDejavuRegular7Dark()));
            }
        }

        table.setSpacingAfter(18);
        table.setSpacingBefore(9);

        document.add(table);
    }

    private List<Entity> getProductColumnDefinitions(final ProductDirection direction, final Entity operationComponent) {
        List<Entity> columns = new LinkedList<Entity>();

        String columnDefinitionModel = null;

        List<Entity> columnComponents;
        if (ProductDirection.IN.equals(direction)) {
            columnComponents = operationComponent
                    .getHasManyField(TechnologyOperationComponentFieldsWP.TECHNOLOGY_OPERATION_INPUT_COLUMNS).find()
                    .addOrder(SearchOrders.asc(TechnologyOperationInputColumnFields.SUCCESSION)).list().getEntities();
            columnDefinitionModel = TechnologyOperationInputColumnFields.COLUMN_FOR_INPUT_PRODUCTS;
        } else if (ProductDirection.OUT.equals(direction)) {
            columnComponents = operationComponent
                    .getHasManyField(TechnologyOperationComponentFieldsWP.TECHNOLOGY_OPERATION_OUTPUT_COLUMNS).find()
                    .addOrder(SearchOrders.asc(TechnologyOperationInputColumnFields.SUCCESSION)).list().getEntities();
            columnDefinitionModel = TechnologyOperationOutputColumnFields.COLUMN_FOR_OUTPUT_PRODUCTS;
        } else {
            throw new IllegalStateException("Wrong product direction");
        }

        for (Entity columnComponent : columnComponents) {
            Entity columnDefinition = columnComponent.getBelongsToField(columnDefinitionModel);

            columns.add(columnDefinition);
        }

        return columns;
    }

    List<String> prepareProductsTableHeader(final Document document, final List<Entity> columnsForProducts,
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

        List<String> productsHeader = Lists.newArrayList();

        for (Entity columnForProducts : columnsForProducts) {
            String nameKey = columnForProducts.getStringField(L_NAME);
            productsHeader.add(translationService.translate(nameKey, locale));
        }

        return productsHeader;
    }

    private void alignColumn(final PdfPCell cell, final ColumnAlignment columnAlignment) {
        if (ColumnAlignment.LEFT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        } else if (ColumnAlignment.RIGHT.equals(columnAlignment)) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
    }

    void addAdditionalFields(final Document document, final Entity operationComponent, final Locale locale)
            throws DocumentException {
        String imagePath;
        try {
            imagePath = getImageUrlInWorkPlan(operationComponent);
        } catch (NoSuchElementException e) {
            return;
        }

        String titleString = translationService.translate("workPlans.workPlan.report.additionalFields", locale);
        document.add(new Paragraph(titleString, FontUtils.getDejavuBold10Dark()));

        pdfHelper.addImage(document, imagePath);

        document.add(Chunk.NEXTPAGE);
    }

    String getImageUrlInWorkPlan(final Entity technologyOperationComponent) {
        String imagePath = technologyOperationComponent
                .getStringField(TechnologyOperationComponentFieldsWP.IMAGE_URL_IN_WORK_PLAN);

        if (imagePath == null) {
            throw new NoSuchElementException("no image");
        } else {
            return imagePath;
        }
    }

    boolean isCommentEnabled(final Entity technologyOperationComponent) {
        return !technologyOperationComponent.getBooleanField(TechnologyOperationComponentFieldsWP.HIDE_DESCRIPTION_IN_WORK_PLANS);
    }

    boolean isOrderInfoEnabled(final Entity technologyOperationComponent) {
        return !technologyOperationComponent
                .getBooleanField(TechnologyOperationComponentFieldsWP.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS);
    }

    boolean isWorkstationInfoEnabled(final Entity technologyOperationComponent) {
        return !technologyOperationComponent.getBooleanField(TechnologyOperationComponentFieldsWP.HIDE_DETAILS_IN_WORK_PLANS);
    }

    boolean isInputProductTableEnabled(final Entity technologyOperationComponent) {
        return !technologyOperationComponent
                .getBooleanField(TechnologyOperationComponentFieldsWP.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS);
    }

    boolean isOutputProductTableEnabled(final Entity technologyOperationComponent) {
        return !technologyOperationComponent
                .getBooleanField(TechnologyOperationComponentFieldsWP.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS);
    }

}
