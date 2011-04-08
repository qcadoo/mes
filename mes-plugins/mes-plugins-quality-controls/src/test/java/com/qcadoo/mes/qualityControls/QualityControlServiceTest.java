/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.RestrictionOperator;
import com.qcadoo.model.api.search.Restrictions;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.internal.DefaultEntity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.components.FieldComponentState;
import com.qcadoo.view.components.form.FormComponentState;
import com.qcadoo.view.components.grid.GridComponentState;
import com.qcadoo.view.components.lookup.LookupComponentState;
import com.qcadoo.view.components.select.SelectComponentState;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FormComponentState.class, SelectComponentState.class, GridComponentState.class, LookupComponentState.class })
public class QualityControlServiceTest {

    private SecurityService securityService = null;

    private QualityControlService qualityControlService;

    private DataDefinitionService dataDefinitionService;

    private TranslationService translationService;

    private NumberGeneratorService numberGeneratorService;

    @Before
    public void init() {
        securityService = mock(SecurityService.class);
        dataDefinitionService = mock(DataDefinitionService.class, RETURNS_DEEP_STUBS);
        qualityControlService = new QualityControlService();
        translationService = mock(TranslationService.class);
        numberGeneratorService = mock(NumberGeneratorService.class);
        setField(qualityControlService, "dataDefinitionService", dataDefinitionService);
        setField(qualityControlService, "securityService", securityService);
        setField(qualityControlService, "translationService", translationService);
        setField(qualityControlService, "numberGeneratorService", numberGeneratorService);
    }

    @Test
    public void shouldSetRequiredOnCommentForControlResult() {
        // given
        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);

