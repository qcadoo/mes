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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.DecimalFormat;
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
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.mes.workPlans.print.WorkPlanPdfService.ProductDirection;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.PrioritizedString;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.security.api.SecurityService;

public class WorkPlanPdfServiceTest {

    private static final String NO_WORKSTATION_TYPE = "No workstation type";

    private static final String NO_DIVISION = "No division";

    private static final String BY_DIVISION = "By division";

    private static final String BY_WORKSTATION_TYPE = "By workstation type";

    private static final String BY_END_PRODUCT = "By end product";

    private static final String NO_DISTINCTION = "No distinction";

    private static final int NUM_OF_UNIQUE_OPERATIONS = 5;

    @Mock
    private TranslationService translationService;

    @Mock
    private SecurityService securityService;

    @Mock
    private PdfHelper pdfHelper;

    @Mock
    private Document document;

    @Mock
    private Entity op1Comp, op2Comp, op3Comp, op4Comp, op5Comp, op6Comp;

    @Mock
    private EntityTreeUtilsService entityTreeUtilsService;

    @Mock
    private Entity workstation1, workstation2;

    @Mock
    private Entity workPlan;

    private WorkPlanPdfService workPlanPdfService;

    private Locale locale;

    private DecimalFormat df;

    private final Map<Entity, Entity> operationComponent2order = new HashMap<Entity, Entity>();

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
        ReflectionTestUtils.setField(workPlanPdfService, "securityService", securityService);
        ReflectionTestUtils.setField(workPlanPdfService, "pdfHelper", pdfHelper);

        locale = Locale.getDefault();

        Entity division1 = mock(Entity.class);
        Entity division2 = mock(Entity.class);

        when(division1.getStringField("name")).thenReturn("division1");
        when(division2.getStringField("name")).thenReturn("division2");

        when(workstation1.getBelongsToField("division")).thenReturn(division1);
        when(workstation2.getBelongsToField("division")).thenReturn(division2);

        when(workstation1.getStringField("name")).thenReturn("workstation1");
        when(workstation2.getStringField("name")).thenReturn("workstation2");

        Entity op1 = mock(Entity.class);
        Entity op2 = mock(Entity.class);
        Entity op3 = mock(Entity.class);
        Entity op4 = mock(Entity.class);
        Entity op5 = mock(Entity.class);

        when(op1Comp.getBelongsToField("operation")).thenReturn(op1);
        when(op2Comp.getBelongsToField("operation")).thenReturn(op2);
        when(op3Comp.getBelongsToField("operation")).thenReturn(op3);
        when(op4Comp.getBelongsToField("operation")).thenReturn(op4);
        when(op5Comp.getBelongsToField("operation")).thenReturn(op5);

        when(op1.getBelongsToField("workstationType")).thenReturn(workstation1);
        when(op2.getBelongsToField("workstationType")).thenReturn(workstation1);
        when(op3.getBelongsToField("workstationType")).thenReturn(workstation2);
        when(op4.getBelongsToField("workstationType")).thenReturn(workstation2);
        when(op5.getBelongsToField("workstationType")).thenReturn(null);

        Entity order1 = mock(Entity.class);
        Entity order2 = mock(Entity.class);

        when(order1.getStringField("number")).thenReturn("1");
        when(order2.getStringField("number")).thenReturn("2");

        EntityList orders = mockEntityListIterator(Arrays.asList(order1, order2));

        Entity tech1 = mock(Entity.class);
        Entity tech2 = mock(Entity.class);

        when(order1.getBelongsToField("technology")).thenReturn(tech1);
        when(order2.getBelongsToField("technology")).thenReturn(tech2);

        Entity prod1 = mock(Entity.class);
        Entity prod2 = mock(Entity.class);

        when(tech1.getBelongsToField("product")).thenReturn(prod1);
        when(tech2.getBelongsToField("product")).thenReturn(prod2);

        when(prod1.getStringField("name")).thenReturn("product1");
        when(prod2.getStringField("name")).thenReturn("product2");

        EntityTree operComp1 = mock(EntityTree.class);
        EntityTree operComp2 = mock(EntityTree.class);

        operComp1 = mockEntityTreeIterator(asList(op1Comp, op2Comp, op3Comp));
        operComp2 = mockEntityTreeIterator(asList(op4Comp, op5Comp));

