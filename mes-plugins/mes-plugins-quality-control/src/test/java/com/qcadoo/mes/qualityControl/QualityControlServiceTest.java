package com.qcadoo.mes.qualityControl;

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

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.NumberGeneratorService;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.components.grid.GridComponentState;
import com.qcadoo.mes.view.components.select.SelectComponentState;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FormComponentState.class, SelectComponentState.class, GridComponentState.class })
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
        qualityControlService.checkIfCommentIsRequiredBasedOnResult(state, Locale.ENGLISH);

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
        qualityControlService.checkIfCommentIsRequiredBasedOnDefects(state, Locale.ENGLISH);

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

        given(entity.getField("controlResult")).willReturn("03objection");
        given(entity.getField("comment")).willReturn(null);

        // when
        qualityControlService.checkIfCommentForResultIsReq(dataDefinition, entity);

        // then
        verify(entity).addGlobalError("core.validate.global.error.custom");
        verify(entity).addError(dataDefinition.getField("comment"),
                "qualityControl.quality.control.validate.global.error.comment");
    }

    @Test
    public void shouldSetErrorOnCommentFieldForAcceptedDefectsQuantity() {
        // given
        Entity entity = mock(Entity.class);
        DataDefinition dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);

        given(entity.getField("acceptedDefectsQuantity")).willReturn(new BigDecimal("1"));
        given(entity.getField("comment")).willReturn(null);

        // when
        qualityControlService.checkIfCommentForQuantityIsReq(dataDefinition, entity);

        // then
        verify(entity).addGlobalError("core.validate.global.error.custom");
        verify(entity).addError(dataDefinition.getField("comment"),
                "qualityControl.quality.control.validate.global.error.comment");
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

        given(state.getFieldValue()).willReturn(7L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(viewDefinitionState.getComponentByReference("controlResult")).willReturn(controlResult);
        given(viewDefinitionState.getComponentByReference("closed")).willReturn(closed);
        given(viewDefinitionState.getComponentByReference("staff")).willReturn(staff);
        given(viewDefinitionState.getComponentByReference("date")).willReturn(date);
        given(controlResult.getFieldValue()).willReturn("03objection");
        given(securityService.getCurrentUserName()).willReturn("admin");
        given(translationService.translate("qualityControl.quality.control.closed.success", Locale.ENGLISH)).willReturn(
                "qualityControl.quality.control.closed.success.pl");

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
        given(dataDefinitionService.get("qualityControl", "qualityForOrder")).willReturn(qualityControlDD);
        given(qualityControlDD.get(7L)).willReturn(qualityControl);
        given(viewDefinitionState.getComponentByReference("controlResult")).willReturn(controlResult);
        given(qualityControl.getField("controlResult")).willReturn("01correct");
        given(controlResult.getFieldValue()).willReturn("01correct");
        given(state.getFieldValue()).willReturn(7L);
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(securityService.getCurrentUserName()).willReturn("admin");
        given(translationService.translate("qualityControl.quality.control.closed.success", Locale.ENGLISH)).willReturn(
                "qualityControl.quality.control.closed.success.pl");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityForOrder" });

        // then
        verify(qualityControl).setField("staff", "admin");
        verify(qualityControl).setField("closed", true);
        verify(state).addMessage("qualityControl.quality.control.closed.success.pl", MessageType.SUCCESS);
    }

    @Test
    public void shouldAddFailureMessageOnEmptyControlResultTypeForFormComponent() {
        // given
        FormComponentState state = mock(FormComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        FieldComponentState controlResult = mock(FieldComponentState.class);
        Entity qualityControl = mock(Entity.class);
        DataDefinition qualityControlDD = mock(DataDefinition.class);

        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(state.getFieldValue()).willReturn(7L);
        given(qualityControl.getField("controlResult")).willReturn("");
        given(dataDefinitionService.get("qualityControl", "qualityForOrder")).willReturn(qualityControlDD);
        given(qualityControlDD.get(7L)).willReturn(qualityControl);
        given(viewDefinitionState.getComponentByReference("controlResult")).willReturn(controlResult);
        given(controlResult.getFieldValue()).willReturn(null);
        given(translationService.translate("qualityControl.quality.control.result.missing", Locale.ENGLISH)).willReturn(
                "qualityControl.quality.control.result.missing.pl");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityForOrder" });

        // then
        verify(controlResult).addMessage("qualityControl.quality.control.result.missing.pl", MessageType.FAILURE);
        verify(state).addMessage("qualityControl.quality.control.result.missing.pl", MessageType.FAILURE);
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

        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(qualityControlDD.getField("controlResult")).willReturn(controlResultField);
        given(state.getFieldValue()).willReturn(7L);
        given(qualityControl.getField("controlResult")).willReturn("");
        given(dataDefinitionService.get("qualityControl", "qualityForOrder")).willReturn(qualityControlDD);
        given(qualityControlDD.get(7L)).willReturn(qualityControl);
        given(viewDefinitionState.getComponentByReference("controlResult")).willReturn(controlResult);
        given(controlResult.getFieldValue()).willReturn(null);
        given(translationService.translate("qualityControl.quality.control.result.missing", Locale.ENGLISH)).willReturn(
                "qualityControl.quality.control.result.missing.pl");

        // when
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityForOrder" });

        // then
        verify(state).addMessage("qualityControl.quality.control.result.missing.pl", MessageType.FAILURE);
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
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityForOrder" });

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
        qualityControlService.closeQualityControl(viewDefinitionState, state, new String[] { "qualityForOrder" });

        // then
        verify(state).addMessage("core.grid.noRowSelectedError.pl", MessageType.FAILURE);
    }

    @Test
    public void shouldGenerateQualityControlForBatch() {
        // given
        List<Entity> genealogies = new ArrayList<Entity>();
        Entity genealogy = new DefaultEntity("genealogies", "genealogy");
        genealogy.setField("batch", "1");
        genealogies.add(genealogy);

        GridComponentState state = mock(GridComponentState.class, Mockito.RETURNS_DEEP_STUBS);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        DataDefinition orderDataDefinition = mock(DataDefinition.class);
        DataDefinition genealogyDataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        DataDefinition qualityForBatchDataDefinition = mock(DataDefinition.class);

        Entity order = mock(Entity.class);
        Entity technology = mock(Entity.class);

        SearchCriteriaBuilder searchCriteria = mock(SearchCriteriaBuilder.class, RETURNS_DEEP_STUBS);

        given(dataDefinitionService.get("products", "order")).willReturn(orderDataDefinition);
        given(dataDefinitionService.get("qualityControl", "qualityForBatch")).willReturn(qualityForBatchDataDefinition);
        given(orderDataDefinition.get(7L)).willReturn(order);
        given(order.getField("technology")).willReturn(technology);
        given(technology.getField("qualityControlType")).willReturn("01forBatch");
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(state.getFieldValue()).willReturn(7L);
        given(dataDefinitionService.get("genealogies", "genealogy")).willReturn(genealogyDataDefinition);
        given(genealogyDataDefinition.find().restrictedWith(Restrictions.eq("order.id", 7L))).willReturn(searchCriteria);
        given(searchCriteria.list().getEntities()).willReturn(genealogies);
        given(numberGeneratorService.generateNumber("qualityForBatch")).willReturn("1");
        given(order.getField("plannedQuantity")).willReturn(new BigDecimal("1"));
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(order.getBelongsToField("technology").getField("qualityControlInstruction")).willReturn("test");
        given(translationService.translate("qualityControl.qualityControl.generated.success", Locale.ENGLISH)).willReturn(
                "qualityControl.qualityControl.generated.success.pl");

        // when
        qualityControlService.generateQualityControl(viewDefinitionState, state, new String[] { "qualityForBatch" });

        // then
        verify(state).addMessage("qualityControl.qualityControl.generated.success.pl", MessageType.SUCCESS);
    }

    @Test
    public void shouldGenerateQualityControlForUnit() {
        // given
        GridComponentState state = mock(GridComponentState.class);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        DataDefinition orderDataDefinition = mock(DataDefinition.class);
        Entity order = mock(Entity.class);

        Entity technology = mock(Entity.class);

        given(dataDefinitionService.get("products", "order")).willReturn(orderDataDefinition);
        given(orderDataDefinition.get(7L)).willReturn(order);
        given(order.getField("technology")).willReturn(technology);
        given(technology.getField("qualityControlType")).willReturn("02forUnit");
        given(state.getLocale()).willReturn(Locale.ENGLISH);
        given(state.getFieldValue()).willReturn(7L);
        given(translationService.translate("qualityControl.qualityControl.generated.success", Locale.ENGLISH)).willReturn(
                "qualityControl.qualityControl.generated.success.pl");
        given(technology.getField("unitSamplingNr")).willReturn(new BigDecimal("2"));
        given(order.getField("plannedQuantity")).willReturn(new BigDecimal("5"));
        given(numberGeneratorService.generateNumber("qualityForUnit")).willReturn("1");
        given(order.getBelongsToField("technology")).willReturn(technology);
        given(order.getBelongsToField("technology").getField("qualityControlInstruction")).willReturn("test");

        // when
        qualityControlService.generateQualityControl(viewDefinitionState, state, new String[] { "qualityForUnit" });

        // then
        verify(state).addMessage("qualityControl.qualityControl.generated.success.pl", MessageType.SUCCESS);
    }
}