        FieldComponentState comment = mock(FieldComponentState.class);
        FieldComponentState controlResult = mock(FieldComponentState.class);

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
        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);

        FieldComponentState comment = mock(FieldComponentState.class);
        FieldComponentState acceptedDefectsQuantity = mock(FieldComponentState.class);

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
        SelectComponentState resultType = mock(SelectComponentState.class);
        FieldComponentState comment = mock(FieldComponentState.class);

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

        given(entity.getField("qualityControlType")).willReturn("qualityControlsForOrder");
        given(entity.getField("controlResult")).willReturn("03objection");
        given(entity.getField("comment")).willReturn(null);

        // when
        qualityControlService.checkIfCommentForResultOrQuantityIsReq(dataDefinition, entity);

        // then
        verify(entity).addGlobalError("core.validate.global.error.custom");
        verify(entity).addError(dataDefinition.getField("comment"),
                "qualityControls.quality.control.validate.global.error.comment");
    }

    @Test
    public void shouldSetErrorOnCommentFieldForAcceptedDefectsQuantity() {
        // given
        Entity entity = mock(Entity.class);
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);

        given(entity.getField("qualityControlType")).willReturn("qualityControlsForUnit");
        given(entity.getField("acceptedDefectsQuantity")).willReturn(new BigDecimal("1"));
        given(entity.getField("comment")).willReturn(null);

        // when
        qualityControlService.checkIfCommentForResultOrQuantityIsReq(dataDefinition, entity);

        // then
        verify(entity).addGlobalError("core.validate.global.error.custom");
        verify(entity).addError(dataDefinition.getField("comment"),
                "qualityControls.quality.control.validate.global.error.comment");
    }

    @Test
    public void shouldCloseQualityControlForFormComponentState() {
        // given
        FormComponentState state = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        FieldComponentState controlResult = mock(FieldComponentState.class);
        FieldComponentState closed = mock(FieldComponentState.class);
        FieldComponentState staff = mock(FieldComponentState.class);
        FieldComponentState date = mock(FieldComponentState.class);
        FieldComponentState qualityControlType = mock(FieldComponentState.class);

        given(state.getFieldValue()).willReturn(7L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(viewDefinitionState.getComponentByReference("controlResult")).willReturn(controlResult);
        given(viewDefinitionState.getComponentByReference("closed")).willReturn(closed);
        given(viewDefinitionState.getComponentByReference("staff")).willReturn(staff);
        given(viewDefinitionState.getComponentByReference("date")).willReturn(date);
        given(viewDefinitionState.getComponentByReference("qualityControlType")).willReturn(qualityControlType);
        given(((FieldComponentState) viewDefinitionState.getComponentByReference("qualityControlType")).getFieldValue())
                .willReturn("qualityControlsForUnit");
        given(qualityControlType.equals("qualityControlsForOrder")).willReturn(false);
        given(qualityControlType.equals("qualityControlsForOperation")).willReturn(false);
        given(controlResult.getFieldValue()).willReturn("03objection");
        given(securityService.getCurrentUserName()).willReturn("admin");
        given(translationService.translate("qualityControls.quality.control.closed.success", Locale.ENGLISH)).willReturn(
                "qualityControls.quality.control.closed.success.pl");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityForOrder" });

        // then
        verify(closed).setFieldValue(true);
    }

    @Test
    public void shouldCloseQualityControlForGridComponentState() {
        // given
        GridComponentState state = mock(GridComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        Entity qualityControl = mock(Entity.class);
        DataDefinition qualityControlDD = mock(DataDefinition.class);
        FieldComponentState controlResult = mock(FieldComponentState.class);
        given(qualityControl.getField("qualityControlType")).willReturn("qualityControlsForUnit");
        given(dataDefinitionService.get("qualityControls", "qualityControl")).willReturn(qualityControlDD);
        given(qualityControlDD.get(7L)).willReturn(qualityControl);
        given(viewDefinitionState.getComponentByReference("controlResult")).willReturn(controlResult);
        given(qualityControl.getField("controlResult")).willReturn("01correct");
        given(controlResult.getFieldValue()).willReturn("01correct");
        given(state.getFieldValue()).willReturn(7L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(securityService.getCurrentUserName()).willReturn("admin");
        given(translationService.translate("qualityControls.quality.control.closed.success", Locale.ENGLISH)).willReturn(
                "qualityControls.quality.control.closed.success.pl");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityControl" });

        // then
        verify(qualityControl).setField("staff", "admin");
        verify(qualityControl).setField("closed", true);
        verify(state).addMessage("qualityControls.quality.control.closed.success.pl", MessageType.SUCCESS);
    }

    @Test
    public void shouldAddFailureMessageOnEmptyControlResultTypeForFormComponent() {
        // given
        FormComponentState state = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        FieldComponentState controlResult = mock(FieldComponentState.class);
        Entity qualityControl = mock(Entity.class);
        DataDefinition qualityControlDD = mock(DataDefinition.class);
        FieldComponentState qualityControlType = mock(FieldComponentState.class);

        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(state.getFieldValue()).willReturn(7L);
        given(qualityControl.getField("controlResult")).willReturn("");
        given(dataDefinitionService.get("qualityControls", "qualityControl")).willReturn(qualityControlDD);
        given(qualityControlDD.get(7L)).willReturn(qualityControl);
        given(viewDefinitionState.getComponentByReference("controlResult")).willReturn(controlResult);
        given(controlResult.getFieldValue()).willReturn(null);
        given(viewDefinitionState.getComponentByReference("qualityControlType")).willReturn(qualityControlType);
        given(((FieldComponentState) viewDefinitionState.getComponentByReference("qualityControlType")).getFieldValue())
                .willReturn("qualityControlsForOrder");
        given(qualityControlType.equals("qualityControlsForOrder")).willReturn(true);
        given(qualityControlType.equals("qualityControlsForOperation")).willReturn(true);
        given(translationService.translate("qualityControls.quality.control.result.missing", Locale.ENGLISH)).willReturn(
                "qualityControls.quality.control.result.missing.pl");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityControls" });

        // then
        verify(controlResult).addMessage("qualityControls.quality.control.result.missing.pl", MessageType.FAILURE);
        verify(state).addMessage("qualityControls.quality.control.result.missing.pl", MessageType.FAILURE);
    }

    @Test
    public void shouldAddFailureMessageOnEmptyControlResultTypeForGridComponent() {
        // given
        GridComponentState state = mock(GridComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        FieldComponentState controlResult = mock(FieldComponentState.class);
        Entity qualityControl = mock(Entity.class);
        DataDefinition qualityControlDD = mock(DataDefinition.class);
        FieldDefinition controlResultField = mock(FieldDefinition.class);
        FieldComponentState qualityControlType = mock(FieldComponentState.class);

        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(qualityControlDD.getField("controlResult")).willReturn(controlResultField);
        given(state.getFieldValue()).willReturn(7L);
        given(qualityControl.getField("controlResult")).willReturn("");
        given(dataDefinitionService.get("qualityControls", "qualityControl")).willReturn(qualityControlDD);
        given(qualityControlDD.get(7L)).willReturn(qualityControl);
        given(viewDefinitionState.getComponentByReference("controlResult")).willReturn(controlResult);
        given(controlResult.getFieldValue()).willReturn(null);
        given(viewDefinitionState.getComponentByReference("qualityControlType")).willReturn(qualityControlType);
        given((String) qualityControl.getField("qualityControlType")).willReturn("qualityControlsForOrder");
        given(qualityControlType.equals("qualityControlsForOrder")).willReturn(true);
        given(qualityControlType.equals("qualityControlsForOperation")).willReturn(true);
        given(translationService.translate("qualityControls.quality.control.result.missing", Locale.ENGLISH)).willReturn(
                "qualityControls.quality.control.result.missing.pl");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityControls" });

        // then
        verify(state).addMessage("qualityControls.quality.control.result.missing.pl", MessageType.FAILURE);
    }

    @Test
    public void shouldAddFailureMessageOnNoValueForFormComponent() {
        // given
        FormComponentState state = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        given(state.getFieldValue()).willReturn(null);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(translationService.translate("core.form.entityWithoutIdentifier", Locale.ENGLISH)).willReturn(
                "core.form.entityWithoutIdentifier.pl");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityControls" });

        // then
        verify(state).addMessage("core.form.entityWithoutIdentifier.pl", MessageType.FAILURE);
    }

    @Test
    public void shouldAddFailureMessageOnNoValueForNonFormComponent() {
        // given
        GridComponentState state = mock(GridComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        given(state.getFieldValue()).willReturn(null);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(translationService.translate("core.grid.noRowSelectedError", Locale.ENGLISH)).willReturn(
                "core.grid.noRowSelectedError.pl");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityControls" });

        // then
        verify(state).addMessage("core.grid.noRowSelectedError.pl", MessageType.FAILURE);
    }

    @Test
    public void shouldGenerateQualityControlForBatch() {
        // given
        DataDefinition genealogyDataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);

        List<Entity> genealogies = new ArrayList<Entity>();
        Entity genealogy = new DefaultEntity(genealogyDataDefinition);
        genealogy.setField("batch", "1");
        genealogies.add(genealogy);

        GridComponentState state = mock(GridComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        DataDefinition orderDataDefinition = mock(DataDefinition.class);
        DataDefinition qualityForBatchDataDefinition = mock(DataDefinition.class);

        Entity order = mock(Entity.class);
        Entity technology = mock(Entity.class);

        SearchCriteriaBuilder searchCriteria = mock(SearchCriteriaBuilder.class, RETURNS_DEEP_STUBS);

        given(dataDefinitionService.get("orders", "order")).willReturn(orderDataDefinition);
        given(dataDefinitionService.get("qualityControls", "qualityControl")).willReturn(qualityForBatchDataDefinition);
        given(orderDataDefinition.get(7L)).willReturn(order);
        given(order.getField("technology")).willReturn(technology);
        given(technology.getField("qualityControlType")).willReturn("01forBatch");
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(state.getFieldValue()).willReturn(7L);
        given(dataDefinitionService.get("genealogies", "genealogy")).willReturn(genealogyDataDefinition);
        given(genealogyDataDefinition.find().restrictedWith(Restrictions.eq("order.id", 7L))).willReturn(searchCriteria);
        given(searchCriteria.list().getEntities()).willReturn(genealogies);
        given(numberGeneratorService.generateNumber("qualityControls", "qualityControl")).willReturn("1");
        given(order.getField("plannedQuantity")).willReturn(new BigDecimal("1"));
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(order.getBelongsToField("technology").getField("qualityControlInstruction")).willReturn("test");
        given(translationService.translate("qualityControls.qualityControls.generated.success", Locale.ENGLISH)).willReturn(
                "qualityControls.qualityControls.generated.success.pl");

        // when
        qualityControlService.generateQualityControl(viewDefinitionState, state, new String[] { "qualityControls" });

        // then
        verify(state).addMessage("qualityControls.qualityControls.generated.success.pl", MessageType.SUCCESS);
    }

    @Test
    public void shouldGenerateQualityControlForUnit() {
        // given
        GridComponentState state = mock(GridComponentState.class);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        DataDefinition orderDataDefinition = mock(DataDefinition.class);
        Entity order = mock(Entity.class);

        Entity technology = mock(Entity.class);

        given(dataDefinitionService.get("orders", "order")).willReturn(orderDataDefinition);
        given(orderDataDefinition.get(7L)).willReturn(order);
        given(order.getField("technology")).willReturn(technology);
        given(technology.getField("qualityControlType")).willReturn("02forUnit");
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(state.getFieldValue()).willReturn(7L);
        given(translationService.translate("qualityControls.qualityControls.generated.success", Locale.ENGLISH)).willReturn(
                "qualityControls.qualityControls.generated.success.pl");
        given(technology.getField("unitSamplingNr")).willReturn(new BigDecimal("2"));
        given(order.getField("plannedQuantity")).willReturn(new BigDecimal("5"));
        given(numberGeneratorService.generateNumber("qualityControls", "qualityControl")).willReturn("1");
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(order.getBelongsToField("technology").getField("qualityControlInstruction")).willReturn("test");

        // when
        qualityControlService.generateQualityControl(viewDefinitionState, state, new String[] { "qualityControls" });

        // then
        verify(state).addMessage("qualityControls.qualityControls.generated.success.pl", MessageType.SUCCESS);
    }

    @Test
    public void shouldEnableCalendarsOnPreRender() {
        // given
        FieldComponentState dateFrom = mock(FieldComponentState.class);
        FieldComponentState dateTo = mock(FieldComponentState.class);

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
        FormComponentState form = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);

        FieldComponentState takenForControl = mock(FieldComponentState.class);
        FieldComponentState rejectedQuantity = mock(FieldComponentState.class);
        FieldComponentState acceptedDefectsQuantity = mock(FieldComponentState.class);

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
        FieldComponentState acceptedDefectsQuantity = mock(FieldComponentState.class);
        FieldComponentState comment = mock(FieldComponentState.class);

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
        FieldComponentState state = mock(FieldComponentState.class);
        FieldComponentState comment = mock(FieldComponentState.class);

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
        LookupComponentState state = mock(LookupComponentState.class);
        FieldComponentState controlInstruction = mock(FieldComponentState.class);
        DataDefinition orderDD = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        DataDefinition technologyDD = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        SearchCriteriaBuilder searchCriteria = mock(SearchCriteriaBuilder.class, RETURNS_DEEP_STUBS);

        Entity technology = new DefaultEntity(technologyDD);
        technology.setField("qualityControlInstruction", "test");

        List<Entity> orders = new ArrayList<Entity>();
        Entity genealogy = new DefaultEntity(orderDD);
        genealogy.setField("technology", technology);
        orders.add(genealogy);

        given(viewDefinitionState.getComponentByReference("controlInstruction")).willReturn(controlInstruction);
        given(state.getFieldValue()).willReturn(1L);

        given(dataDefinitionService.get("orders", "order")).willReturn(orderDD);

        given(
                orderDD.find().withMaxResults(1)
                        .restrictedWith(Restrictions.idRestriction(Mockito.anyLong(), RestrictionOperator.EQ))).willReturn(
                searchCriteria);

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
        LookupComponentState operation = mock(LookupComponentState.class);

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
        FormComponentState qualityControlsForm = mock(FormComponentState.class);
        FieldComponentState qualityControlType = mock(FieldComponentState.class);

        given(viewDefinitionState.getComponentByReference("form")).willReturn(qualityControlsForm);
        given(qualityControlsForm.getName()).willReturn("qualityControlForBatch");
        given(viewDefinitionState.getComponentByReference("qualityControlType")).willReturn(qualityControlType);

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
        given(qualityControl.getField("qualityControlType")).willReturn("qualityControlsForOperation");
        given(qualityControl.getField("operation")).willReturn(null);

        given(qualityControlDD.getField("operation")).willReturn(operation);

        // when
        qualityControlService.checkIfOperationIsRequired(qualityControlDD, qualityControl);

        // then
        verify(qualityControl).addGlobalError("core.validate.global.error.custom");
        verify(qualityControl).addError(qualityControlDD.getField("operation"),
                "qualityControls.quality.control.validate.global.error.operation");
    }

    @Test
    public void shouldSetQuantitiesToDefaultValues() {
        // given
        DataDefinition qualityControlDD = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        Entity entity = mock(Entity.class);
        given(entity.getField("qualityControlType")).willReturn("qualityControlsForUnit");
        given(entity.getField("controlledQuantity")).willReturn(null);
        given(entity.getField("takenForControlQuantity")).willReturn(null);
        given(entity.getField("rejectedQuantity")).willReturn(null);
        given(entity.getField("acceptedDefectsQuantity")).willReturn(null);

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
        given(entity.getField("qualityControlType")).willReturn("qualityControlsForUnit");
        given(entity.getField("controlledQuantity")).willReturn(null);
        given(entity.getField("takenForControlQuantity")).willReturn(new BigDecimal("1"));
        given(entity.getField("rejectedQuantity")).willReturn(new BigDecimal("5"));
        given(entity.getField("acceptedDefectsQuantity")).willReturn(null);

        // when
        qualityControlService.checkIfQuantitiesAreCorrect(qualityControlDD, entity);

        // then
        verify(entity).addGlobalError("core.validate.global.error.custom");
        verify(entity).addError(qualityControlDD.getField("rejectedQuantity"),
                "qualityControls.quality.control.validate.global.error.rejectedQuantity.tooLarge");
    }

    @Test
    public void shouldAddErrorMessageOnTooLargeAcceptedDefectsQuantity() {
        // given
        DataDefinition qualityControlDD = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        Entity entity = mock(Entity.class);
        given(entity.getField("qualityControlType")).willReturn("qualityControlsForUnit");
        given(entity.getField("controlledQuantity")).willReturn(null);
        given(entity.getField("takenForControlQuantity")).willReturn(new BigDecimal("5"));
        given(entity.getField("rejectedQuantity")).willReturn(new BigDecimal("5"));
        given(entity.getField("acceptedDefectsQuantity")).willReturn(new BigDecimal("10"));

        // when
        qualityControlService.checkIfQuantitiesAreCorrect(qualityControlDD, entity);

        // then
        verify(entity).addGlobalError("core.validate.global.error.custom");
        verify(entity).addError(qualityControlDD.getField("acceptedDefectsQuantity"),
                "qualityControls.quality.control.validate.global.error.acceptedDefectsQuantity.tooLarge");
    }
}
