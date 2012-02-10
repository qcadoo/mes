/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.util.EntityNumberComparator;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
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

    private static final String NAME_LITERAL = "name";

    private static final String NUMBER_LITERAL = "number";

    private static final String PRODUCT_LITERAL = "product";

    private static Locale currentLocale = LocaleContextHolder.getLocale();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DateUtils.DATE_FORMAT, currentLocale);

    private final int[] defaultWorkPlanColumnWidth = new int[] { 15, 25, 30, 15, 15 };

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private ApplicationContext applicationContext;

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

        PdfPTable orderTable = pdfHelper.createTableWithHeader(5, prepareOrdersTableHeader(document, workPlan, locale), false,
                defaultWorkPlanColumnWidth);

        List<Entity> orders = workPlan.getManyToManyField("orders");
        addOrderSeries(orderTable, orders, decimalFormat);
        document.add(orderTable);
        document.add(Chunk.NEWLINE);

        addOperations(document, workPlan, decimalFormat, locale);
    }

    @SuppressWarnings("unchecked")
    private Map<Entity, Map<String, String>> getColumnValues(final List<Entity> orders) {
        Map<Entity, Map<String, String>> valuesMap = new HashMap<Entity, Map<String, String>>();

        for (String columnDefinitionModel : Arrays.asList("columnForInputProducts", "columnForOutputProducts")) {
            DataDefinition dd = dataDefinitionService.get("workPlans", columnDefinitionModel);

            List<Entity> columnDefinitions = dd.find().list().getEntities();

            Set<String> classesStrings = new HashSet<String>();

            for (Entity columnDefinition : columnDefinitions) {
                String classString = columnDefinition.getStringField("columnFiller");
                classesStrings.add(classString);
            }

            for (String classString : classesStrings) {
                Class<?> clazz;
                try {
                    clazz = Thread.currentThread().getContextClassLoader().loadClass(classString);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Failed to find class: " + classString, e);
                }

                Object bean = applicationContext.getBean(clazz);

                if (bean == null) {
                    throw new IllegalStateException("Failed to find bean for class: " + classString);
                }

                Method method;

                try {
                    method = clazz.getMethod("getValues", List.class);
                } catch (SecurityException e) {
                    throw new IllegalStateException("Failed to find column evaulator method in class: " + classString, e);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Failed to find column evaulator method in class: " + classString, e);
                }

                Map<Entity, Map<String, String>> values;

                String invokeMethodError = "Failed to invoke column evaulator method";
                try {
                    values = (Map<Entity, Map<String, String>>) method.invoke(bean, orders);
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException(invokeMethodError, e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(invokeMethodError, e);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException(invokeMethodError, e);
                }

                for (Entry<Entity, Map<String, String>> entry : values.entrySet()) {
                    if (valuesMap.containsKey(entry.getKey())) {
                        for (Entry<String, String> deepEntry : entry.getValue().entrySet()) {
                            valuesMap.get(entry.getKey()).put(deepEntry.getKey(), deepEntry.getValue());
                        }
                    } else {
                        valuesMap.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        return valuesMap;
    }

    void addOperations(final Document document, final Entity workPlan, final DecimalFormat df, final Locale locale)
            throws DocumentException {
        Map<Entity, Entity> operationComponent2order = new HashMap<Entity, Entity>();

        List<Entity> orders = workPlan.getManyToManyField("orders");

        Map<Entity, Map<String, String>> columnValues = getColumnValues(orders);

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

        String operationName = operationComponent.getBelongsToField(OPERATION_LITERAL).getStringField(NAME_LITERAL);
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
            workstationTypeName = workstationType.getStringField(NAME_LITERAL);

            Entity division = workstationType.getBelongsToField("division");
            if (division != null) {
                divisionName = division.getStringField(NAME_LITERAL);
                divisionLabel = translationService.translate("workPlans.workPlan.report.operation.division", locale);
                Entity supervisor = division.getBelongsToField("supervisor");
                if (supervisor != null) {
                    supervisorName = supervisor.getStringField(NAME_LITERAL) + " " + supervisor.getStringField("surname");
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
            technologyString = technology.getStringField(NAME_LITERAL);
        }
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.technology", locale), technologyString);

        String orderName = order.getStringField(NAME_LITERAL);
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
            String endProductName = endProduct.getStringField(NAME_LITERAL);
            title = new PrioritizedString(prefix + " " + endProductName);
        } else if (WorkPlanType.BY_WORKSTATION_TYPE.getStringValue().equals(type)) {
            Entity workstation = operationComponent.getBelongsToField(OPERATION_LITERAL).getBelongsToField("workstationType");

            if (workstation == null) {
                title = new PrioritizedString(translationService.translate("workPlans.workPlan.report.title.noWorkstationType",
                        locale), 1);
            } else {
                String suffix = translationService.translate("workPlans.workPlan.report.title.byWorkstationType", locale);
                String workstationName = workstation.getStringField(NAME_LITERAL);
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
                    String divisionName = division.getStringField(NAME_LITERAL);
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

    void addOrderSeries(final PdfPTable table, final List<Entity> orders, final DecimalFormat df) throws DocumentException {
        Collections.sort(orders, new EntityNumberComparator());
        for (Entity order : orders) {
            table.addCell(new Phrase(order.getField(NUMBER_LITERAL).toString(), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(order.getField(NAME_LITERAL).toString(), FontUtils.getDejavuRegular9Dark()));

            String unitString = "";

            Entity product = (Entity) order.getField(PRODUCT_LITERAL);
            if (product == null) {
                table.addCell(new Phrase("", FontUtils.getDejavuRegular9Dark()));
            } else {
                String productString = product.getField(NAME_LITERAL).toString() + " ("
                        + product.getField(NUMBER_LITERAL).toString() + ")";
                table.addCell(new Phrase(productString, FontUtils.getDejavuRegular9Dark()));

                Object unit = product.getField("unit");
                if (unit != null) {
                    unitString = " " + unit.toString();
                }
            }

            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");
            plannedQuantity = (plannedQuantity == null) ? BigDecimal.ZERO : plannedQuantity;
            String quantityString = df.format(plannedQuantity) + unitString;
            table.addCell(new Phrase(quantityString, FontUtils.getDejavuRegular9Dark()));
            String formattedDateTo = "-";
            if (order.getField("dateTo") != null) {
                synchronized (this) {
                    formattedDateTo = DATE_FORMAT.format((Date) order.getField("dateTo"));
                }
            }
            table.addCell(new Phrase(formattedDateTo, FontUtils.getDejavuRegular9Dark()));
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

        String titleString = translationService.translate("workPlans.workPlan.report.additionalFields", locale);
        document.add(new Paragraph(titleString, FontUtils.getDejavuBold10Dark()));

        pdfHelper.addImage(document, imagePath);
    }

    void addMainHeader(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documenTitle = translationService.translate("workPlans.workPlan.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeader(document, entity.getField(NAME_LITERAL).toString(), documenTitle, documentAuthor,
                (Date) entity.getField("date"), securityService.getCurrentUserName());
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
            String nameKey = column.getStringField(NAME_LITERAL);
            header.add(translationService.translate(nameKey, locale));
        }

        return header;
    }

    List<String> prepareOrdersTableHeader(final Document document, final Entity entity, final Locale locale)
            throws DocumentException {
        document.add(new Paragraph(translationService.translate("workPlans.workPlan.report.ordersTable", locale), FontUtils
                .getDejavuBold11Dark()));
        List<String> orderHeader = new ArrayList<String>();
        orderHeader.add(translationService.translate("orders.order.number.label", locale));
        orderHeader.add(translationService.translate("orders.order.name.label", locale));
        orderHeader.add(translationService.translate("workPlans.workPlan.report.colums.product", locale));
        orderHeader.add(translationService.translate("workPlans.workPlan.report.colums.plannedQuantity", locale));
        orderHeader.add(translationService.translate("workPlans.orderTable.dateTo", locale));
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
