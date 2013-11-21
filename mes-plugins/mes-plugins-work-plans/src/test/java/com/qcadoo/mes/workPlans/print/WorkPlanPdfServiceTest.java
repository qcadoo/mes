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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.DivisionFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentEntityType;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.workPlans.constants.ColumnForInputProductsFields;
import com.qcadoo.mes.workPlans.constants.TechnologyOperationComponentFieldsWP;
import com.qcadoo.mes.workPlans.constants.TechnologyOperationInputColumnFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.mes.workPlans.print.WorkPlanPdfService.ProductDirection;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.PrioritizedString;
import com.qcadoo.report.api.pdf.PdfHelper;

public class WorkPlanPdfServiceTest {

    private static final String L_NAME = "name";

    private static final String L_NO_WORKSTATION_TYPE = "No workstation type";

    private static final String L_NO_DIVISION = "No division";

    private static final String L_GET_ORDER_ID_WITH_TITLE_AND_OPERATION_COMPONENTS_MAP = "getOrderIdWithTitleAndOperationComponentsMap";

    private static final int L_NUM_OF_UNIQUE_OPERATIONS = 5;

    private WorkPlanPdfService workPlanPdfService;

    @Mock
    private TranslationService translationService;

    @Mock
    private EntityTreeUtilsService entityTreeUtilsService;

    @Mock
    private PdfHelper pdfHelper;

    @Mock
    private Document document;

    @Mock
    private Entity workPlan, order1, order2, technology1, technology2, operationComponent1, operationComponent2,
            operationComponent3, operationComponent4, operationComponent5, operation1, operation2, operation3, operation4,
            operation5, workstation1, workstation2, division1, division2, product1, product2;

    @Mock
    private EntityTree operationComponents1, operationComponents2;

    private Locale locale;

