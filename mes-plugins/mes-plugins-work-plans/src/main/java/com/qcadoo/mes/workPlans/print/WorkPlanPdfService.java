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

    enum ProductDirection {
        IN, OUT;
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

    private Map<Entity, Map<String, String>> getColumnValues(List<Entity> orders) {
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
                Class<?> columnFiller;
                try {
                    columnFiller = Class.forName(classString);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Failed to find class: " + classString, e);
                }

                Method method;

                try {
                    method = columnFiller.getMethod("getValues", List.class);
                } catch (SecurityException e) {
                    throw new IllegalStateException("Failed to find column evaulator method in class: " + classString, e);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Failed to find column evaulator method in class: " + classString, e);
                }

                Map<Entity, Map<String, String>> values;

                try {
                    values = (Map<Entity, Map<String, String>>) method.invoke(columnFiller.newInstance(), orders);
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("Failed to invoke column evaulator method", e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Failed to invoke column evaulator method", e);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException("Failed to invoke column evaulator method", e);
                } catch (InstantiationException e) {
                    throw new IllegalStateException("Failed to invoke column evaulator method", e);
                }
                valuesMap.putAll(values);
            }
        }

        return valuesMap;
    }

    void addOperations(Document document, Entity workPlan, final DecimalFormat df, Locale locale) throws DocumentException {
        Map<Entity, Entity> operationComponent2order = new HashMap<Entity, Entity>();

        List<Entity> orders = workPlan.getManyToManyField("orders");

        Map<Entity, Map<String, String>> columnValues = getColumnValues(orders);

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
                    addOutProductsSeries(document, columnValues, operationComponent, df, locale);
                }

                if (isInputProductTableEnabled(operationComponent)) {
                    addInProductsSeries(document, columnValues, operationComponent, df, locale);
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
        String workstationTypeName = "";
        String divisionName = "";
        String supervisorName = "";
        String divisionLabel = "";
        String supervisorLabel = "";

        if (workstationType != null) {
            workstationTypeName = workstationType.getStringField("name");

            Entity division = workstationType.getBelongsToField("division");
            if (division != null) {
                divisionName = division.getStringField("name");
                divisionLabel = getTranslationService().translate("workPlans.workPlan.report.operation.division", locale);
                Entity supervisor = division.getBelongsToField("supervisor");
                if (supervisor != null) {
                    supervisorName = supervisor.getStringField("name") + " " + supervisor.getStringField("surname");
                    supervisorLabel = getTranslationService().translate("workPlans.workPlan.report.operation.supervisor", locale);
                }
            }

            PdfUtil.addTableCellAsTable(operationTable,
                    getTranslationService().translate("workPlans.workPlan.report.operation.workstationType", locale),
                    workstationTypeName, null, PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
            PdfUtil.addTableCellAsTable(operationTable, divisionLabel, divisionName, null, PdfUtil.getArialBold10Dark(),
                    PdfUtil.getArialRegular10Dark());
            PdfUtil.addTableCellAsTable(operationTable, supervisorLabel, supervisorName, null, PdfUtil.getArialBold10Dark(),
                    PdfUtil.getArialRegular10Dark());
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

    void addProductsSeries(List<Entity> productComponents, Document document, Map<Entity, Map<String, String>> columnValues,
            Entity operationComponent, final DecimalFormat df, ProductDirection direction, Locale locale)
            throws DocumentException {
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

        List<Entity> columns = fetchColumnDefinitions(direction, operationComponent);

        if (columns.isEmpty()) {
            return;
        }

        PdfPTable table = PdfUtil.createTableWithHeader(columns.size(),
                prepareProductsTableHeader(document, columns, direction, locale), false);

        for (Entity productComponent : productComponents) {
            for (Entity column : columns) {
                String columnIdentifier = column.getStringField("identifier");
                String value = columnValues.get(productComponent).get(columnIdentifier);
                table.addCell(new Phrase(value, PdfUtil.getArialRegular9Dark()));
            }
        }

        table.setSpacingAfter(18);
        table.setSpacingBefore(9);

        document.add(table);
    }

    void addInProductsSeries(Document document, Map<Entity, Map<String, String>> columnValues, Entity operationComponent,
            final DecimalFormat df, Locale locale) throws DocumentException {

        List<Entity> productComponents = operationComponent.getHasManyField("operationProductInComponents");

        addProductsSeries(productComponents, document, columnValues, operationComponent, df, ProductDirection.IN, locale);
    }

    void addOutProductsSeries(Document document, Map<Entity, Map<String, String>> columnValues, Entity operationComponent,
            final DecimalFormat df, Locale locale) throws DocumentException {

        List<Entity> productComponents = operationComponent.getHasManyField("operationProductOutComponents");

        addProductsSeries(productComponents, document, columnValues, operationComponent, df, ProductDirection.OUT, locale);
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

        PdfUtil.addImage(document, imagePath);
    }

    void addMainHeader(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        String documenTitle = getTranslationService().translate("workPlans.workPlan.report.title", locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, entity.getField("name").toString(), documenTitle, documentAuthor,
                (Date) entity.getField("date"), securityService.getCurrentUserName());
    }

    List<Entity> fetchColumnDefinitions(ProductDirection direction, Entity operationComponent) {
        List<Entity> columns = new LinkedList<Entity>();

        DataDefinition dd;

        final String columnDefinitionModel;

        if (ProductDirection.IN.equals(direction)) {
            dd = dataDefinitionService.get("workPlans", "parameterInputColumn");
            columnDefinitionModel = "columnForInputProducts";
        } else if (ProductDirection.OUT.equals(direction)) {
            dd = dataDefinitionService.get("workPlans", "parameterOutputColumn");
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
            Entity columnDefinition = columnComponent.getBelongsToField(columnDefinitionModel);

            columns.add(columnDefinition);
        }

        return columns;
    }

    List<String> prepareProductsTableHeader(final Document document, List<Entity> columns, ProductDirection direction,
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

        for (Entity column : columns) {
            String nameKey = column.getStringField("name");
            header.add(getTranslationService().translate(nameKey, locale));
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
        orderHeader.add(getTranslationService().translate("workPlans.columnDefinition.name.value.plannedQuantity", locale));
        orderHeader.add(getTranslationService().translate("workPlans.orderTable.dateTo", locale));
        return orderHeader;
    }

    String getImagePathFromDD(Entity operationComponent) {
        String imagePath = operationComponent.getStringField("imageUrlInWorkPlan");

        if (imagePath == null) {
            throw new NoSuchElementException("no image");
        } else {
            return imagePath;
        }
    }

    public boolean isCommentEnabled(Entity operationComponent) {
        boolean hideComment = operationComponent.getBooleanField("hideDescriptionInWorkPlans");
        return !hideComment;
    }

    public boolean isOrderInfoEnabled(Entity operationComponent) {
        boolean hideOrderInfo = operationComponent.getBooleanField("hideTechnologyAndOrderInWorkPlans");
        return !hideOrderInfo;
    }

    public boolean isWorkstationInfoEnabled(Entity operationComponent) {
        boolean hideWorkstationInfo = operationComponent.getBooleanField("hideDetailsInWorkPlans");
        return !hideWorkstationInfo;
    }

    public boolean isInputProductTableEnabled(Entity operationComponent) {
        boolean hideInputProducts = operationComponent.getBooleanField("dontPrintInputProductsInWorkPlans");
        return !hideInputProducts;
    }

    public boolean isOutputProductTableEnabled(Entity operationComponent) {
        boolean hideOutputProducts = operationComponent.getBooleanField("dontPrintOutputProductsInWorkPlans");
        return !hideOutputProducts;
    }
}
