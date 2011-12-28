package com.qcadoo.mes.workPlans.print;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

public class WorkPlanPdfServiceTest {

    private WorkPlanPdfService2 workPlanPdfService;

    private TranslationService translationService;

    private SecurityService securityService;

    private Locale locale;

    private Document document;

    private Entity workPlan;

    @Before
    public void init() {
        workPlanPdfService = new WorkPlanPdfService2();

        translationService = mock(TranslationService.class);
        securityService = mock(SecurityService.class);

        ReflectionTestUtils.setField(workPlanPdfService, "translationService", translationService);
        ReflectionTestUtils.setField(workPlanPdfService, "securityService", securityService);

        locale = Locale.getDefault();
        document = mock(Document.class);
        workPlan = mock(Entity.class);
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
    public void shouldPrepareCorrectOrdersTableHeader() throws DocumentException {
        // given
        when(translationService.translate("workPlans.workPlan.report.paragrah", locale)).thenReturn("paragraph");
        when(translationService.translate("orders.order.number.label", locale)).thenReturn("numberLabel");
        when(translationService.translate("orders.order.name.label", locale)).thenReturn("nameLabel");
        when(translationService.translate("workPlans.workPlan.report.colums.product", locale)).thenReturn("productLabel");
        when(translationService.translate("orders.order.plannedQuantity.label", locale)).thenReturn("quantityLabel");
        when(translationService.translate("orders.order.dateTo.label", locale)).thenReturn("dateLabel");

        // when
        List<String> ordersTableHeader = workPlanPdfService.prepareOrdersTableHeader(document, workPlan, locale);

        // then
        Iterator<String> iterator = ordersTableHeader.iterator();
        assertEquals("numberLabel", iterator.next());
        assertEquals("nameLabel", iterator.next());
        assertEquals("productLabel", iterator.next());
        assertEquals("quantityLabel", iterator.next());
        assertEquals("dateLabel", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void shouldAddOrdersToTheTableCorrectly() throws DocumentException {
        // given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.DATE_FORMAT);

        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());

        PdfPTable table = mock(PdfPTable.class);
        PdfPCell defaultCell = mock(PdfPCell.class);
        Entity order = mock(Entity.class);
        Entity product = mock(Entity.class);
        Date now = new Date();
        BigDecimal quantity = new BigDecimal(10);
        List<Entity> orders = new ArrayList<Entity>();
        orders.add(order);

        when(table.getDefaultCell()).thenReturn(defaultCell);

        when(order.getField("number")).thenReturn("orderNumber");
        when(order.getField("name")).thenReturn("orderName");
        when(order.getField("plannedQuantity")).thenReturn(quantity);
        when(order.getField("product")).thenReturn(product);

        when(product.getField("number")).thenReturn("productNumber");
        when(product.getField("name")).thenReturn("productName");
        when(product.getField("unit")).thenReturn("productUnit");

        when(order.getField("dateTo")).thenReturn(now);

        // when
        workPlanPdfService.addOrderSeries(table, orders, df);

        // then
        ArgumentCaptor<Phrase> phrase = ArgumentCaptor.forClass(Phrase.class);
        verify(table, times(5)).addCell(phrase.capture());

        assertEquals("orderNumber", phrase.getAllValues().get(0).getContent());
        assertEquals("orderName", phrase.getAllValues().get(1).getContent());
        assertEquals("productName (productNumber)", phrase.getAllValues().get(2).getContent());
        assertEquals(df.format(quantity) + " productUnit", phrase.getAllValues().get(3).getContent());
        assertEquals(simpleDateFormat.format(now), phrase.getAllValues().get(4).getContent());

        assertEquals(PdfUtil.getArialRegular9Dark(), phrase.getAllValues().get(0).getFont());
        assertEquals(PdfUtil.getArialRegular9Dark(), phrase.getAllValues().get(1).getFont());
        assertEquals(PdfUtil.getArialRegular9Dark(), phrase.getAllValues().get(2).getFont());
        assertEquals(PdfUtil.getArialRegular9Dark(), phrase.getAllValues().get(3).getFont());
        assertEquals(PdfUtil.getArialRegular9Dark(), phrase.getAllValues().get(4).getFont());
    }

    @Test
    public void shouldNotTryToAddProductToTheOrdersTableIfThereIsNone() throws DocumentException {
        // given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.DATE_FORMAT);

        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());

        PdfPTable table = mock(PdfPTable.class);
        PdfPCell defaultCell = mock(PdfPCell.class);
        Entity order = mock(Entity.class);
        Date now = new Date();
        BigDecimal quantity = new BigDecimal(10);
        List<Entity> orders = new ArrayList<Entity>();
        orders.add(order);

        when(table.getDefaultCell()).thenReturn(defaultCell);

        when(order.getField("number")).thenReturn("orderNumber");
        when(order.getField("name")).thenReturn("orderName");
        when(order.getField("plannedQuantity")).thenReturn(quantity);

        when(order.getField("dateTo")).thenReturn(now);

        // when
        workPlanPdfService.addOrderSeries(table, orders, df);

        // then
        ArgumentCaptor<Phrase> phrase = ArgumentCaptor.forClass(Phrase.class);
        verify(table, times(5)).addCell(phrase.capture());

        assertEquals("orderNumber", phrase.getAllValues().get(0).getContent());
        assertEquals("orderName", phrase.getAllValues().get(1).getContent());
        assertEquals("", phrase.getAllValues().get(2).getContent());
        assertEquals(df.format(quantity), phrase.getAllValues().get(3).getContent());
        assertEquals(simpleDateFormat.format(now), phrase.getAllValues().get(4).getContent());

        assertEquals(PdfUtil.getArialRegular9Dark(), phrase.getAllValues().get(0).getFont());
        assertEquals(PdfUtil.getArialRegular9Dark(), phrase.getAllValues().get(1).getFont());
        assertEquals(PdfUtil.getArialRegular9Dark(), phrase.getAllValues().get(2).getFont());
        assertEquals(PdfUtil.getArialRegular9Dark(), phrase.getAllValues().get(3).getFont());
        assertEquals(PdfUtil.getArialRegular9Dark(), phrase.getAllValues().get(4).getFont());
    }

    @Test
    public void shouldPrepareOperationsTitleForAllOperations() {
        // given
        when(workPlan.getStringField("type")).thenReturn(WorkPlanType.ALL_OPERATIONS.getStringValue());

        // when
        String titleKey = workPlanPdfService.getOperationsTitle(workPlan);

        // then
        assertEquals("workPlans.workPlan.report.title.allOperations", titleKey);
    }
}