    private static EntityList mockEntityListIterator(final List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    private static EntityTree mockEntityTreeIterator(final List<Entity> list) {
        EntityTree entityTree = mock(EntityTree.class);
        when(entityTree.iterator()).thenReturn(list.iterator());
        return entityTree;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        workPlanPdfService = new WorkPlanPdfService();

        ReflectionTestUtils.setField(workPlanPdfService, "translationService", translationService);
        ReflectionTestUtils.setField(workPlanPdfService, "pdfHelper", pdfHelper);

        locale = Locale.getDefault();

        when(division1.getStringField(DivisionFields.NAME)).thenReturn("division1");
        when(division2.getStringField(DivisionFields.NAME)).thenReturn("division2");

        // when(workstation1.getBelongsToField(WorkstationTypeFields.DIVISION)).thenReturn(division1);
        // when(workstation2.getBelongsToField(WorkstationTypeFields.DIVISION)).thenReturn(division2);

        when(workstation1.getStringField(WorkstationTypeFields.NAME)).thenReturn("workstation1");
        when(workstation2.getStringField(WorkstationTypeFields.NAME)).thenReturn("workstation2");

        when(operationComponent1.getBelongsToField(TechnologyOperationComponentFields.OPERATION)).thenReturn(operation1);
        when(operationComponent2.getBelongsToField(TechnologyOperationComponentFields.OPERATION)).thenReturn(operation2);
        when(operationComponent3.getBelongsToField(TechnologyOperationComponentFields.OPERATION)).thenReturn(operation3);
        when(operationComponent4.getBelongsToField(TechnologyOperationComponentFields.OPERATION)).thenReturn(operation4);
        when(operationComponent5.getBelongsToField(TechnologyOperationComponentFields.OPERATION)).thenReturn(operation5);

        when(operation1.getBelongsToField(OperationFields.WORKSTATION_TYPE)).thenReturn(workstation1);
        when(operation2.getBelongsToField(OperationFields.WORKSTATION_TYPE)).thenReturn(workstation1);
        when(operation3.getBelongsToField(OperationFields.WORKSTATION_TYPE)).thenReturn(workstation2);
        when(operation4.getBelongsToField(OperationFields.WORKSTATION_TYPE)).thenReturn(workstation2);
        when(operation5.getBelongsToField(OperationFields.WORKSTATION_TYPE)).thenReturn(null);

        when(order1.getId()).thenReturn(1L);
        when(order2.getId()).thenReturn(2L);

        when(order1.getStringField(OrderFields.NUMBER)).thenReturn("1");
        when(order2.getStringField(OrderFields.NUMBER)).thenReturn("2");

        EntityList orders = mockEntityListIterator(Arrays.asList(order1, order2));

        when(order1.getBelongsToField(OrderFields.TECHNOLOGY)).thenReturn(technology1);
        when(order2.getBelongsToField(OrderFields.TECHNOLOGY)).thenReturn(technology2);

        when(technology1.getBelongsToField(TechnologyFields.PRODUCT)).thenReturn(product1);
        when(technology2.getBelongsToField(TechnologyFields.PRODUCT)).thenReturn(product2);

        when(product1.getStringField(ProductFields.NAME)).thenReturn("product1");
        when(product2.getStringField(ProductFields.NAME)).thenReturn("product2");

        operationComponents1 = mockEntityTreeIterator(asList(operationComponent1, operationComponent2, operationComponent3));
        operationComponents2 = mockEntityTreeIterator(asList(operationComponent4, operationComponent5));

        when(technology1.getTreeField(TechnologyFields.OPERATION_COMPONENTS)).thenReturn(operationComponents1);
        when(technology2.getTreeField(TechnologyFields.OPERATION_COMPONENTS)).thenReturn(operationComponents2);

        when(entityTreeUtilsService.getSortedEntities(Mockito.any(EntityTree.class))).thenAnswer(new Answer<List<Entity>>() {

            @SuppressWarnings("unchecked")
            @Override
            public List<Entity> answer(InvocationOnMock invocation) throws Throwable {
                return (List<Entity>) invocation.getArguments()[0];
            }
        });

        ReflectionTestUtils.setField(workPlanPdfService, "entityTreeUtilsService", entityTreeUtilsService);

        when(operationComponent1.getStringField(TechnologyOperationComponentFields.NODE_NUMBER)).thenReturn("2.A.1");
        when(operationComponent2.getStringField(TechnologyOperationComponentFields.NODE_NUMBER)).thenReturn("1.");
        when(operationComponent3.getStringField(TechnologyOperationComponentFields.NODE_NUMBER)).thenReturn("2.");
        when(operationComponent4.getStringField(TechnologyOperationComponentFields.NODE_NUMBER)).thenReturn("1.");
        when(operationComponent5.getStringField(TechnologyOperationComponentFields.NODE_NUMBER)).thenReturn("2.");

        when(operationComponent1.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE)).thenReturn(
                TechnologyOperationComponentEntityType.OPERATION.getStringValue());
        when(operationComponent2.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE)).thenReturn(
                TechnologyOperationComponentEntityType.OPERATION.getStringValue());
        when(operationComponent3.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE)).thenReturn(
                TechnologyOperationComponentEntityType.OPERATION.getStringValue());
        when(operationComponent4.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE)).thenReturn(
                TechnologyOperationComponentEntityType.OPERATION.getStringValue());
        when(operationComponent5.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE)).thenReturn(
                TechnologyOperationComponentEntityType.OPERATION.getStringValue());

        when(workPlan.getManyToManyField(WorkPlanFields.ORDERS)).thenReturn(orders);

        when(translationService.translate("workPlans.workPlan.report.title.noDistinction", locale)).thenReturn(
                WorkPlanType.NO_DISTINCTION.getStringValue());
        when(translationService.translate("workPlans.workPlan.report.title.byWorkstationType", locale)).thenReturn(
                WorkPlanType.BY_WORKSTATION_TYPE.getStringValue());
        when(translationService.translate("workPlans.workPlan.report.title.byEndProduct", locale)).thenReturn(
                WorkPlanType.BY_END_PRODUCT.getStringValue());
        when(translationService.translate("workPlans.workPlan.report.title.byDivision", locale)).thenReturn(
                WorkPlanType.BY_DIVISION.getStringValue());
        when(translationService.translate("workPlans.workPlan.report.title.noDivision", locale)).thenReturn(L_NO_DIVISION);
        when(translationService.translate("workPlans.workPlan.report.title.noWorkstationType", locale)).thenReturn(
                L_NO_WORKSTATION_TYPE);
    }

    @Test
    public void shouldHasCorrectReportTitle() {
        // given
        String correctTitle = "title";
        when(translationService.translate("workPlans.workPlan.report.title", locale)).thenReturn(correctTitle);

        // when
        String title = workPlanPdfService.getReportTitle(locale);

        // then
        assertEquals(correctTitle, title);
    }

    @Test
    public void shouldAddCorrectMainHeader() throws DocumentException {
        // given
        Date date = mock(Date.class);
        Object name = mock(Object.class);
        when(name.toString()).thenReturn(L_NAME);
        when(workPlan.getField(WorkPlanFields.DATE)).thenReturn(date);
        when(workPlan.getField(WorkPlanFields.NAME)).thenReturn(name);

        // when
        workPlanPdfService.addMainHeader(document, workPlan, locale);

        // then
        verify(translationService).translate("workPlans.workPlan.report.title", locale);
        verify(translationService).translate("qcadooReport.commons.generatedBy.label", locale);
    }

    @Test
    public void shouldGetOperationComponentsForNoDistinction() {
        // given
        when(workPlan.getStringField(WorkPlanFields.TYPE)).thenReturn(WorkPlanType.NO_DISTINCTION.getStringValue());

        // when
        Map<Long, Map<PrioritizedString, List<Entity>>> operationComponents = ReflectionTestUtils.invokeMethod(
                workPlanPdfService, L_GET_ORDER_ID_WITH_TITLE_AND_OPERATION_COMPONENTS_MAP, workPlan, locale);

        // then
        isDistinct(operationComponents);
    }

    @Test
    public void shouldGetOperationComponentsForDistinctionByWorkstationType() {
        // given
        when(workPlan.getStringField(WorkPlanFields.TYPE)).thenReturn(WorkPlanType.BY_WORKSTATION_TYPE.getStringValue());

        // when
        Map<Long, Map<PrioritizedString, List<Entity>>> operationComponents = ReflectionTestUtils.invokeMethod(
                workPlanPdfService, L_GET_ORDER_ID_WITH_TITLE_AND_OPERATION_COMPONENTS_MAP, workPlan, locale);

        // then
        isDistinct(operationComponents);
    }

    @Test
    public void shouldGetOperationComponentsForDistinctionByDivision() {
        // given
        when(workPlan.getStringField(WorkPlanFields.TYPE)).thenReturn(WorkPlanType.BY_DIVISION.getStringValue());

        // when
        Map<Long, Map<PrioritizedString, List<Entity>>> operationComponents = ReflectionTestUtils.invokeMethod(
                workPlanPdfService, L_GET_ORDER_ID_WITH_TITLE_AND_OPERATION_COMPONENTS_MAP, workPlan, locale);

        // then
        isDistinct(operationComponents);
    }

    @Test
    public void shouldGetOperationComponentsForDistinctionByEndProduct() {
        // given
        when(workPlan.getStringField(WorkPlanFields.TYPE)).thenReturn(WorkPlanType.BY_END_PRODUCT.getStringValue());

        // when
        Map<Long, Map<PrioritizedString, List<Entity>>> operationComponents = ReflectionTestUtils.invokeMethod(
                workPlanPdfService, L_GET_ORDER_ID_WITH_TITLE_AND_OPERATION_COMPONENTS_MAP, workPlan, locale);

        // then
        isDistinct(operationComponents);
    }

    @Test
    public void shouldAddAdditionalFieldsCorrectly() throws DocumentException {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getStringField(TechnologyOperationComponentFieldsWP.IMAGE_URL_IN_WORK_PLAN)).thenReturn(
                "actualPath");

        // when
        workPlanPdfService.addAdditionalFields(document, operationComponent, locale);

        // then
        verify(document, times(2)).add(Mockito.any(Paragraph.class));
        verify(document, times(2)).add(Mockito.any(Image.class));
    }

    @Test
    public void shouldAddOperationComment() throws DocumentException {
        // given
        Entity operationComponent = mock(Entity.class);
        PdfPTable table = mock(PdfPTable.class);
        PdfPCell cell = mock(PdfPCell.class);

        when(operationComponent.getStringField(TechnologyOperationComponentFields.COMMENT)).thenReturn("comment");
        when(operationComponent.getField(TechnologyOperationComponentFieldsWP.HIDE_DESCRIPTION_IN_WORK_PLANS)).thenReturn(false);
        when(pdfHelper.createPanelTable(1)).thenReturn(table);
        when(table.getDefaultCell()).thenReturn(cell);

        // when
        workPlanPdfService.addOperationComment(document, operationComponent, locale);

        // then
        verify(document).add(Mockito.any(PdfPTable.class));
    }

    @Test
    public void shouldPrepareProductTableHeaderCorrectly() throws DocumentException {
        // given
        when(translationService.translate("workPlans.workPlan.report.colums.product", locale)).thenReturn("product");
        when(translationService.translate("orders.order.plannedQuantity.label", locale)).thenReturn("quantity");
        List<Entity> columns = new LinkedList<Entity>();
        Entity column1 = mock(Entity.class);
        Entity column2 = mock(Entity.class);
        columns.add(column1);
        columns.add(column2);
        when(column1.getStringField(L_NAME)).thenReturn("workPlans.workPlan.report.colums.product");
        when(column2.getStringField(L_NAME)).thenReturn("orders.order.plannedQuantity.label");
        ProductDirection direction = ProductDirection.IN;

        // when
        List<String> header = workPlanPdfService.prepareProductsTableHeader(document, columns, direction, locale);

        // then
        verify(document).add(Mockito.any(Paragraph.class));
        assertEquals("product", header.get(0));
        assertEquals("quantity", header.get(1));
    }

    @Test
    public void shouldGetCorrectImagePathFromDataDefinition() {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getStringField(TechnologyOperationComponentFieldsWP.IMAGE_URL_IN_WORK_PLAN)).thenReturn(
                "actualPath");

        // when
        String imagePath = workPlanPdfService.getImageUrlInWorkPlan(operationComponent);

        // then
        assertEquals("actualPath", imagePath);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldReturnExceptionIfThereIsNoImage() {
        // given
        Entity operationComponent = mock(Entity.class);

        // when
        String imagePath = workPlanPdfService.getImageUrlInWorkPlan(operationComponent);
    }

    @Test
    public void shouldHideOperationCommentIfTold() {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getBooleanField(TechnologyOperationComponentFieldsWP.HIDE_DESCRIPTION_IN_WORK_PLANS)).thenReturn(
                true);

        // when
        boolean isCommentEnabled = workPlanPdfService.isCommentEnabled(operationComponent);

        // then
        assertFalse(isCommentEnabled);
    }

    @Test
    public void shouldHideOrderInfoIfTold() {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getBooleanField(TechnologyOperationComponentFieldsWP.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS))
                .thenReturn(true);

        // when
        boolean isOrderInfoEnabled = workPlanPdfService.isOrderInfoEnabled(operationComponent);

        // then
        assertFalse(isOrderInfoEnabled);
    }

    @Test
    public void shouldHideWorkstationInfoIfTold() {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getBooleanField(TechnologyOperationComponentFieldsWP.HIDE_DETAILS_IN_WORK_PLANS))
                .thenReturn(true);

        // when
        boolean isWorkstationInfoEnabled = workPlanPdfService.isWorkstationInfoEnabled(operationComponent);

        // then
        assertFalse(isWorkstationInfoEnabled);
    }

    @Test
    public void shouldHideInputProductsInfoIfTold() {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getBooleanField(TechnologyOperationComponentFieldsWP.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS))
                .thenReturn(true);

        // when
        boolean isInputProductTableEnabled = workPlanPdfService.isInputProductTableEnabled(operationComponent);

        // then
        assertFalse(isInputProductTableEnabled);
    }

    @Test
    public void shouldHideOutputProductsInfoIfTold() {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getBooleanField(TechnologyOperationComponentFieldsWP.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS))
                .thenReturn(true);

        // when
        boolean isOutputProductTableEnabled = workPlanPdfService.isOutputProductTableEnabled(operationComponent);

        // then
        assertFalse(isOutputProductTableEnabled);
    }

    @Test
    public void shouldPrepareOrdersTableHeaderCorrectly() throws Exception {
        // given
        Locale locale = Locale.getDefault();
        Document document = mock(Document.class);
        Entity column1 = mock(Entity.class);
        Entity column2 = mock(Entity.class);
        given(column1.getStringField(L_NAME)).willReturn("column1");
        given(column2.getStringField(L_NAME)).willReturn("column2");
        given(translationService.translate("column1", locale)).willReturn("column1");
        given(translationService.translate("column2", locale)).willReturn("column2");
        List<Entity> columns = asList(column1, column2);

        // when
        List<String> ordersHeader = workPlanPdfService.prepareOrdersTableHeader(document, columns, locale);

        // then
        verify(document).add(Mockito.any(Paragraph.class));
        assertEquals("column1", ordersHeader.get(0));
        assertEquals("column2", ordersHeader.get(1));
    }

    @Test
    public void shouldPrepareProductSeriesTableCorrectly() {
        // given
        Entity prodComp1 = mock(Entity.class);
        Entity prodComp2 = mock(Entity.class);
        Entity prod1 = mock(Entity.class);
        Entity prod2 = mock(Entity.class);
        SearchCriteriaBuilder searchCriteriaBuilder = mock(SearchCriteriaBuilder.class);
        SearchResult searchResult = mock(SearchResult.class);
        when(prod1.getStringField(ProductFields.NUMBER)).thenReturn("1");
        when(prod2.getStringField(ProductFields.NUMBER)).thenReturn("2");
        when(prodComp1.getBelongsToField(OperationProductInComponentFields.PRODUCT)).thenReturn(prod1);
        when(prodComp2.getBelongsToField(OperationProductInComponentFields.PRODUCT)).thenReturn(prod2);

        List<Entity> operationProductInputComponents = asList(prodComp1, prodComp2);
        Document document = mock(Document.class);
        Map<Entity, Map<String, String>> columnValues = Maps.newHashMap();
        Entity operationComponent = mock(Entity.class);
        Entity column1 = mock(Entity.class);
        Entity column2 = mock(Entity.class);
        Entity column1Def = mock(Entity.class);
        Entity column2Def = mock(Entity.class);
        when(column1.getBelongsToField(TechnologyOperationInputColumnFields.COLUMN_FOR_INPUT_PRODUCTS)).thenReturn(column1Def);
        when(column2.getBelongsToField(TechnologyOperationInputColumnFields.COLUMN_FOR_INPUT_PRODUCTS)).thenReturn(column2Def);

        when(column1Def.getStringField(ColumnForInputProductsFields.IDENTIFIER)).thenReturn("column1");
        when(column2Def.getStringField(ColumnForInputProductsFields.IDENTIFIER)).thenReturn("column2");

        when(column1Def.getStringField(ColumnForInputProductsFields.NAME)).thenReturn("column1");
        when(column2Def.getStringField(ColumnForInputProductsFields.NAME)).thenReturn("column2");

        when(column1.getField(TechnologyOperationInputColumnFields.SUCCESSION)).thenReturn(Integer.valueOf(1));
        when(column2.getField(TechnologyOperationInputColumnFields.SUCCESSION)).thenReturn(Integer.valueOf(2));

        when(column1Def.getStringField(ColumnForInputProductsFields.ALIGNMENT)).thenReturn("01left");
        when(column2Def.getStringField(ColumnForInputProductsFields.ALIGNMENT)).thenReturn("02right");

        columnValues.put(prodComp1, new HashMap<String, String>());
        columnValues.put(prodComp2, new HashMap<String, String>());

        columnValues.get(prodComp1).put("column1", "value1");
        columnValues.get(prodComp1).put("column1", "value2");

        columnValues.get(prodComp2).put("column1", "value1");
        columnValues.get(prodComp2).put("column1", "value2");

        EntityList columns = mockEntityListIterator(asList(column1, column2));
        when(operationComponent.getHasManyField(TechnologyOperationComponentFieldsWP.TECHNOLOGY_OPERATION_INPUT_COLUMNS))
                .thenReturn(columns);
        when(columns.find()).thenReturn(searchCriteriaBuilder);
        when(searchCriteriaBuilder.addOrder(Mockito.any(SearchOrder.class))).thenReturn(searchCriteriaBuilder);
        when(searchCriteriaBuilder.list()).thenReturn(searchResult);
        when(searchResult.getEntities()).thenReturn(columns);

        ProductDirection direction = ProductDirection.IN;

        PdfPTable table = mock(PdfPTable.class);
        PdfPCell cell = mock(PdfPCell.class);
        when(table.getDefaultCell()).thenReturn(cell);

        when(pdfHelper.createTableWithHeader(Mockito.eq(2), Mockito.anyList(), Mockito.eq(false))).thenReturn(table);
        when(translationService.translate("workPlans.workPlan.report.productsInTable", locale)).thenReturn("title");

        // when
        try {
            workPlanPdfService.addProductsSeries(document, columnValues, operationComponent, operationProductInputComponents,
                    direction, locale);
        } catch (DocumentException e) {
        }

        // then
        try {
            verify(document).add(table);
            verify(cell, times(2)).setHorizontalAlignment(Element.ALIGN_LEFT);
            verify(cell, times(2)).setHorizontalAlignment(Element.ALIGN_RIGHT);
            verify(table, times(4)).addCell(new Phrase(Mockito.anyString(), FontUtils.getDejavuRegular7Dark()));
        } catch (DocumentException e) {
        }
    }

    private void isDistinct(final Map<Long, Map<PrioritizedString, List<Entity>>> orderId2OpComponentsMap) {
        final List<Entity> opComponents = Lists.newArrayList();
        for (final Map<PrioritizedString, List<Entity>> opComponentsMap : orderId2OpComponentsMap.values()) {
            for (List<Entity> opComponent : opComponentsMap.values()) {
                opComponents.addAll(opComponent);
            }
        }
        Assert.assertEquals(L_NUM_OF_UNIQUE_OPERATIONS, Sets.newHashSet(opComponents).size());
    }

}
