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
package com.qcadoo.mes.qualityControls;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.qualityControls.constants.QualityControlsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.model.internal.api.DataAccessService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@RunWith(PowerMockRunner.class)
public class QualityControlServiceTest {

    private SecurityService securityService = null;

    private QualityControlService qualityControlService;

    private DataDefinitionService dataDefinitionService;

    private QualityControlForNumberService qualityControlForNumber;

    private NumberService numberService;

    private MathContext mathContext;

    private static final Integer DIGITS_NUMBER = 6;

    @Before
    public void init() {
        securityService = mock(SecurityService.class);
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        qualityControlService = new QualityControlService();
        numberService = mock(NumberService.class);
        qualityControlForNumber = mock(QualityControlForNumberService.class);
        setField(qualityControlService, "dataDefinitionService", dataDefinitionService);
        setField(qualityControlService, "securityService", securityService);
        setField(qualityControlService, "qualityControlForNumber", qualityControlForNumber);
        setField(qualityControlService, "numberService", numberService);

        mathContext = MathContext.DECIMAL64;
        when(numberService.getMathContext()).thenReturn(mathContext);
    }

    @Test
    public void shouldSetRequiredOnCommentForControlResult() {
        // given
        FormComponent form = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);

        FieldComponent comment = mock(FieldComponent.class);
        FieldComponent controlResult = mock(FieldComponent.class);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("comment")).willReturn(comment);
        given(state.getComponentByReference("controlResult")).willReturn(controlResult);
        given(state.getComponentByReference("controlResult").getFieldValue()).willReturn("03objection");
        given(state.getComponentByReference("form")).willReturn(form);

        // when
        qualityControlService.checkIfCommentIsRequiredBasedOnResult(state);

        // then
        verify(comment).setRequired(true);
        verify(state, atLeastOnce()).getComponentByReference(anyString());
        verifyNoMoreInteractions(state);
    }

    @Test
    public void shouldSetRequiredOnCommentForDefects() {
        // given
        FormComponent form = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);

        FieldComponent comment = mock(FieldComponent.class);
        FieldComponent acceptedDefectsQuantity = mock(FieldComponent.class);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("comment")).willReturn(comment);
        given(state.getComponentByReference("acceptedDefectsQuantity")).willReturn(acceptedDefectsQuantity);
        given(state.getComponentByReference("acceptedDefectsQuantity").getFieldValue()).willReturn(new BigDecimal("12"));
        given(state.getComponentByReference("form")).willReturn(form);

        // when
        qualityControlService.checkIfCommentIsRequiredBasedOnDefects(state);

        // then
        verify(comment).setRequired(true);
        verify(state, atLeastOnce()).getComponentByReference(anyString());
        verifyNoMoreInteractions(state);
    }

    @Test
    public void shouldSetRequiredOnCommentAfterSelectChange() {
        // given
        FieldComponent resultType = mock(FieldComponent.class);
        FieldComponent comment = mock(FieldComponent.class);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(resultType.getFieldValue()).willReturn("03objection");
        given(state.getComponentByReference("comment")).willReturn(comment);

        // when
        qualityControlService.checkQualityControlResult(state, resultType, new String[] {});

        // then
        verify(comment).setRequired(true);
    }

    @Test
    public void shouldSetErrorOnCommentFieldForResultType() {
        // given
        Entity entity = mock(Entity.class);
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);

        given(entity.getStringField("qualityControlType")).willReturn("qualityControlsForOrder");
        given(entity.getStringField("controlResult")).willReturn("03objection");
        given(entity.getField("comment")).willReturn(null);

        // when
        qualityControlService.checkIfCommentForResultOrQuantityIsReq(dataDefinition, entity);

        // then
        verify(entity).addGlobalError("qcadooView.validate.global.error.custom");
        verify(entity).addError(dataDefinition.getField("comment"),
                "qualityControls.quality.control.validate.global.error.comment");
    }

    @Test
    public void shouldSetErrorOnCommentFieldForAcceptedDefectsQuantity() {
        // given
        Entity entity = mock(Entity.class);
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);

        given(entity.getStringField("qualityControlType")).willReturn("qualityControlsForUnit");
        given(entity.getDecimalField("acceptedDefectsQuantity")).willReturn(new BigDecimal("1"));
        given(entity.getField("comment")).willReturn(null);

        // when
        qualityControlService.checkIfCommentForResultOrQuantityIsReq(dataDefinition, entity);

        // then
        verify(entity).addGlobalError("qcadooView.validate.global.error.custom");
        verify(entity).addError(dataDefinition.getField("comment"),
                "qualityControls.quality.control.validate.global.error.comment");
    }

    @Test
    public void shouldCloseQualityControlForEntityComponentState() {
        // given
        FormComponent state = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        FieldComponent controlResult = mock(FieldComponent.class);
        FieldComponent closed = mock(FieldComponent.class);
        FieldComponent staff = mock(FieldComponent.class);
        FieldComponent date = mock(FieldComponent.class);
        FieldComponent qualityControlType = mock(FieldComponent.class);

        given(state.getFieldValue()).willReturn(7L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(viewDefinitionState.getComponentByReference("controlResult")).willReturn(controlResult);
        given(viewDefinitionState.getComponentByReference("closed")).willReturn(closed);
        given(viewDefinitionState.getComponentByReference("staff")).willReturn(staff);
        given(viewDefinitionState.getComponentByReference("date")).willReturn(date);
        given(viewDefinitionState.getComponentByReference("qualityControlType")).willReturn(qualityControlType);
        given(((FieldComponent) viewDefinitionState.getComponentByReference("qualityControlType")).getFieldValue()).willReturn(
                "qualityControlsForUnit");
        given(qualityControlType.getFieldValue().equals("qualityControlsForOrder")).willReturn(false);
        given(qualityControlType.getFieldValue().equals("qualityControlsForOperation")).willReturn(false);
        given(controlResult.getFieldValue()).willReturn("03objection");
        given(securityService.getCurrentUserName()).willReturn("admin");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityForOrder" });

        // then
        verify(closed).setFieldValue(true);
    }

    @Test
    public void shouldCloseQualityControlForIGridComponentState() {
        // given
        GridComponent state = mock(GridComponent.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        Entity qualityControl = mock(Entity.class);
        DataDefinition qualityControlDD = mock(DataDefinition.class);
        FieldComponent controlResult = mock(FieldComponent.class);
        given(qualityControl.getField("qualityControlType")).willReturn("qualityControlsForUnit");
        given(
                dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                        QualityControlsConstants.MODEL_QUALITY_CONTROL)).willReturn(qualityControlDD);
        given(qualityControlDD.get(7L)).willReturn(qualityControl);
        given(viewDefinitionState.getComponentByReference("controlResult")).willReturn(controlResult);
        given(qualityControl.getField("controlResult")).willReturn("01correct");
        given(controlResult.getFieldValue()).willReturn("01correct");
        given(state.getFieldValue()).willReturn(7L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(securityService.getCurrentUserName()).willReturn("admin");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityControl" });

        // then
        verify(qualityControl).setField("staff", "admin");
        verify(qualityControl).setField("closed", true);
        verify(state).addMessage("qualityControls.quality.control.closed.success", MessageType.SUCCESS);
    }

    @Test
    public void shouldAddFailureMessageOnEmptyControlResultTypeForFormComponent() {
        // given
        FormComponent state = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        FieldComponent controlResult = mock(FieldComponent.class);
        Entity qualityControl = mock(Entity.class);
        DataDefinition qualityControlDD = mock(DataDefinition.class);
        FieldComponent qualityControlType = mock(FieldComponent.class);

        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(state.getFieldValue()).willReturn(7L);
        given(qualityControl.getField("controlResult")).willReturn("");
        given(
                dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                        QualityControlsConstants.MODEL_QUALITY_CONTROL)).willReturn(qualityControlDD);
        given(qualityControlDD.get(7L)).willReturn(qualityControl);
        given(viewDefinitionState.getComponentByReference("controlResult")).willReturn(controlResult);
        given(controlResult.getFieldValue()).willReturn(null);
        given(viewDefinitionState.getComponentByReference("qualityControlType")).willReturn(qualityControlType);
        given(((FieldComponent) viewDefinitionState.getComponentByReference("qualityControlType")).getFieldValue()).willReturn(
                "qualityControlsForOrder");
        given(qualityControlType.getFieldValue()).willReturn("qualityControlsForOrder");
        given(qualityControlType.getFieldValue()).willReturn("qualityControlsForOperation");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityControls" });

        // then
        verify(controlResult).addMessage("qualityControls.quality.control.result.missing", MessageType.FAILURE);
        verify(state).addMessage("qualityControls.quality.control.result.missing", MessageType.FAILURE);
    }

    @Test
    public void shouldAddFailureMessageOnEmptyControlResultTypeForGridComponent() {
        // given
        GridComponent state = mock(GridComponent.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        FieldComponent controlResult = mock(FieldComponent.class);
        Entity qualityControl = mock(Entity.class);
        DataDefinition qualityControlDD = mock(DataDefinition.class);
        FieldDefinition controlResultField = mock(FieldDefinition.class);
        FieldComponent qualityControlType = mock(FieldComponent.class);

        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(qualityControlDD.getField("controlResult")).willReturn(controlResultField);
        given(state.getFieldValue()).willReturn(7L);
        given(qualityControl.getStringField("controlResult")).willReturn("");
        given(
                dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                        QualityControlsConstants.MODEL_QUALITY_CONTROL)).willReturn(qualityControlDD);
        given(qualityControlDD.get(7L)).willReturn(qualityControl);
        given(viewDefinitionState.getComponentByReference("controlResult")).willReturn(controlResult);
        given(controlResult.getFieldValue()).willReturn(null);
        given(viewDefinitionState.getComponentByReference("qualityControlType")).willReturn(qualityControlType);
        given(qualityControl.getStringField("qualityControlType")).willReturn("qualityControlsForOrder");
        given(qualityControlType.getFieldValue()).willReturn("qualityControlsForOrder");
        given(qualityControlType.getFieldValue()).willReturn("qualityControlsForOperation");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityControls" });

        // then
        verify(state).addMessage("qualityControls.quality.control.result.missing", MessageType.FAILURE);
    }

    @Test
    public void shouldAddFailureMessageOnNoValueForFormComponent() {
        // given
        FormComponent state = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        given(state.getFieldValue()).willReturn(null);
        given(state.getLocale()).willReturn(Locale.ENGLISH);

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityControls" });

        // then
        verify(state).addMessage("qcadooView.form.entityWithoutIdentifier", MessageType.FAILURE);
    }

    @Test
    public void shouldAddFailureMessageOnNoValueForNonFormComponent() {
        // given
        GridComponent state = mock(GridComponent.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        given(state.getFieldValue()).willReturn(null);
        given(state.getLocale()).willReturn(Locale.ENGLISH);

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityControls" });

        // then
        verify(state).addMessage("qcadooView.grid.noRowSelectedError", MessageType.FAILURE);
    }

    @Test
    public void shouldGenerateQualityControlForBatch() {
        // given
        DataDefinition genealogyDataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);

        List<Entity> genealogies = new ArrayList<Entity>();
        Entity genealogy = new DefaultEntity(genealogyDataDefinition);
        genealogy.setField("batch", "1");
        genealogies.add(genealogy);

        GridComponent state = mock(GridComponent.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        DataDefinition orderDataDefinition = mock(DataDefinition.class);
        DataDefinition qualityForBatchDataDefinition = mock(DataDefinition.class);

        Entity order = mock(Entity.class);
        Entity technology = mock(Entity.class);

        SearchCriteriaBuilder searchCriteria = mock(SearchCriteriaBuilder.class, RETURNS_DEEP_STUBS);

        given(dataDefinitionService.get("orders", "order")).willReturn(orderDataDefinition);
        given(
                dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                        QualityControlsConstants.MODEL_QUALITY_CONTROL)).willReturn(qualityForBatchDataDefinition);
        given(orderDataDefinition.get(7L)).willReturn(order);
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(technology.getStringField("qualityControlType")).willReturn("01forBatch");
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(state.getFieldValue()).willReturn(7L);
        given(dataDefinitionService.get("genealogies", "genealogy")).willReturn(genealogyDataDefinition);
        given(genealogyDataDefinition.find().belongsTo("order", 7L)).willReturn(searchCriteria);
        given(searchCriteria.list().getEntities()).willReturn(genealogies);
        given(
                qualityControlForNumber.generateNumber(QualityControlsConstants.PLUGIN_IDENTIFIER,
                        QualityControlsConstants.MODEL_QUALITY_CONTROL, DIGITS_NUMBER, "qualityControlForBatch")).willReturn("1");
        given(order.getDecimalField("plannedQuantity")).willReturn(new BigDecimal("1"));
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(order.getBelongsToField("technology").getField("qualityControlInstruction")).willReturn("test");

        DataAccessService dataAccessService = mock(DataAccessService.class);
        given(dataAccessService.convertToDatabaseEntity(Mockito.any(Entity.class))).willReturn(new Object());

        DataDefinition qualityControlDD = mock(DataDefinition.class, Mockito.RETURNS_DEEP_STUBS);
        given(
                dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                        QualityControlsConstants.MODEL_QUALITY_CONTROL)).willReturn(qualityControlDD);
        given(qualityControlDD.find().add(Mockito.any(SearchCriterion.class)).list().getTotalNumberOfEntities()).willReturn(0);

        SearchRestrictions searchRestrictions = new SearchRestrictions();
        ReflectionTestUtils.setField(searchRestrictions, "dataAccessService", dataAccessService);

        // when
        qualityControlService.generateQualityControl(viewDefinitionState, state, new String[] { "qualityControls" });

        // then
        verify(state).addMessage("qualityControls.qualityControls.generated.success", MessageType.SUCCESS);
    }

    @Test
    public void shouldGenerateQualityControlForUnit() {
        // given
        GridComponent state = mock(GridComponent.class);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        DataDefinition orderDataDefinition = mock(DataDefinition.class);
        Entity order = mock(Entity.class);

        Entity technology = mock(Entity.class);

        given(dataDefinitionService.get("orders", "order")).willReturn(orderDataDefinition);
        given(orderDataDefinition.get(7L)).willReturn(order);
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(technology.getStringField("qualityControlType")).willReturn("02forUnit");
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(state.getFieldValue()).willReturn(7L);
        given(technology.getDecimalField("unitSamplingNr")).willReturn(new BigDecimal("2"));
        given(order.getDecimalField("plannedQuantity")).willReturn(new BigDecimal("5"));
        given(
                qualityControlForNumber.generateNumber(QualityControlsConstants.PLUGIN_IDENTIFIER,
                        QualityControlsConstants.MODEL_QUALITY_CONTROL, DIGITS_NUMBER, "qualityControlForUnit")).willReturn("1");
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(order.getBelongsToField("technology").getStringField("qualityControlInstruction")).willReturn("test");

        DataAccessService dataAccessService = mock(DataAccessService.class);
        given(dataAccessService.convertToDatabaseEntity(Mockito.any(Entity.class))).willReturn(new Object());

        DataDefinition qualityControlDD = mock(DataDefinition.class, Mockito.RETURNS_DEEP_STUBS);
        given(
                dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                        QualityControlsConstants.MODEL_QUALITY_CONTROL)).willReturn(qualityControlDD);
        given(qualityControlDD.find().add(Mockito.any(SearchCriterion.class)).list().getTotalNumberOfEntities()).willReturn(0);

        SearchRestrictions searchRestrictions = new SearchRestrictions();
        ReflectionTestUtils.setField(searchRestrictions, "dataAccessService", dataAccessService);

        // when
        qualityControlService.generateQualityControl(viewDefinitionState, state, new String[] { "qualityControls" });

        // then
        verify(state).addMessage("qualityControls.qualityControls.generated.success", MessageType.SUCCESS);
    }

    @Test
    public void shouldEnableCalendarsOnPreRender() {
        // given
        FieldComponent dateFrom = mock(FieldComponent.class);
        FieldComponent dateTo = mock(FieldComponent.class);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("dateFrom")).willReturn(dateFrom);
        given(state.getComponentByReference("dateTo")).willReturn(dateTo);

        // when
        qualityControlService.enableCalendarsOnRender(state);

        // then
        verify(dateFrom).setEnabled(true);
        verify(dateTo).setEnabled(true);
    }

    @Test
    public void shouldSetQuantitiesToDefaultsIfEmpty() {
        // given
        FormComponent form = mock(FormComponent.class, Mockito.RETURNS_DEEP_STUBS);

        FieldComponent takenForControl = mock(FieldComponent.class);
        FieldComponent rejectedQuantity = mock(FieldComponent.class);
        FieldComponent acceptedDefectsQuantity = mock(FieldComponent.class);

        ViewDefinitionState state = mock(ViewDefinitionState.class);
        given(state.getComponentByReference("form")).willReturn(form);
        given(state.getComponentByReference("takenForControlQuantity")).willReturn(takenForControl);
        given(state.getComponentByReference("takenForControlQuantity").getFieldValue()).willReturn(null);
        given(state.getComponentByReference("rejectedQuantity")).willReturn(rejectedQuantity);
        given(state.getComponentByReference("rejectedQuantity").getFieldValue()).willReturn(null);
        given(state.getComponentByReference("acceptedDefectsQuantity")).willReturn(acceptedDefectsQuantity);
        given(state.getComponentByReference("acceptedDefectsQuantity").getFieldValue()).willReturn(null);

        // when
        qualityControlService.setQuantitiesToDefaulIfEmpty(state);

        // then
        verify(takenForControl).setFieldValue(BigDecimal.ONE);
        verify(rejectedQuantity).setFieldValue(BigDecimal.ZERO);
        verify(acceptedDefectsQuantity).setFieldValue(BigDecimal.ZERO);
    }

    @Test
    public void shouldSetCommentAsRequiredOnPositiveDefectsQuantity() {
        // given
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        FieldComponent acceptedDefectsQuantity = mock(FieldComponent.class);
        FieldComponent comment = mock(FieldComponent.class);

        given(viewDefinitionState.getComponentByReference("comment")).willReturn(comment);
        given(acceptedDefectsQuantity.getFieldValue()).willReturn("1");
        given(acceptedDefectsQuantity.getFieldValue().toString()).willReturn("1");

        // when
        qualityControlService.checkAcceptedDefectsQuantity(viewDefinitionState, acceptedDefectsQuantity, new String[] {});

        // then
        verify(comment).setRequired(true);
    }

    @Test
    public void shouldSetCommentAsNotRequiredOnPositiveDefectsQuantity() {
        // given
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        FieldComponent state = mock(FieldComponent.class);
        FieldComponent comment = mock(FieldComponent.class);

        given(viewDefinitionState.getComponentByReference("comment")).willReturn(comment);
        given(state.getFieldValue()).willReturn("0");
        given(state.getFieldValue().toString()).willReturn("0");

        // when
        qualityControlService.checkAcceptedDefectsQuantity(viewDefinitionState, state, new String[] {});

        // then
        verify(comment).setRequired(false);
    }

    @Test
    public void shouldSetQualityControlInstructionToDefaultFromTechnology() {
        // given
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        FieldComponent state = mock(FieldComponent.class);
        FieldComponent controlInstruction = mock(FieldComponent.class);
        DataDefinition orderDD = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        SearchCriteriaBuilder searchCriteria = mock(SearchCriteriaBuilder.class, RETURNS_DEEP_STUBS);

        Entity technology = mock(Entity.class);
        given(technology.getField("qualityControlInstruction")).willReturn("test");
        given(technology.getStringField("qualityControlInstruction")).willReturn("test");

        List<Entity> orders = new ArrayList<Entity>();
        Entity genealogy = mock(Entity.class);
        given(genealogy.getBelongsToField("technology")).willReturn(technology);
        orders.add(genealogy);

        given(viewDefinitionState.getComponentByReference("controlInstruction")).willReturn(controlInstruction);
        given(state.getFieldValue()).willReturn(1L);

        given(dataDefinitionService.get("orders", "order")).willReturn(orderDD);

        given(orderDD.find().setMaxResults(1).add(SearchRestrictions.eq("id", Mockito.anyLong()))).willReturn(searchCriteria);

        given(searchCriteria.list().getEntities()).willReturn(orders);

        // when
        qualityControlService.setQualityControlInstruction(viewDefinitionState, state, new String[] {});

        // then
        verify(controlInstruction).setFieldValue("");
        verify(controlInstruction).setFieldValue("test");
    }

    @Test
    public void shouldSetOperationAsRequired() {
        // given
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        FieldComponent operation = mock(FieldComponent.class);

        given(viewDefinitionState.getComponentByReference("operation")).willReturn(operation);

        // when
        qualityControlService.setOperationAsRequired(viewDefinitionState);

        // then
        verify(operation).setRequired(true);
    }

    @Test
    public void shouldSetQualityControlTypeHiddenFieldToQualityControlsForBatch() {
        // given
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        FormComponent qualityControlsForm = mock(FormComponent.class);
        FieldComponent qualityControlType = mock(FieldComponent.class);
        FieldComponent closed = mock(FieldComponent.class);

        given(viewDefinitionState.getComponentByReference("form")).willReturn(qualityControlsForm);
        given(qualityControlsForm.getName()).willReturn("qualityControlForBatch");
        given(viewDefinitionState.getComponentByReference("qualityControlType")).willReturn(qualityControlType);
        given(viewDefinitionState.getComponentByReference("closed")).willReturn(closed);

        // when
        qualityControlService.setQualityControlTypeHiddenField(viewDefinitionState);

        // then
        verify(qualityControlType).setFieldValue("qualityControlsForBatch");
    }

    @Test
    public void shouldSetErrorMessageOnEmptyOperationField() {
        // given
        DataDefinition qualityControlDD = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        FieldDefinition operation = mock(FieldDefinition.class);

        Entity qualityControl = mock(Entity.class);
        given(qualityControl.getStringField("qualityControlType")).willReturn("qualityControlsForOperation");
        given(qualityControl.getBelongsToField("operation")).willReturn(null);

        given(qualityControlDD.getField("operation")).willReturn(operation);

        // when
        qualityControlService.checkIfOperationIsRequired(qualityControlDD, qualityControl);

        // then
        verify(qualityControl).addGlobalError("qcadooView.validate.global.error.custom");
        verify(qualityControl).addError(qualityControlDD.getField("operation"),
                "qualityControls.quality.control.validate.global.error.operation");
    }

    @Test
    public void shouldSetQuantitiesToDefaultValues() {
        // given
        DataDefinition qualityControlDD = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        Entity entity = mock(Entity.class);
        given(entity.getStringField("qualityControlType")).willReturn("qualityControlsForUnit");
        given(entity.getDecimalField("controlledQuantity")).willReturn(null);
        given(entity.getDecimalField("takenForControlQuantity")).willReturn(null);
        given(entity.getDecimalField("rejectedQuantity")).willReturn(null);
        given(entity.getDecimalField("acceptedDefectsQuantity")).willReturn(null);

        // when
        qualityControlService.checkIfQuantitiesAreCorrect(qualityControlDD, entity);

        // then
        verify(entity).setField("controlledQuantity", BigDecimal.ZERO);
        verify(entity).setField("takenForControlQuantity", BigDecimal.ZERO);
        verify(entity).setField("rejectedQuantity", BigDecimal.ZERO);
        verify(entity).setField("acceptedDefectsQuantity", BigDecimal.ZERO);
    }

    @Test
    public void shouldAddErrorMessageOnTooLargeRejectedQuantity() {
        // given
        DataDefinition qualityControlDD = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        Entity entity = mock(Entity.class);
        given(entity.getStringField("qualityControlType")).willReturn("qualityControlsForUnit");
        given(entity.getDecimalField("controlledQuantity")).willReturn(null);
        given(entity.getDecimalField("takenForControlQuantity")).willReturn(new BigDecimal("1"));
        given(entity.getDecimalField("rejectedQuantity")).willReturn(new BigDecimal("5"));
        given(entity.getDecimalField("acceptedDefectsQuantity")).willReturn(null);

        // when
        qualityControlService.checkIfQuantitiesAreCorrect(qualityControlDD, entity);

        // then
        verify(entity).addGlobalError("qcadooView.validate.global.error.custom");
        verify(entity).addError(qualityControlDD.getField("rejectedQuantity"),
                "qualityControls.quality.control.validate.global.error.rejectedQuantity.tooLarge");
    }

    @Test
    public void shouldAddErrorMessageOnTooLargeAcceptedDefectsQuantity() {
        // given
        DataDefinition qualityControlDD = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        Entity entity = mock(Entity.class);
        given(entity.getStringField("qualityControlType")).willReturn("qualityControlsForUnit");
        given(entity.getDecimalField("controlledQuantity")).willReturn(null);
        given(entity.getDecimalField("takenForControlQuantity")).willReturn(new BigDecimal("5"));
        given(entity.getDecimalField("rejectedQuantity")).willReturn(new BigDecimal("5"));
        given(entity.getDecimalField("acceptedDefectsQuantity")).willReturn(new BigDecimal("10"));

        // when
        qualityControlService.checkIfQuantitiesAreCorrect(qualityControlDD, entity);

        // then
        verify(entity).addGlobalError("qcadooView.validate.global.error.custom");
        verify(entity).addError(qualityControlDD.getField("acceptedDefectsQuantity"),
                "qualityControls.quality.control.validate.global.error.acceptedDefectsQuantity.tooLarge");
    }
}