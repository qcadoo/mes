package com.qcadoo.mes.productionPerShift.hooks;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.PPSHelper;
import com.qcadoo.mes.productionPerShift.constants.PlannedProgressType;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.TimeConverterService;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class ProductionPerShiftDetailsHooksTest {

    private ProductionPerShiftDetailsHooks hooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private ComponentState lookup, producesInput;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition ddTIOC, orderDD;

    @Mock
    private Entity tioc, toc, prodComp, prod, order;

    @Mock
    private EntityTreeNode root;

    @Mock
    private EntityTree techInstOperComps;

    @Mock
    private EntityList progressForDays;

    @Mock
    private FormComponent form, form1;

    @Mock
    private FieldComponent operation, plannedProgressType, plannedDate, corretedDate, plannedProgressCorrectionType,
            plannedProgressCorrectionComment, unitField, setRoot;

    @Mock
    private SearchResult result;

    @Mock
    private SearchCriteriaBuilder builder;

    @Mock
    private TechnologyService technologyService;

    @Mock
    private TimeConverterService timeConverterService;

    @Mock
    private List<FormComponent> forms, forms1;

    @Mock
    Iterator<FormComponent> iteratorForm, iteratorForm1;

    @Mock
    AwesomeDynamicListComponent adl, dprogress;

    @Mock
    private PPSHelper helper;

    private Long orderId;

    @Before
    public void init() {
        hooks = new ProductionPerShiftDetailsHooks();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(hooks, "technologyService", technologyService);
        ReflectionTestUtils.setField(hooks, "helper", helper);
        ReflectionTestUtils.setField(hooks, "timeConverterService", timeConverterService);

        when(dataDefinitionService.get("orders", "order")).thenReturn(orderDD);
        when(dataDefinitionService.get("technologies", "technologyInstanceOperationComponent")).thenReturn(ddTIOC);
        when(helper.getOrderFromLookup(view)).thenReturn(order);
    }

    @Test
    public void shouldFillProducesFieldAfterSelection() {
        // given
        String prodName = "asdf";
        String unit = "PLN";

        when(helper.getTiocFromOperationLookup(view)).thenReturn(tioc);
        when(view.getComponentByReference("produces")).thenReturn(producesInput);
        when(producesInput.getFieldValue()).thenReturn("");

        when(tioc.getBelongsToField("technologyOperationComponent")).thenReturn(toc);
        when(technologyService.getMainOutputProductComponent(toc)).thenReturn(prodComp);
        when(prodComp.getBelongsToField("product")).thenReturn(prod);
        when(prod.getStringField("name")).thenReturn(prodName);
        when(view.getComponentByReference("progressForDaysADL")).thenReturn(adl);
        when(adl.getFormComponents()).thenReturn(forms);
        when(forms.iterator()).thenReturn(iteratorForm);
        when(iteratorForm.next()).thenReturn(form);
        when(iteratorForm.hasNext()).thenReturn(true, false);

        when(form.findFieldComponentByName("dailyProgressADL")).thenReturn(dprogress);
        when(dprogress.getFormComponents()).thenReturn(forms1);
        when(forms1.iterator()).thenReturn(iteratorForm1);
        when(iteratorForm1.next()).thenReturn(form1);
        when(iteratorForm1.hasNext()).thenReturn(true, false);

        when(prod.getStringField("unit")).thenReturn(unit);
        when(form1.findFieldComponentByName("unit")).thenReturn(unitField);
        // when
        hooks.fillProducedField(view);

        // then
        Mockito.verify(unitField).setFieldValue(unit);
    }

    @Test
    public void shouldAddRootForOperation() throws Exception {
        // given
        Long entityId = 1L;
        Long operationId = 2L;
        Long rootId = 2L;
        when(view.getComponentByReference("setRoot")).thenReturn(setRoot);
        when(setRoot.getFieldValue()).thenReturn("");
        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(entityId);

        when(order.getTreeField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS)).thenReturn(techInstOperComps);
        when(techInstOperComps.isEmpty()).thenReturn(false);
        when(techInstOperComps.getRoot()).thenReturn(root);
        when(view.getComponentByReference("productionPerShiftOperation")).thenReturn(operation);
        when(operation.getFieldValue()).thenReturn(null);
        when(view.getComponentByReference("progressForDaysADL")).thenReturn(adl);
        when(view.getComponentByReference("plannedProgressType")).thenReturn(plannedProgressType);
        when(plannedProgressType.getFieldValue()).thenReturn("01planned");
        when(order.getStringField("state")).thenReturn("01pending");
        when(operation.getFieldValue()).thenReturn(null);
        when(root.getId()).thenReturn(rootId);
        // when
        hooks.addRootForOperation(view);
        // then

        Assert.assertEquals(rootId, operationId);
    }

    @Test
    public void shouldDisabledPlannedProgressTypeForPendingOrder() throws Exception {
        // given
        String empty = "";
        when(orderDD.get(orderId)).thenReturn(order);
        when(order.getStringField("state")).thenReturn("01pending");
        when(view.getComponentByReference("plannedProgressType")).thenReturn(plannedProgressType);
        when(plannedProgressType.getFieldValue()).thenReturn(empty);
        // when
        hooks.disablePlannedProgressTypeForPendingOrder(view);
        // then
        verify(plannedProgressType).setFieldValue("01planned");
        verify(plannedProgressType).setEnabled(false);
    }

    @Test
    public void shouldEnabledPlannedProgressTypeForInProgressOrder() throws Exception {
        // given
        Long orderId = 1L;
        String empty = "";
        when(view.getComponentByReference("order")).thenReturn(lookup);
        when(lookup.getFieldValue()).thenReturn(orderId);
        when(orderDD.get(orderId)).thenReturn(order);
        when(order.getStringField("state")).thenReturn("03inProgress");
        when(view.getComponentByReference("plannedProgressType")).thenReturn(plannedProgressType);
        when(plannedProgressType.getFieldValue()).thenReturn(empty);
        // when
        hooks.disablePlannedProgressTypeForPendingOrder(view);
        // then
        verify(plannedProgressType).setEnabled(true);
    }

    @Test
    public void shouldSetOrderStartDatesWhenPlannedDateExists() throws Exception {
        // given
        when(orderDD.get(orderId)).thenReturn(order);
        when(view.getComponentByReference("orderPlannedStartDate")).thenReturn(plannedDate);
        when(view.getComponentByReference("orderCorrectedStartDate")).thenReturn(corretedDate);

        Date planned = new Date();
        when(order.getField("dateFrom")).thenReturn(planned);
        when(order.getField("correctedDateFrom")).thenReturn(null);
        // when
        hooks.setOrderStartDate(view);
        // then
        verify(plannedDate).setFieldValue(Mockito.any(Date.class));
    }

    @Test
    public void shouldSetOrderStartDatesWhenPlannedAndCorrectedDateExists() throws Exception {
        // given
        when(orderDD.get(orderId)).thenReturn(order);
        when(view.getComponentByReference("orderPlannedStartDate")).thenReturn(plannedDate);
        when(view.getComponentByReference("orderCorrectedStartDate")).thenReturn(corretedDate);

        Date planned = new Date();
        Date corrected = new Date();
        when(order.getField("dateFrom")).thenReturn(planned);
        when(order.getField("correctedDateFrom")).thenReturn(corrected);
        // when
        hooks.setOrderStartDate(view);
        // then
        verify(plannedDate).setFieldValue(Mockito.any(Date.class));
        verify(corretedDate).setFieldValue(Mockito.any(Date.class));
    }

    @Test
    public void shouldDisableReasonOfCorrectionsFieldForPlannedProgressType() throws Exception {
        // given
        when(view.getComponentByReference("plannedProgressType")).thenReturn(plannedProgressType);
        when(view.getComponentByReference("plannedProgressCorrectionType")).thenReturn(plannedProgressCorrectionType);
        when(view.getComponentByReference("plannedProgressCorrectionComment")).thenReturn(plannedProgressCorrectionComment);
        when(plannedProgressType.getFieldValue()).thenReturn("01planned");
        // when
        hooks.disableReasonOfCorrection(view);
        // then
        verify(plannedProgressCorrectionType).setEnabled(false);
        verify(plannedProgressCorrectionComment).setEnabled(false);
    }

    @Test
    public void shouldEnableReasonOfCorrectionsFieldForCorrectedProgressType() throws Exception {
        // given
        when(view.getComponentByReference("plannedProgressType")).thenReturn(plannedProgressType);
        when(view.getComponentByReference("plannedProgressCorrectionType")).thenReturn(plannedProgressCorrectionType);
        when(view.getComponentByReference("plannedProgressCorrectionComment")).thenReturn(plannedProgressCorrectionComment);
        when(plannedProgressType.getFieldValue()).thenReturn("02corrected");
        // when
        hooks.disableReasonOfCorrection(view);
        // then
        verify(plannedProgressCorrectionType).setEnabled(true);
        verify(plannedProgressCorrectionComment).setEnabled(true);
    }

    @Test
    @Ignore
    public void shouldFillProgressForDaysSelectedOperation() throws Exception {
        // given
        String corrected = "02corrected";
        when(view.getComponentByReference("progressForDaysADL")).thenReturn(adl);
        when(helper.getTiocFromOperationLookup(view)).thenReturn(tioc);

        when(view.getComponentByReference("plannedProgressType")).thenReturn(plannedProgressType);
        when(tioc.getHasManyField("progressForDays")).thenReturn(progressForDays);
        //
        when(plannedProgressType.getFieldValue()).thenReturn(corrected);
        SearchCriterion criterion = SearchRestrictions.eq("corrected",
                corrected.equals(PlannedProgressType.CORRECTED.getStringValue()));
        //
        when(progressForDays.find()).thenReturn(builder);
        when(builder.add(criterion)).thenReturn(builder);
        when(builder.list()).thenReturn(result);
        when(result.getEntities()).thenReturn(progressForDays);
        // when
        hooks.fillProgressForDays(view);
        // then
        Mockito.verify(adl).setFieldValue(progressForDays);
    }

    @Test
    public void shouldDisabledButtonWhenProgressTypeIsPlanne() throws Exception {
        // given
        WindowComponentState windowComponent = mock(WindowComponentState.class);
        Ribbon ribbon = mock(Ribbon.class);
        RibbonGroup progressSelectedOperation = mock(RibbonGroup.class);
        RibbonActionItem clearButton = mock(RibbonActionItem.class);
        RibbonActionItem copyButton = mock(RibbonActionItem.class);

        when(view.getComponentByReference("window")).thenReturn(windowComponent);
        when(windowComponent.getRibbon()).thenReturn(ribbon);
        when(ribbon.getGroupByName("progress")).thenReturn(progressSelectedOperation);
        when(progressSelectedOperation.getItemByName("clear")).thenReturn(clearButton);
        when(progressSelectedOperation.getItemByName("copyFromPlanned")).thenReturn(copyButton);
        when(view.getComponentByReference("plannedProgressType")).thenReturn(plannedProgressType);
        when(plannedProgressType.getFieldValue()).thenReturn("01planned");

        // when
        hooks.changedButtonState(view);

        // then
        Mockito.verify(clearButton).setEnabled(false);
        Mockito.verify(copyButton).setEnabled(false);
    }

    @Test
    public void shouldEnabledButtonWhenProgressTypeIsCorrected() throws Exception {
        // given
        WindowComponentState windowComponent = mock(WindowComponentState.class);
        Ribbon ribbon = mock(Ribbon.class);
        RibbonGroup progressSelectedOperation = mock(RibbonGroup.class);
        RibbonActionItem clearButton = mock(RibbonActionItem.class);
        RibbonActionItem copyButton = mock(RibbonActionItem.class);

        when(view.getComponentByReference("window")).thenReturn(windowComponent);
        when(windowComponent.getRibbon()).thenReturn(ribbon);
        when(ribbon.getGroupByName("progress")).thenReturn(progressSelectedOperation);
        when(progressSelectedOperation.getItemByName("clear")).thenReturn(clearButton);
        when(progressSelectedOperation.getItemByName("copyFromPlanned")).thenReturn(copyButton);
        when(view.getComponentByReference("plannedProgressType")).thenReturn(plannedProgressType);
        when(plannedProgressType.getFieldValue()).thenReturn("02corrected");

        // when
        hooks.changedButtonState(view);

        // then
        Mockito.verify(clearButton).setEnabled(true);
        Mockito.verify(copyButton).setEnabled(true);
    }

}
