package com.qcadoo.mes.productionPerShift.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.orders.constants.OrderFields;
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
import com.qcadoo.view.api.utils.TimeConverterService;

public class ProductionPerShiftDetailsHooksTest {

    private ProductionPerShiftDetailsHooks hooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private ComponentState componentState, lookup, producesInput;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition ddTIOC, orderDD;

    @Mock
    private Entity tioc, toc, prodComp, prod, order, progressFroDay;

    @Mock
    private EntityTreeNode root;

    @Mock
    private EntityTree techInstOperComps;

    @Mock
    private EntityList progressForDays;

    @Mock
    private FormComponent form;

    @Mock
    private FieldComponent operation, plannedProgressType, plannedDate, corretedDate, plannedProgressCorrectionType,
            plannedProgressCorrectionComment;

    @Mock
    private SearchResult result;

    @Mock
    private SearchCriteriaBuilder builder;

    @Mock
    private TechnologyService technologyService;

    @Mock
    private TimeConverterService timeConverterService;

    @Mock
    AwesomeDynamicListComponent adl;

    private Long orderId;

    @Before
    public void init() {
        hooks = new ProductionPerShiftDetailsHooks();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(hooks, "technologyService", technologyService);
        ReflectionTestUtils.setField(hooks, "dataDefinitionService", dataDefinitionService);
        ReflectionTestUtils.setField(hooks, "timeConverterService", timeConverterService);

        when(dataDefinitionService.get("orders", "order")).thenReturn(orderDD);
        when(dataDefinitionService.get("technologies", "technologyInstanceOperationComponent")).thenReturn(ddTIOC);
        orderId = 1L;
        when(view.getComponentByReference("order")).thenReturn(lookup);
        when(lookup.getFieldValue()).thenReturn(orderId);
    }

    @Test
    public void shouldFillProducesFieldAfterSelection() {
        // given
        Long id = 3L;
        String prodName = "asdf";

        given(view.getComponentByReference("productionPerShiftOperation")).willReturn(lookup);
        given(view.getComponentByReference("produces")).willReturn(producesInput);

        given(lookup.getFieldValue()).willReturn(id);
        given(ddTIOC.get(id)).willReturn(tioc);

        given(tioc.getBelongsToField("technologyOperationComponent")).willReturn(toc);
        given(technologyService.getMainOutputProductComponent(toc)).willReturn(prodComp);
        given(prodComp.getBelongsToField("product")).willReturn(prod);
        given(prod.getStringField("name")).willReturn(prodName);

        // when
        hooks.fillProducedField(view);

        // then
        verify(producesInput).setFieldValue(prodName);
    }

    @Test
    public void shouldAddRootForOperation() throws Exception {
        // given
        Long entityId = 1L;
        Long operationId = 2L;
        Long rootId = 2L;
        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(entityId);
        when(orderDD.get(entityId)).thenReturn(order);
        when(order.getTreeField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS)).thenReturn(techInstOperComps);
        when(techInstOperComps.isEmpty()).thenReturn(false);
        when(techInstOperComps.getRoot()).thenReturn(root);
        when(view.getComponentByReference("productionPerShiftOperation")).thenReturn(operation);
        when(operation.getFieldValue()).thenReturn(null);
        when(view.getComponentByReference("progressForDaysADL")).thenReturn(adl);
        when(view.getComponentByReference("plannedProgressType")).thenReturn(plannedProgressType);
        when(plannedProgressType.getFieldValue()).thenReturn("01planned");
        when(order.getStringField("state")).thenReturn("01pending");
        when(operation.getFieldValue()).thenReturn(operationId);
        when(root.getId()).thenReturn(rootId);
        // when
        hooks.addRootForOperation(view);
        // then

        Assert.assertEquals(rootId, operationId);
    }

    @Test
    public void shouldReturnNullWhenEntityIdIsNull() throws Exception {
        // given
        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(null);

        // when
        hooks.addRootForOperation(view);
        // then
    }

    @Test
    public void shouldReturnWhenTechOperCompsIsEmpty() throws Exception {
        // given
        Long entityId = 1L;
        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(entityId);
        when(orderDD.get(entityId)).thenReturn(order);
        when(order.getTreeField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS)).thenReturn(techInstOperComps);
        when(techInstOperComps.isEmpty()).thenReturn(true);

        // when
        hooks.addRootForOperation(view);
        // then
    }

    @Test
    public void shouldDisabledPlannedProgressTypeForPendingOrder() throws Exception {
        // given

        when(orderDD.get(orderId)).thenReturn(order);
        when(order.getStringField("state")).thenReturn("01pending");
        when(view.getComponentByReference("plannedProgressType")).thenReturn(plannedProgressType);
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
        when(view.getComponentByReference("order")).thenReturn(lookup);
        when(lookup.getFieldValue()).thenReturn(orderId);
        when(orderDD.get(orderId)).thenReturn(order);
        when(order.getStringField("state")).thenReturn("03inProgress");
        when(view.getComponentByReference("plannedProgressType")).thenReturn(plannedProgressType);
        // when
        hooks.disablePlannedProgressTypeForPendingOrder(view);
        // then
        verify(plannedProgressType).setEnabled(true);
    }

    @Test
    public void shouldReturnTIOCFromDB() throws Exception {
        // given
        Long operationId = 1L;
        when(view.getComponentByReference("productionPerShiftOperation")).thenReturn(componentState);
        when(componentState.getFieldValue()).thenReturn(operationId);
        when(ddTIOC.get(operationId)).thenReturn(tioc);

        // when
        Entity techInstOperComp = hooks.getTiocFromOperationLookup(view);
        // then
        Assert.assertEquals(tioc, techInstOperComp);
    }

    @Test
    public void shouldReturnNullWhenTiocIdIsNull() throws Exception {
        // given
        when(view.getComponentByReference("productionPerShiftOperation")).thenReturn(componentState);
        when(componentState.getFieldValue()).thenReturn(null);

        // when
        Entity techInstOperComp = hooks.getTiocFromOperationLookup(view);
        // then
        Assert.assertEquals(null, techInstOperComp);
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
        Long operationId = 1L;
        String corrected = "02corrected";
        when(view.getComponentByReference("progressForDaysADL")).thenReturn(adl);
        when(view.getComponentByReference("productionPerShiftOperation")).thenReturn(operation);
        when(operation.getFieldValue()).thenReturn(operationId);
        when(ddTIOC.get(operationId)).thenReturn(tioc);
        when(view.getComponentByReference("plannedProgressType")).thenReturn(plannedProgressType);
        when(plannedProgressType.getFieldValue()).thenReturn("02corrected");
        when(tioc.getHasManyField("progressForDays")).thenReturn(progressForDays);

        when(plannedProgressType.getFieldValue()).thenReturn(corrected);
        SearchCriterion criterion = SearchRestrictions.eq("corrected", corrected);

        when(progressForDays.find()).thenReturn(builder);
        when(builder.add(criterion)).thenReturn(builder);
        when(builder.list()).thenReturn(result);
        when(result.getEntities()).thenReturn(progressForDays);
        // when
        hooks.fillProgressForDays(view);
        // then
    }

}