        when(tech1.getTreeField("operationComponents")).thenReturn(operComp1);
        when(tech2.getTreeField("operationComponents")).thenReturn(operComp2);

        when(entityTreeUtilsService.getSortedEntities(Mockito.any(EntityTree.class))).thenAnswer(new Answer<List<Entity>>() {

            @SuppressWarnings("unchecked")
            @Override
            public List<Entity> answer(InvocationOnMock invocation) throws Throwable {
                return (List<Entity>) invocation.getArguments()[0];
            }
        });

        ReflectionTestUtils.setField(workPlanPdfService, "entityTreeUtilsService", entityTreeUtilsService);

        when(op1Comp.getStringField("nodeNumber")).thenReturn("2.A.1");
        when(op2Comp.getStringField("nodeNumber")).thenReturn("1.");
        when(op3Comp.getStringField("nodeNumber")).thenReturn("2.");
        when(op4Comp.getStringField("nodeNumber")).thenReturn("1.");
        when(op5Comp.getStringField("nodeNumber")).thenReturn("2.");

        when(op1Comp.getStringField("entityType")).thenReturn("operation");
        when(op2Comp.getStringField("entityType")).thenReturn("operation");
        when(op3Comp.getStringField("entityType")).thenReturn("operation");
        when(op4Comp.getStringField("entityType")).thenReturn("operation");
        when(op5Comp.getStringField("entityType")).thenReturn("operation");

        when(workPlan.getManyToManyField("orders")).thenReturn(orders);

        when(translationService.translate("workPlans.workPlan.report.title.noDistinction", locale)).thenReturn(NO_DISTINCTION);
        when(translationService.translate("workPlans.workPlan.report.title.byWorkstationType", locale)).thenReturn(
                BY_WORKSTATION_TYPE);
        when(translationService.translate("workPlans.workPlan.report.title.noWorkstationType", locale)).thenReturn(
                NO_WORKSTATION_TYPE);
        when(translationService.translate("workPlans.workPlan.report.title.byEndProduct", locale)).thenReturn(BY_END_PRODUCT);
        when(translationService.translate("workPlans.workPlan.report.title.byDivision", locale)).thenReturn(BY_DIVISION);
        when(translationService.translate("workPlans.workPlan.report.title.noDivision", locale)).thenReturn(NO_DIVISION);

        df = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
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
        when(name.toString()).thenReturn("name");
        when(workPlan.getField("date")).thenReturn(date);
        when(workPlan.getField("name")).thenReturn(name);
        when(securityService.getCurrentUserName()).thenReturn("userName");

        // when
        workPlanPdfService.addMainHeader(document, workPlan, locale);

        // then
        verify(translationService).translate("workPlans.workPlan.report.title", locale);
        verify(translationService).translate("qcadooReport.commons.generatedBy.label", locale);
    }

    @Test
    public void shouldGetOperationComponentsForNoDistinction() {
        // given
        when(workPlan.getStringField("type")).thenReturn(WorkPlanType.NO_DISTINCTION.getStringValue());

        // when
        Map<PrioritizedString, List<Entity>> operationComponents = workPlanPdfService.getOperationComponentsWithDistinction(
                workPlan, operationComponent2order, locale);

        // then
        isDistinct(operationComponents);
    }

    @Test
    public void shouldTraverseTechnologyTreeEvenThroughReferenedTechnologies() {
        // given
        when(workPlan.getStringField("type")).thenReturn(WorkPlanType.NO_DISTINCTION.getStringValue());
        when(op5Comp.getStringField("entityType")).thenReturn("referenceTechnology");

        Entity refTech = mock(Entity.class);
        EntityTree refTechOps = mockEntityTreeIterator(asList(op6Comp));
        when(op5Comp.getBelongsToField("referenceTechnology")).thenReturn(refTech);
        when(refTech.getTreeField("operationComponents")).thenReturn(refTechOps);

        // when
        Map<PrioritizedString, List<Entity>> operationComponents = workPlanPdfService.getOperationComponentsWithDistinction(
                workPlan, operationComponent2order, locale);

        // then
        isDistinct(operationComponents);
    }

    @Test
    public void shouldGetOperationComponentsForDistinctionByWorkstationType() {
        // given
        when(workPlan.getStringField("type")).thenReturn(WorkPlanType.BY_WORKSTATION_TYPE.getStringValue());

        // when
        Map<PrioritizedString, List<Entity>> operationComponents = workPlanPdfService.getOperationComponentsWithDistinction(
                workPlan, operationComponent2order, locale);

        // then
        isDistinct(operationComponents);
    }

    @Test
    public void shouldGetOperationComponentsForDistinctionByDivision() {
        // given
        when(workPlan.getStringField("type")).thenReturn(WorkPlanType.BY_DIVISION.getStringValue());

        // when
        Map<PrioritizedString, List<Entity>> operationComponents = workPlanPdfService.getOperationComponentsWithDistinction(
                workPlan, operationComponent2order, locale);

        // then
        isDistinct(operationComponents);
    }

    @Test
    public void shouldGetOperationComponentsForDistinctionByEndProduct() {
        // given
        when(workPlan.getStringField("type")).thenReturn(WorkPlanType.BY_END_PRODUCT.getStringValue());

        // when
        Map<PrioritizedString, List<Entity>> operationComponents = workPlanPdfService.getOperationComponentsWithDistinction(
                workPlan, operationComponent2order, locale);

        // then
        isDistinct(operationComponents);
    }

    @Test
    public void shouldAddAdditionalFieldsCorrectly() throws DocumentException {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getStringField("imageUrlInWorkPlan")).thenReturn("actualPath");

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
        Entity operation = mock(Entity.class);
        PdfPTable table = mock(PdfPTable.class);
        PdfPCell cell = mock(PdfPCell.class);
        when(operation.getStringField("comment")).thenReturn("comment");
        when(operationComponent.getBelongsToField("operation")).thenReturn(operation);
        when(operationComponent.getField("hideDescriptionInWorkPlans")).thenReturn(false);
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
        when(column1.getStringField("name")).thenReturn("workPlans.workPlan.report.colums.product");
        when(column2.getStringField("name")).thenReturn("orders.order.plannedQuantity.label");
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
        when(operationComponent.getStringField("imageUrlInWorkPlan")).thenReturn("actualPath");

        // when
        String imagePath = workPlanPdfService.getImagePathFromDD(operationComponent);

        // then
        assertEquals("actualPath", imagePath);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldReturnExceptionIfThereIsNoImage() {
        // given
        Entity operationComponent = mock(Entity.class);

        // when
        String imagePath = workPlanPdfService.getImagePathFromDD(operationComponent);
    }

    @Test
    public void shouldHideOperationCommentIfTold() {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getBooleanField("hideDescriptionInWorkPlans")).thenReturn(true);

        // when
        boolean isCommentEnabled = workPlanPdfService.isCommentEnabled(operationComponent);

        // then
        assertFalse(isCommentEnabled);
    }

    @Test
    public void shouldHideOrderInfoIfTold() {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getBooleanField("hideTechnologyAndOrderInWorkPlans")).thenReturn(true);

        // when
        boolean isOrderInfoEnabled = workPlanPdfService.isOrderInfoEnabled(operationComponent);

        // then
        assertFalse(isOrderInfoEnabled);
    }

    @Test
    public void shouldHideWorkstationInfoIfTold() {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getBooleanField("hideDetailsInWorkPlans")).thenReturn(true);

        // when
        boolean isWorkstationInfoEnabled = workPlanPdfService.isWorkstationInfoEnabled(operationComponent);

        // then
        assertFalse(isWorkstationInfoEnabled);
    }

    @Test
    public void shouldHideInputProductsInfoIfTold() {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getBooleanField("dontPrintInputProductsInWorkPlans")).thenReturn(true);

        // when
        boolean isInputProductTableEnabled = workPlanPdfService.isInputProductTableEnabled(operationComponent);

        // then
        assertFalse(isInputProductTableEnabled);
    }

    @Test
    public void shouldHideOutputProductsInfoIfTold() {
        // given
        Entity operationComponent = mock(Entity.class);
        when(operationComponent.getBooleanField("dontPrintOutputProductsInWorkPlans")).thenReturn(true);

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
        Entity column = mock(Entity.class);
        Entity column2 = mock(Entity.class);
        given(column.getStringField("name")).willReturn("column");
        given(column2.getStringField("name")).willReturn("column2");
        given(translationService.translate("column", locale)).willReturn("column");
        given(translationService.translate("column2", locale)).willReturn("column2");
        List<Entity> columns = asList(column, column2);

        // when
        List<String> ordersHeader = workPlanPdfService.prepareOrdersTableHeader(document, columns, locale);

        // then
        verify(document).add(Mockito.any(Paragraph.class));
        assertEquals("column", ordersHeader.get(0));
        assertEquals("column2", ordersHeader.get(1));
    }

    @Test
    public void shouldPrepareProductSeriesTableCorrectly() {
        // given
        Entity prodComp1 = mock(Entity.class);
        Entity prodComp2 = mock(Entity.class);
        Entity prod1 = mock(Entity.class);
        Entity prod2 = mock(Entity.class);
        when(prod1.getStringField("number")).thenReturn("1");
        when(prod2.getStringField("number")).thenReturn("2");
        when(prodComp1.getBelongsToField("product")).thenReturn(prod1);
        when(prodComp2.getBelongsToField("product")).thenReturn(prod2);

        List<Entity> components = asList(prodComp1, prodComp2);
        Document document = mock(Document.class);
        Map<Entity, Map<String, String>> columnValues = new HashMap<Entity, Map<String, String>>();
        Entity operationComponent = mock(Entity.class);
        Entity column1 = mock(Entity.class);
        Entity column2 = mock(Entity.class);
        Entity column1Def = mock(Entity.class);
        Entity column2Def = mock(Entity.class);
        when(column1.getBelongsToField("columnForInputProducts")).thenReturn(column1Def);
        when(column2.getBelongsToField("columnForInputProducts")).thenReturn(column2Def);

        when(column1Def.getStringField("identifier")).thenReturn("column1");
        when(column2Def.getStringField("identifier")).thenReturn("column2");

        when(column1Def.getStringField("name")).thenReturn("column1");
        when(column2Def.getStringField("name")).thenReturn("column2");

        when(column1.getField("succession")).thenReturn(Integer.valueOf(1));
        when(column2.getField("succession")).thenReturn(Integer.valueOf(2));

        when(column1Def.getStringField("alignment")).thenReturn("01left");
        when(column2Def.getStringField("alignment")).thenReturn("02right");

        columnValues.put(prodComp1, new HashMap<String, String>());
        columnValues.put(prodComp2, new HashMap<String, String>());

        columnValues.get(prodComp1).put("column1", "value1");
        columnValues.get(prodComp1).put("column1", "value2");

        columnValues.get(prodComp2).put("column1", "value1");
        columnValues.get(prodComp2).put("column1", "value2");

        EntityList columns = mockEntityListIterator(asList(column1, column2));
        when(operationComponent.getHasManyField("technologyOperationInputColumns")).thenReturn(columns);
        ProductDirection direction = ProductDirection.IN;

        PdfPTable table = mock(PdfPTable.class);
        PdfPCell cell = mock(PdfPCell.class);
        when(table.getDefaultCell()).thenReturn(cell);

        when(pdfHelper.createTableWithHeader(Mockito.eq(2), Mockito.anyList(), Mockito.eq(false))).thenReturn(table);
        when(translationService.translate("workPlans.workPlan.report.productsInTable", locale)).thenReturn("title");

        // when
        try {
            workPlanPdfService.addProductsSeries(components, document, columnValues, operationComponent, df, direction, locale);
        } catch (DocumentException e) {
        }

        // then
        try {
            verify(document).add(table);
            verify(cell, times(2)).setHorizontalAlignment(Element.ALIGN_LEFT);
            verify(cell, times(2)).setHorizontalAlignment(Element.ALIGN_RIGHT);
            verify(table, times(4)).addCell(new Phrase(Mockito.anyString(), FontUtils.getDejavuRegular9Dark()));
        } catch (DocumentException e) {
        }
    }

    private void isDistinct(final Map<PrioritizedString, List<Entity>> map) {
        List<Entity> opComponents = Lists.newArrayList();
        for (List<Entity> opComponent : map.values()) {
            opComponents.addAll(opComponent);
        }
        Assert.assertEquals(NUM_OF_UNIQUE_OPERATIONS, Sets.newHashSet(opComponents).size());
    }

}
